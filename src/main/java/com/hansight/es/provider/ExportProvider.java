package com.hansight.es.provider;

import com.hansight.es.channel.TransportChannel;
import com.hansight.es.domain.ESDoc;
import com.hansight.es.domain.EsConfig;
import com.hansight.es.utils.ClassUtil;
import com.hansight.es.utils.DefaultConfig;
import com.hansight.es.utils.FasterXmlUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.collect.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * ExportProvider
 *
 * @author shuyu
 * @date 2016/6/14
 */
public class ExportProvider implements Provider {
    private static final Logger logger = LoggerFactory.getLogger(ExportProvider.class);

    private EsConfig config;
    private long count = 0;
    private long total = 0;
    private Set<String> indices;
    private TransportChannel channel;

    public ExportProvider(EsConfig config) {
        this.config = config;
        this.indices = new HashSet<>();
        init();
    }

    private void init() {
        try {
            logger.info("init start");
            this.channel = ClassUtil.newInstance(config.getChannel(),
                    new Tuple<Class<?>, Object>(EsConfig.class, config));
            File file = new File(config.getRoot());
            if (!file.exists()) file.mkdirs();
            logger.info("init end");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get channel instance error", e);
        }
    }

    @Override
    public boolean config() {
        logger.info("config start");
        String[] _indices = null;
        String[] _types = null;
        if (config.getIndices() != null && config.getIndices().size() > 0)
            _indices = config.getIndices().toArray(new String[]{});
        if (config.getTypes() != null && config.getTypes().size() > 0)
            _types = config.getTypes().toArray(new String[]{});

        Map<String, Object> configs = new HashMap<>();

        Map<String, Object> settings = channel.getSettings(_indices);
        Map<String, Object> mappings = channel.getMapping(_indices, _types);
        indices.addAll(settings.keySet());
        indices.addAll(mappings.keySet());

        indices.forEach(index -> {
            Map<String, Object> params = new HashMap<String, Object>();
            if (settings.get(index) != null) params.putAll((Map<String, Object>) settings.get(index));
            if (mappings.get(index) != null) params.putAll((Map<String, Object>) mappings.get(index));
            configs.put(index,params);
        });

        FileWriter mfw = null;
        BufferedWriter mbw = null;
        try {
            mfw = new FileWriter(new File(getFilePath("", true)));
            mbw = new BufferedWriter(mfw);
            mbw.write(FasterXmlUtils.toJson(configs));
            mbw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                mbw.close();
                mfw.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }

        logger.info("config end");
        return true;
    }

    @Override
    public void option() {
        logger.info("option start");
        if (indices.size() == 0) {
            writer("");
            total += count;
        } else indices.forEach(index -> {
            writer(index);
            total += count;
            logger.info("export index {} finished, count {}, total {}", index, count, total);
            count = 0;
        });
        logger.info("option end");
    }

    private void writer(String index) {
        FileWriter dfw = null;
        BufferedWriter dbw = null;
        try {
            String[] _indices = null;
            String[] _types = null;
            String scrollID = "";
            if (StringUtils.isNotEmpty(index)) {
                _indices = new String[]{index};
            } else {
                if (config.getIndices() != null && config.getIndices().size() > 0)
                    _indices = config.getIndices().toArray(new String[]{});
            }
            dfw = new FileWriter(new File(getFilePath(index, false)));
            dbw = new BufferedWriter(dfw);
            if (config.getTypes() != null && config.getTypes().size() > 0)
                _types = config.getTypes().toArray(new String[]{});
            Tuple<String, List<ESDoc>> tuple = channel.searchByScroll(_indices, _types, scrollID);

            if (StringUtils.isNotEmpty(tuple.v1()) && tuple.v2() != null && tuple.v2().size() > 0) {
                scrollID = tuple.v1();
                write(dbw, tuple.v2(), index);
                while (true) {
                    tuple = channel.searchByScroll(_indices, _types, scrollID);
                    if (StringUtils.isNotEmpty(tuple.v1()) && tuple.v2() != null && tuple.v2().size() > 0) {
                        scrollID = tuple.v1();
                        write(dbw, tuple.v2(), index);
                    } else break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dbw.close();
                dfw.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    private void write(BufferedWriter writer, List<ESDoc> docs, String index) {
        try {
            int per = docs.size();
            count += per;
            logger.info("export {} from index {}, count {}", per, index, count);
            for (ESDoc doc : docs) {
                writer.write(FasterXmlUtils.toJson(doc));
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        channel.close();
        logger.info("finished, total {}", total);
        count = 0;
        total = 0;
    }

    private String getFilePath(String ending, boolean isMapping) {
        StringBuffer sb = new StringBuffer();
        sb.append(config.getRoot()).append(File.separator);
        if (isMapping) sb.append(config.getMappingFileName());
        else sb.append(config.getDataFileName());
        if (StringUtils.isEmpty(ending)) sb.append("all");
        else sb.append(ending);
        return sb.append(DefaultConfig.FILE_ENDING).toString();
    }
}
