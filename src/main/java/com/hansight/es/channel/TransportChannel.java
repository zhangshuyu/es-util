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
    /**
     * create index
     *
     * @param index    index name
     * @param settings index settings info
     * @param mappings index mappings fields
     */
    void createIndex(String index, Map<String, Object> settings, Map<String, Object> mappings);

    /**
     * put mapping
     *
     * @param index  index name
     * @param type   type name
     * @param source mapping body
     */
    void putMapping(String index, String type, Map<String, Object> source);

    /**
     * get mapping info
     *
     * @param indices index names
     * @param types   type names
     * @return
     */
    Map<String, Object> getMapping(String[] indices, String[] types);

    /**
     * search docs by scroll api
     *
     * @param indices  index names
     * @param types    type names
     * @param scrollId scroll id
     * @return
     */
    Tuple<String, List<ESDoc>> searchByScroll(String[] indices, String[] types, String scrollId);

    /**
     * index docs
     *
     * @param docs
     */
    void saveBulk(List<ESDoc> docs);

    /**
     * check index exist or not
     *
     * @param index
     * @return
     */
    boolean indexExists(String index);

    /**
     * close connection
     */
    void close();

    /**
     * close index
     *
     * @param index
     * @return
     */
    boolean closeIndex(String index);

    /**
     * open index
     *
     * @param index
     * @return
     */
    boolean openIndex(String index);

    /**
     * put index settings
     *
     * @param indices
     * @return Map<String, Object> {(index, setting obj)}
     */
    Map<String, Object> getSettings(String... indices);

}
