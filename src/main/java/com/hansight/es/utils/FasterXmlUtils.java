package com.hansight.es.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FasterXmlUtils
 *
 * @author shuyu
 * @date 2016/4/21
 */
public class FasterXmlUtils {

    private static Logger LOG = LoggerFactory.getLogger(FasterXmlUtils.class);

    public static String toJson(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json;
        try {
            json = objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            LOG.error("JSON转换错误", e);
            json = "";
        }
        return json;
    }

    /**
     * 如果字段的值为null或者""时，该字段不被转换到json字符串中
     *
     * @param object
     * @return
     */
    public static String toNotEmptyJson(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json;
        try {
            json = objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            LOG.error("JSON转换错误", e);
            json = "";
        }
        return json;
    }

    public static <T> T fromJson(String json, Class<T> valueType) {
        T result = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            result = objectMapper.readValue(json, valueType);
        } catch (Exception ex) {
            LOG.error("JSON 反序列化错误:[" + json + "]", ex);
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    public static <T> T fromJson(String json, Class collectionClass, Class<?>... elementClasses) {
        T result = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            result = objectMapper.readValue(json, fromJson2JavaType(collectionClass, elementClasses));
        } catch (Exception ex) {
            LOG.error("JSON 反序列化错误", ex);
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    public static JavaType fromJson2JavaType(Class collectionClass, Class<?>... elementClasses) {
        JavaType result = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            result = objectMapper.getTypeFactory().constructParametricType( collectionClass, elementClasses);
        } catch (Exception ex) {
            LOG.error("JSON 反序列化错误,获取JavaType错误", ex);
        }
        return result;
    }
}
