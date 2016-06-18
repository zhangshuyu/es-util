package com.hansight.es.utils;

import org.elasticsearch.common.collect.Tuple;

import java.lang.reflect.Constructor;

/**
 * ClassUtil
 *
 * @author shuyu
 * @date 2016/6/17
 */
public class ClassUtil {
    public static <T> T newInstance(String classname, Tuple<Class<?>, Object>... parameterTuple) throws Exception {
        Class<?>[] parameterTypes = new Class[parameterTuple.length];
        Object[] parameters = new Object[parameterTuple.length];
        for (int i = 0; i < parameterTuple.length; i++) {
            parameterTypes[i] = parameterTuple[i].v1();
            parameters[i] = parameterTuple[i].v2();
        }
        Constructor<?> constructor = Class.forName(classname).getConstructor(parameterTypes);
        @SuppressWarnings("unchecked")
        T inst = (T) constructor.newInstance(parameters);
        return inst;
    }
}
