package com.hansight.es.channel;

import com.hansight.es.domain.ESDoc;
import com.hansight.es.domain.EsConfig;
import com.hansight.es.utils.DefaultConfig;
import com.hansight.es.utils.ESUtils;
import com.hansight.es.utils.FasterXmlUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.Tuple;
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

    public JavaTransportChannel(EsConfig config) {
        this.client = ESUtils.getNewClient(config.getClusterName(), config.getNodes());
        this.keepAlive = config.getScrollAlive();
        this.size = config.getSize();
    }

    @Override
    public void createIndex(String index, Map<String, Object> mappings) {
        CreateIndexRequestBuilder builder = client.admin().indices().prepareCreate(index);
        mappings.forEach((type, source) -> builder.addMapping(type, (Map) source));
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
    public Tuple<String, List<String>> getMapping(String[] indices, String[] types) {
        try {
            GetMappingsRequestBuilder mappingsRequestBuilder = client.admin().indices().prepareGetMappings();
            if (indices != null && indices.length > 0)
                mappingsRequestBuilder.setIndices(indices);
            if (types != null && types.length > 0)
                mappingsRequestBuilder.addTypes(types);
            GetMappingsResponse mappingsResponse = mappingsRequestBuilder.get();
            final Map<String, Object> mappingObj = new HashMap<>();
            final List<String> indicesReal = new ArrayList<>();
            mappingsResponse.mappings().forEach(indexValue -> {
                final Map<String, Map<String, Object>> indexMapping = new HashMap<>();
                indexValue.value.forEach((value) -> {
                    try {
                        indexMapping.put(value.key, value.value.getSourceAsMap());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                indicesReal.add(indexValue.key);
                Map<String, Object> mappingsWrapper = new HashMap<String, Object>();
                mappingsWrapper.put("mappings", indexMapping);
                mappingObj.put(indexValue.key, mappingsWrapper);
            });
            return new Tuple<>(FasterXmlUtils.toJson(mappingObj), indicesReal);
        } catch (Exception e) {
            e.printStackTrace();
            return new Tuple<>("", null);
        }
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
}
