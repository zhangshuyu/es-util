package com.hansight.es;

import com.hansight.es.channel.TransportChannel;
import com.hansight.es.domain.EsConfig;
import com.hansight.es.utils.ClassUtil;
import com.hansight.es.utils.DefaultConfig;
import com.hansight.es.utils.FasterXmlUtils;
import org.elasticsearch.common.collect.Tuple;
import org.junit.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TransportChannelTest
 *
 * @author shuyu
 * @date 2016/6/18
 */
public class TransportChannelTest {

    private EsConfig config;
    private TransportChannel channel;

    @Before
    public void init() {
        config = new EsConfig();
        config.setClusterName("hansight");
        List<String> nodes = new ArrayList<>();
        nodes.add("172.16.219.231:9200");
        config.setNodes(nodes);

        config.setSettingsIncludedFields(DefaultConfig.ES_SETTINGS_INCLUDED_FIELDS);

        try {
            channel = ClassUtil.newInstance("com.hansight.es.channel.HttpTransportChannel", new Tuple<Class<?>, Object>(EsConfig.class, config));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void closeIndexTest() {
        System.out.println(channel.closeIndex("graph_2006"));
    }

    @Test
    public void openIndex() {
        System.out.println(channel.openIndex("graph_2006"));
    }

    @Test
    public void getSettingsTest() {
//        channel.createIndex("test", null, null);
        System.out.println(FasterXmlUtils.toJson(channel.getSettings("test")));
    }

    @After
    public void after() {
        channel.close();
    }
}
