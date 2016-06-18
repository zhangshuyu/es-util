package com.hansight.es.channel;

import com.hansight.es.domain.ESDoc;
import org.elasticsearch.common.collect.Tuple;

import java.util.List;
import java.util.Map;

/**
 * ESClient
 *
 * @author shuyu
 * @date 2016/6/16
 */
public interface TransportChannel {
    void createIndex(String index, Map<String, Object> mappings);

    void putMapping(String index, String type, Map<String, Object> source);

    Tuple<String, List<String>> getMapping(String[] indices, String[] types);

    Tuple<String, List<ESDoc>> searchByScroll(String[] indices, String[] types, String scrollId);

    void saveBulk(List<ESDoc> docs);

    boolean indexExists(String index);

    void close();

    public static TransportChannelImpl newInstance(String cluster, List<String> nodes) {
        return new TransportChannelImpl(cluster, nodes);
    }
}
