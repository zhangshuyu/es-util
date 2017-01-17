package com.hansight.es.schema;

import java.util.List;
import java.util.Map;

/**
 * Created by evan on 2016/12/28.
 */
public class TypeSchema {
    private String name;
    private String description;
    private List<FieldSchema> fields;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFields(List<FieldSchema> fields) {
        this.fields = fields;
    }

    public List<FieldSchema> getFields() {
        return fields;
    }
}
