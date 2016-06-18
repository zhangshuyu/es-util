package com.hansight.es.utils;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ESUtils
 *
 * @author shuyu
 * @date 2016/4/22
 */
public class ESUtils {
    private static Logger logger = LoggerFactory.getLogger(ESUtils.class);
    private static List<String> indicesCache;
    private static TransportClient client;
    private static String esClusterName;
    private static List<String> esClusterNodes;

    static {
        try {
            esClusterName = ConfigUtils.getStringValue(ConfigUtils.ES_CLUSTER_NAME, null);
            esClusterNodes = ConfigUtils.getStringListValue(ConfigUtils.ES_CLUSTER_NODES, null);
        } catch (Exception e) {
//            logger.error("initial failed", e);
        }
        indicesCache = new ArrayList();
    }

    /**
     * 获取es client实例
     *
     * @return client
     */
    public synchronized static TransportClient getClient() {
        if (client == null) {
            client = getNewClient(esClusterName, esClusterNodes);
        }
        return client;
    }

    /**
     * 创建es client实例
     *
     * @param clusterName 集群名称
     * @param nodes       节点列表
     * @return client
     */
    public static TransportClient getNewClient(String clusterName, List<String> nodes) {
        if (clusterName == null || nodes == null) {
            logger.error("wrong configuration information of es cluster");
            return null;
        } else {
            Settings settings = Settings.settingsBuilder().put("cluster.name", clusterName).put("client.transport.sniff", true).build();
            final TransportClient clientNew  = TransportClient.builder().settings(settings).build();
            nodes.forEach(node -> {
                String[] hostAndPort = node.split(":");
                if (hostAndPort.length == 2) {
                    try {
                        clientNew.addTransportAddress(
                                new InetSocketTransportAddress(InetAddress.getByName(hostAndPort[0]), Integer.parseInt(hostAndPort[1]))
                        );
                    } catch (UnknownHostException e) {
                        logger.error("building es client error, when adding node ", e);
                    }
                }
            });
            return clientNew;
        }
    }

    /**
     * 关闭 client 连接
     */
    public synchronized void closeClient() {
        if (client != null) client.close();
    }

    /**
     * 创建index
     *
     * @param index index name
     * @return
     */
    public static boolean createIndex(String index) {
        CreateIndexResponse response = getClient().admin().indices().prepareCreate(index).execute().actionGet();
        return response.isAcknowledged();
    }

    /**
     * 创建index With default Mapping
     *
     * @param index
     * @return
     */
    public static boolean createIndexWithDefaultMapping(String index) {
        CreateIndexResponse response = getClient().admin().indices().prepareCreate(index).addMapping("_default_", defaultMapping(null)).execute().actionGet();
        return response.isAcknowledged();
    }

    public static boolean createIndexWithDefaultMappingCN(String index) {
        CreateIndexResponse response = getClient().admin().indices().prepareCreate(index).addMapping("_default_", defaultMapping("cn")).execute().actionGet();
        return response.isAcknowledged();
    }

    /**
     * 判断es index是否存在
     *
     * @param indices index列表
     * @return true or false
     */
    public static boolean indicesExists(String... indices) {
        try {
            IndicesExistsRequest request = new IndicesExistsRequest(indices);
            IndicesExistsResponse res = getClient().admin().indices().exists(request).actionGet();
            return res.isExists();
        } catch (Exception e) {
            logger.warn("check indices fail", e);
            return false;
        }
    }

    private static void indexCheck(String index) {
        if (!indicesCache.contains(index)) {
            if (!indicesExists(index)) {
                if (createIndexWithDefaultMapping(index)) indicesCache.add(index);
            } else indicesCache.add(index);
        }
    }

    /**
     * 设置默认Mapping
     *
     * @param indices
     * @return
     */
    public static boolean putDefaultMapping(String... indices) {
        try {
            return putMapping("_default_", defaultMapping(null), indices);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static XContentBuilder defaultMapping(String type) {
        try {
            XContentBuilder content = XContentFactory.jsonBuilder();
            content
                    .startObject()
                    .startObject("_default_")
                    .startArray("dynamic_templates")
                    .startObject()
                    .startObject("string_fields")
                    .startObject("mapping")
                    .field("index", "analyzed")
                    .field("omit_norms", true)
                    .field("type", "string");
            if (StringUtils.isNotEmpty(type) && "cn".equals(type)) content.field("analyzer", "ik_max_word");
            content
                    .startObject("fields")
                    .startObject("raw")
                    .field("index", "not_analyzed")
                    .field("ignore_above", 256)
                    .field("type", "string")
                    .endObject()
                    .endObject()
                    .endObject()
                    .field("match", "*")
                    .field("match_mapping_type", "string")
                    .endObject()
                    .endObject()
                    .endArray()
                    .endObject()
                    .endObject();

            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param map
     * @param indices
     * @return
     */
    public static boolean putMapping(Map<String, Object> map, String... indices) {
        final List<String> list = new ArrayList<>();
        map.forEach((type, content) -> {
            if (!putMapping(type, content, indices)) list.add(type);
        });
        return list.size() == 0;
    }

    public static boolean putMapping(String type, Object content, String... indices) {
        PutMappingRequestBuilder mappingRequest = getClient().admin().indices().preparePutMapping(indices);
        if (StringUtils.isNotEmpty(type)) mappingRequest.setType(type);
        PutMappingResponse response = mappingRequest.setSource(FasterXmlUtils.toNotEmptyJson(content)).get();
        return response.isAcknowledged();
    }

    public static String getDoc(String index, String type, String id) {
        try {
            GetResponse response = getClient().prepareGet(index, type, id).get();
            return FasterXmlUtils.toJson(response.getSource());
        } catch (Exception e) {
            logger.error("get doc has error ", e);
            return null;
        }
    }

    public static String getDocs(String index, String type) {
        try {
            SearchResponse response = getClient().prepareSearch(index).setTypes(type).setSize(9999).get();
            List<Map<String, Object>> data = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                data.add(hit.getSource());
            }
            return FasterXmlUtils.toJson(data);
        } catch (Exception e) {
            logger.error("get docs has error ", e);
            return null;
        }
    }

    public static void deleteIndex(String index) {
        getClient().admin().indices().prepareDelete(index).execute().actionGet();
    }

}
