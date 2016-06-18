package com.hansight.es.utils;

import com.typesafe.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by zhangshuyu on 2016/4/20.
 */
public class ConfigUtils {
    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
    private static Config config;
    public static String ES_CLUSTER_NAME = "es.cluster.name";
    public static String ES_CLUSTER_NODES = "es.cluster.nodes";
    public static String ES_BATCH_SIZE = "es.bulk.size";
    public static String SYS_DES_DEFAULT_KEY = "sys.des.default.key";
    public static final String PROVIDER = "app.provider";
    public static final String CHANNEL = "app.channel";


    static {
        try {
            config = ConfigFactory.load("config");
        } catch (Exception e) {
            logger.error("load config error", e);
        }
    }

    public static String getStringValue(String key) {
        try {
            return config.getString(key);
        } catch (Exception e) {
            logger.error("unknown key ", e);
            return null;
        }
    }

    public static String getStringValue(String key, String defaultValue) {
        try {
            return config.getString(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int getIntValue(String key) {
        try {
            return config.getInt(key);
        } catch (Exception e) {
            logger.error("unknown key ", e);
            return 0;
        }
    }

    public static int getIntValue(String key, int defaultValue) {
        try {
            return config.getInt(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static long getLongValue(String key) {
        try {
            return config.getLong(key);
        } catch (Exception e) {
            logger.error("unknown key ", e);
            return 0;
        }
    }

    public static long getLongValue(String key, long defaultValue) {
        try {
            return config.getLong(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static List<Object> getListValue(String key) {
        try {
            return config.getList(key).unwrapped();
        } catch (Exception e) {
            logger.error("unknown key ", e);
            return null;
        }
    }

    public static List<Object> getListValue(String key, List<Object> defaultValue) {
        try {
            return config.getList(key).unwrapped();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static List<String> getStringListValue(String key) {
        try {
            return config.getStringList(key);
        } catch (Exception e) {
            logger.error("unknown key ", e);
            return null;
        }
    }

    public static List<String> getStringListValue(String key, List<String> defaultValue) {
        try {
            return config.getStringList(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getPasswordValue(String key) {
        try {
            return DesUtils.getInstance().decrypt(getStringValue(key));
        } catch (Exception e) {
            logger.error("get password value error", e);
            return "";
        }
    }

    public static String getPasswordValue(String key, String defaultValue) {
        try {
            return DesUtils.getInstance().decrypt(getStringValue(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
