package com.hansight.es;

import com.hansight.es.domain.ESDoc;
import com.hansight.es.utils.ESUtils;
import com.hansight.es.utils.FasterXmlUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.transport.TransportClient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test
 *
 * @author shuyu
 * @date 2016/6/14
 */
public class Test {

    public static void main(String[] args) {
//        checkDir("G:/data/es/sentiment_cn_2017/data.log");
//        json();
    }

    private  void mappingTest() {
        TransportClient client = ESUtils.getClient();
        try {
            GetMappingsResponse mappingsResponse = client.admin().indices().prepareGetMappings().addIndices("sentiment_cn_2016").get();
            final Map<String, Map<String, Object>> mappingObj = new HashMap<>();
            mappingsResponse.mappings().get("sentiment_cn_2016").forEach((value) -> {
                try {
                    mappingObj.put(value.key, value.value.getSourceAsMap());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            String mapping = FasterXmlUtils.toJson(mappingObj);


            System.out.println(mapping);
            Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) FasterXmlUtils.fromJson(mapping, Map.class);
//            if (ESUtils.indicesExists("sentiment_cn_2017")) {
//                map.forEach((type, source) -> client.admin().indices().preparePutMapping("sentiment_cn_2017").setType(type).setSource(source).get());
//            } else {
//                CreateIndexRequestBuilder builder = client.admin().indices().prepareCreate("sentiment_cn_2017");
//                map.forEach((type, source) -> builder.addMapping(type, source));
//                builder.get();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.close();
    }

    public  void checkDir(String path) {
        File file = new File(path);
        System.out.println(file.exists());
        file.mkdirs();
        System.out.println(file.isDirectory());
        System.out.println(file.isFile());
    }

    @org.junit.Test
    public void json() {
        String json = "{\"index\":\"sentiment\",\"type\":\"dictionary\",\"id\":\"45477c036043421089c2ebe0e08f47bc\",\"source\":{\"en\":\"Internet security\",\"cn\":\"网络安全\"}}";
        ESDoc doc = FasterXmlUtils.fromJson(json, ESDoc.class);

        System.out.println(doc);
    }
}
