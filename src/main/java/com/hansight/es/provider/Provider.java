package com.hansight.es.provider;

/**
 * Created by zhangshuyu on 2016/6/14.
 */
public interface Provider {
    boolean mapping();
    void option();
    void close();
}
