package com.hansight.es.provider;

import com.hansight.es.channel.TransportChannel;
import com.hansight.es.domain.EsConfig;
import com.hansight.es.utils.ClassUtil;
import com.hansight.es.utils.ESUtils;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.Tuple;

import java.util.ArrayList;
import java.util.List;

/**
 * DefaultProvider
 *
 * @author shuyu
 * @date 2016/6/17
 */
public class ProviderImpl implements Provider {

    private EsConfig config;
    private long count = 0;
    private long total = 0;
    private List<String> indices;
    private TransportChannel channel;

    public ProviderImpl(EsConfig config) {
        this.config = config;
        this.indices = new ArrayList<>();
        try {
            this.channel = ClassUtil.newInstance(config.getChannel(),
                    new Tuple<Class<?>, Object>(TransportClient.class, ESUtils.getNewClient(config.getClusterName(), config.getNodes())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        init();
    }

    public void init(){

    }

    @Override
    public boolean config() {
        return false;
    }

    @Override
    public void option() {

    }

    @Override
    public void close() {

    }
}
