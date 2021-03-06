package com.hansight.es.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * DefaultConfig
 *
 * @author shuyu
 * @date 2016/6/14
 */
public class DefaultConfig {
    public static final int SIZE = 1000;
    public static final List<String> NODES;
    public static final List<String> INDICES;
    public static final List<String> TYPES;
    public static final String ROOT_PATH = "./data/";
    public static final String DATA_NAME = "data_";
    public static final String MAPPING_NAME = "mapping_";
    public static final String KEEP_ALIVE = "1m";
    public static final String PROVIDER = "com.hansight.es.provider.ExportProvider";
    public static final String CHANNEL = "com.hansight.es.channel.JavaTransportChannel";
    public static final String FILE_ENDING = ".log";
    public static final List<String> ES_SETTINGS_INCLUDED_FIELDS;

    static {
        ES_SETTINGS_INCLUDED_FIELDS = new ArrayList<>();
        ES_SETTINGS_INCLUDED_FIELDS.add("index.analysis*");
        NODES = new ArrayList<>();
        INDICES = new ArrayList<>();
        TYPES = new ArrayList<>();
    }
}
