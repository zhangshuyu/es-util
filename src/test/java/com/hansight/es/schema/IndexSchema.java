package com.hansight.es.schema;

import java.util.List;
import java.util.Map;

/**
 * Created by evan on 2016/12/28.
 */
public class IndexSchema {
    private String description;
    private String name;
    private List<TypeSchema> types;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TypeSchema> getTypes() {
        return types;
    }

    public void setTypes(List<TypeSchema> types) {
        this.types = types;
    }
}
