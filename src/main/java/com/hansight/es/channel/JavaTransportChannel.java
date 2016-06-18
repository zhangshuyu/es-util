package com.hansight.es.channel;

import com.hansight.es.domain.ESDoc;
import com.hansight.es.domain.EsConfig;
import com.hansight.es.utils.DefaultConfig;
import com.hansight.es.utils.ESUtils;
import com.hansight.es.utils.FasterXmlUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequestBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaTransportChannel
 *
 * @author shuyu
 * @date 2016/6/16
 */
public class JavaTransportChannel implements TransportChannel {
    private static final Logger logger = LoggerFactory.getLogger(JavaTransportChannel.class);
    private TransportClient client;
    private String keepAlive;
    private int size;
    private EsConfig config;

    public JavaTransportChannel(EsConfig config) {
        this.config = config;
        this.client = ESUtils.getNewClient(this.config.getClusterName(), this.config.getNodes());
        this.keepAlive = this.config.getScrollAlive();
        this.size = this.config.getSize();
    }

    @Override
    public void createIndex(String index, Map<String, Object> settings, Map<String, Object> mappings) {
        CreateIndexRequestBuilder builder = client.admin().indices().prepareCreate(index);
        if (settings != null)
            builder.setSettings(FasterXmlUtils.toJson(settings));
        if (mappings != null)
            mappings.forEach((type, source) -> builder.addMapping(type, (Map<String, Object>) source));
        builder.get();
    }

    @Override
    public boolean indexExists(String index) {
        try {
            IndicesExistsRequest request = new IndicesExistsRequest(index);
            IndicesExistsResponse res = client.admin().indices().exists(request).actionGet();
            return res.isExists();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> getMapping(String[] indices, String[] types) {
        final Map<String, Object> mappingObj = new HashMap<>();
        try {
            GetMappingsRequestBuilder mappingsRequestBuilder = client.admin().indices().prepareGetMappings();
            if (indices != null && indices.length > 0)
                mappingsRequestBuilder.setIndices(indices);
            if (types != null && types.length > 0)
                mappingsRequestBuilder.addTypes(types);
            GetMappingsResponse mappingsResponse = mappingsRequestBuilder.get();
            mappingsResponse.mappings().forEach(indexValue -> {
                final Map<String, Map<String, Object>> indexMapping = new HashMap<>();
                indexValue.value.forEach((value) -> {
                    try {
                        indexMapping.put(value.key, value.value.getSourceAsMap());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                Map<String, Object> mappingsWrapper = new HashMap<String, Object>();
                mappingsWrapper.put("mappings", indexMapping);
                mappingObj.put(indexValue.key, mappingsWrapper);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mappingObj;
    }

    @Override
    public void putMapping(String index, String type, Map<String, Object> source) {
        client.admin().indices().preparePutMapping(index).setType(type).setSource(source).get();
    }

    @Override
    public Tuple<String, List<ESDoc>> searchByScroll(String[] indices, String[] types, String scrollId) {
        try {
            SearchResponse response;
            if (StringUtils.isNotEmpty(scrollId))
                response = client.prepareSearchScroll(scrollId).setScroll(this.keepAlive).get();
            else {
                SearchRequestBuilder builder = client.prepareSearch();
                if (indices != null && indices.length > 0)
                    builder.setIndices(indices);
                if (types != null && types.length > 0)
                    builder.setTypes(types);
                response = builder.setScroll(this.keepAlive).setSize(this.size).get();
            }
            List<ESDoc> docs = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                docs.add(new ESDoc(hit.getIndex(), hit.getType(), hit.getId(), hit.getSource()));
            }
            return new Tuple<>(response.getScrollId(), docs);
        } catch (Exception e) {
            e.printStackTrace();
            return new Tuple<>("", null);
        }
    }

    @Override
    public void saveBulk(List<ESDoc> docs) {
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            docs.forEach(e ->
                    bulkRequest.add(client.prepareIndex(e.getIndex(), e.getType(), e.getId()).setSource(e.getSource()))
            );
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                logger.warn("save docs has failures :" + bulkResponse.buildFailureMessage());
            }
        } catch (Exception ex) {
            logger.error("save docs has error ", ex);
        }
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public boolean closeIndex(String index) {
        CloseIndexResponse response = client.admin().indices().prepareClose(index).get();
        return response.isAcknowledged();
    }

    @Override
    public boolean openIndex(String index) {
        OpenIndexResponse response = client.admin().indices().prepareOpen(index).get();
        return response.isAcknowledged();
    }


    public void putAnalyzer(String index, Map<String, String> params) {
        AnalyzeRequestBuilder builder = client.admin().indices().prepareAnalyze(index);
        if (params != null) {
            if (params.get("analyzer") != null) builder.setAnalyzer(params.get("analyzer"));
            if (params.get("char_filter") != null) builder.setCharFilters(params.get("char_filter"));
            if (params.get("tokenizer ") != null) builder.setTokenizer(params.get("tokenizer "));
            if (params.get("filter") != null) builder.setTokenFilters(params.get("filter"));
        }
        System.out.println(builder.request().toString());
        AnalyzeResponse response = builder.get();
        System.out.println(response.detail());
    }

    @Override
    public Map<String, Object> getSettings(String... indices) {
        GetSettingsResponse response = client.admin().indices().prepareGetSettings(indices).setNames(this.config.getSettingsIncludedFields().toArray(new String[]{})).get();
        Map<String, Object> settings = new HashMap<>();
        response.getIndexToSettings().forEach((v) -> {
            Map<String, Object> settingsWrapper = new HashMap<String, Object>();
            settingsWrapper.put("settings", v.value.getAsStructuredMap());
            settings.put(v.key, settingsWrapper);
        });
        return settings;
    }
}
