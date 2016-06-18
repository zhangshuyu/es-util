package com.hansight.es.channel;

import com.hansight.es.domain.ESDoc;
import com.hansight.es.domain.EsConfig;
import com.hansight.es.utils.FasterXmlUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HttpTransportChannel
 *
 * @author shuyu
 * @date 2016/6/17
 */
public class HttpTransportChannel implements TransportChannel {
    private static final Logger logger = LoggerFactory.getLogger(HttpTransportChannel.class);
    private String address;
    private String keepAlive;
    private int size;

    public HttpTransportChannel(EsConfig config) {
        this.address = config.getNodes().get(0);
        this.keepAlive = config.getScrollAlive();
        this.size = config.getSize();
    }

    @Override
    public void createIndex(String index, Map<String, Object> mappings) {
        try {
            HttpPut put = new HttpPut(getRequestUrl(new String[]{index}, null));
            execute(put);
            mappings.forEach((type, source) -> {
                putMapping(index, type, (Map<String, Object>) source);
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("create index error", e);
        }
    }

    @Override
    public boolean indexExists(String index) {
        HttpHead head = new HttpHead(getRequestUrl(new String[]{index}, null));
        try {
            CloseableHttpClient client = HttpClients.custom().build();
            CloseableHttpResponse response = client.execute(head);
            int code = response.getStatusLine().getStatusCode();
            response.close();
            return code == 200;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("check index exist error", e);
            return false;
        }
    }

    @Override
    public void putMapping(String index, String type, Map<String, Object> source) {
        try {
            HttpPut put = new HttpPut(getRequestUrl(new String[]{index}, null) + "_mapping/" + type);
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject().field(type, source).endObject();
            put.setEntity(new StringEntity(builder.string(), ContentType.APPLICATION_JSON));
            execute(put);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("put mapping error", e);
        }
    }

    @Override
    public Tuple<String, List<String>> getMapping(String[] indices, String[] types) {
        try {
            String result = null;
            try {
                result = execute(new HttpGet(getRequestUrl(indices, types) + "_mapping"));
            } catch (Exception e) {
                logger.error("get mapping error", e);
            }
            final List<String> indicesReal = new ArrayList<>();
            if (result != null) {
                Map<String, Object> mappingObj = (Map<String, Object>) FasterXmlUtils.fromJson(result, Map.class);
                mappingObj.forEach((index, value) -> indicesReal.add(index));
            }
            return new Tuple<>(result, indicesReal);
        } catch (Exception e) {
            logger.error("get mapping error", e);
            return new Tuple<>("", null);
        }
    }

    @Override
    public Tuple<String, List<ESDoc>> searchByScroll(String[] indices, String[] types, String scrollId) {
        try {
            String result;
            if (StringUtils.isEmpty(scrollId)) {
                XContentBuilder content = XContentFactory.jsonBuilder();
                content.startObject().field("size", this.size).endObject();
                HttpPost post = new HttpPost(getRequestUrl(indices, types) + "_search?scroll=" + this.keepAlive);
                post.setEntity(new StringEntity(content.string(), ContentType.APPLICATION_JSON));
                result = execute(post);
            } else {
                HttpGet get = new HttpGet(getRequestUrl(null, null) + "_search/scroll?scroll=" + this.keepAlive + "&scroll_id=" + scrollId);
                result = execute(get);
            }
            return analyzeResponse(result);
        } catch (Exception e) {
            logger.error("scroll search error", e);
            return new Tuple<>("", null);
        }
    }

    @Override
    public void saveBulk(List<ESDoc> docs) {
        StringBuffer text = new StringBuffer();
        docs.forEach(doc -> {
            try {
                XContentBuilder builder = XContentFactory.jsonBuilder();
                builder.startObject().startObject("index")
                        .field("_index", doc.getIndex())
                        .field("_type", doc.getType())
                        .field("_id", doc.getId())
                        .endObject().endObject();
                text.append(builder.string()).append("\n");
                text.append(FasterXmlUtils.toJson(doc.getSource())).append("\n");
            } catch (IOException e) {
                logger.error("Build json object error, when execute bulk save.", e);
            }
        });
        HttpPost post = new HttpPost(getRequestUrl(null, null) + "_bulk");
        post.setEntity(new StringEntity(text.toString(), ContentType.DEFAULT_TEXT));
        try {
            execute(post);
        } catch (Exception e) {
            logger.error("Bulk save error.", e);
        }
    }

    @Override
    public void close() {

    }

    private Tuple<String, List<ESDoc>> analyzeResponse(String response) {
        List<ESDoc> docs = new ArrayList<ESDoc>();
        Tuple<String, List<ESDoc>> tuple;
        try {
            Map<String, Object> map = FasterXmlUtils.fromJson(response, Map.class);
            Map<String, Object> hitsWrapper = (Map<String, Object>) map.get("hits");
            List<Object> hits = (List<Object>) hitsWrapper.get("hits");
            if (hits != null) {
                for (Object hit : hits) {
                    Map<String, Object> hitObj = (Map<String, Object>) hit;
                    docs.add(new ESDoc((String) hitObj.get("_index"), (String) hitObj.get("_type"), (String) hitObj.get("_id"), (Map<String, Object>) hitObj.get("_source")));
                }
            }
            String scrollId = (String) map.get("_scroll_id");
            tuple = new Tuple<String, List<ESDoc>>(scrollId, docs);
        } catch (Exception e) {
            logger.error("analyze response error :", e);
            tuple = new Tuple<String, List<ESDoc>>("", docs);
        }
        return tuple;
    }

    private String getRequestUrl(String[] indices, String[] types) {
        StringBuffer sb = new StringBuffer("http://").append(this.address).append("/");
        if (indices != null && indices.length > 0) {
            for (int i = 0; i < indices.length; i++) {
                sb.append(indices[i]);
                if (i < (indices.length - 1)) sb.append(",");
            }
            sb.append("/");
            if (types != null && types.length > 0) {
                for (int i = 0; i < types.length; i++) {
                    sb.append(types[i]);
                    if (i < (types.length - 1)) sb.append(",");
                }
                sb.append("/");
            }
        }
        return sb.toString();
    }

    public String execute(HttpUriRequest request) throws Exception {
        logger.debug("execute http request: url = {}", request.getURI().toString());
        String json = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient client = HttpClients.custom().build();
        try {
            response = client.execute(request);
            json = EntityUtils.toString(response.getEntity(), Charset.forName(HTTP.UTF_8));
        } finally {
            if (response != null) response.close();
            if (client != null) client.close();
        }
        logger.debug("response is : {}", json);
        return json;
    }
}
