package com.hansight.es.channel;

import com.hansight.es.domain.ESDoc;
import org.elasticsearch.common.collect.Tuple;

import java.util.List;
import java.util.Map;

/**
 * TransportChannelImpl
 *
 * @author shuyu
 * @date 2016/6/17
 */
public class TransportChannelImpl implements TransportChannel {
    private String cluster;
    private List<String> nodes;

    public TransportChannelImpl(String cluster, List<String> nodes) {
        this.cluster = cluster;
        this.nodes = nodes;
    }

    @Override
    public void createIndex(String index, Map<String, Object> mappings) {

    }

    @Override
    public void putMapping(String index, String type, Map<String, Object> source) {

    }

    @Override
    public Tuple<String, List<String>> getMapping(String[] indices, String[] types) {
        return null;
    }

    @Override
    public Tuple<String, List<ESDoc>> searchByScroll(String[] indices, String[] types, String scrollId) {
        return null;
    }

    @Override
    public void saveBulk(List<ESDoc> docs) {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean indexExists(String index) {
        return false;
    }
}
