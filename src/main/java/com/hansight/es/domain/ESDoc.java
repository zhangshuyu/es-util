package com.hansight.es.domain;

import java.util.Map;

/**
 * ESDoc
 *
 * @author shuyu
 * @date 2016/6/16
 */
public class ESDoc {
    private String index;
    private String type;
    private String id;
    private Map<String, Object> source;

    public ESDoc() {
    }

    public ESDoc(String index, String type, String id, Map<String, Object> source) {
        this.index = index;
        this.type = type;
        this.id = id;
        this.source = source;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getSource() {
        return source;
    }

    public void setSource(Map<String, Object> source) {
        this.source = source;
    }
}
