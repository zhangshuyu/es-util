package com.hansight.es.provider;

import com.hansight.es.channel.TransportChannel;
import com.hansight.es.domain.ESDoc;
import com.hansight.es.domain.EsConfig;
import com.hansight.es.utils.ClassUtil;
import com.hansight.es.utils.DefaultConfig;
import com.hansight.es.utils.FasterXmlUtils;
import org.elasticsearch.common.collect.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ImportProvider
 *
 * @author shuyu
 * @date 2016/6/14
 */
public class ImportProvider implements Provider {

    private static final Logger logger = LoggerFactory.getLogger(ImportProvider.class);

    private TransportChannel channel;

    private List<File> mappingFiles;

    private List<File> dataFiles;

    private EsConfig config;

    private long count = 0;
    private long total = 0;

    public ImportProvider(EsConfig config) {
        logger.info("init start");
        this.config = config;
        this.mappingFiles = new ArrayList<>();
        this.dataFiles = new ArrayList<>();
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("init end");
    }

    private void init() throws Exception {
        this.channel = ClassUtil.newInstance(config.getChannel(),
                new Tuple<Class<?>, Object>(EsConfig.class, config));
        File root = new File(config.getRoot());
        if (!root.exists()) throw new FileNotFoundException(config.getRoot());
        else {
            for (File log : root.listFiles()) {
                if (log.isFile() && log.getName().endsWith(DefaultConfig.FILE_ENDING)) {
                    if (log.getName().startsWith(DefaultConfig.MAPPING_NAME)) mappingFiles.add(log);
                    if (log.getName().startsWith(DefaultConfig.DATA_NAME)) dataFiles.add(log);
                }
            }
        }
    }

    @Override
    public boolean mapping() {
        logger.info("mapping start");

        mappingFiles.forEach(mappingFile -> {
            FileReader mfr = null;
            BufferedReader mbr = null;
            try {
                mfr = new FileReader(mappingFile);
                mbr = new BufferedReader(mfr);
                StringBuffer mappingStr = new StringBuffer();
                String line;
                while ((line = mbr.readLine()) != null) {
                    mappingStr.append(line);
                }
                Map<String, Map<String, Object>> mappingObj = (Map<String, Map<String, Object>>) FasterXmlUtils.fromJson(mappingStr.toString(), Map.class);
                mappingObj.forEach((index, typesWrapper) -> {
                    Map<String, Object> types = (Map<String, Object>) typesWrapper.get("mappings");
                    if (channel.indexExists(index)) {
                        types.forEach((type, source) -> channel.putMapping(index, type, (Map<String, Object>) source));
                    } else {
                        channel.createIndex(index, types);
                    }
                    logger.info("mapping index {}", index);
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    mfr.close();
                    mbr.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        });
        logger.info("mapping end");
        return true;
    }

    @Override
    public void option() {
        logger.info("option start");
        dataFiles.forEach(dataFile -> reader(dataFile));
        logger.info("option end");
    }

    private void reader(File dataFile) {
        FileReader dfr = null;
        BufferedReader dbr = null;
        try {
            dfr = new FileReader(dataFile);
            dbr = new BufferedReader(dfr);
            String line;
            List<ESDoc> storages = new ArrayList<>();
            while ((line = dbr.readLine()) != null) {
                ESDoc storage = FasterXmlUtils.fromJson(line, ESDoc.class);
                storages.add(storage);
                if (storages.size() >= config.getSize()) {
                    channel.saveBulk(storages);
                    storages.clear();
                    count += config.getSize();
                    logger.info("import {} from file {}, count {}", config.getSize(), dataFile.getName(), count);
                }
            }
            if (storages.size() > 0) {
                channel.saveBulk(storages);
                count += storages.size();
                logger.info("import {} from file {}, count {}", storages.size(), dataFile.getName(), count);
                storages.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dfr.close();
                dbr.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            total += count;
            logger.info("import file {} finished, count {}, total {}", dataFile.getName(), count, total);
            count = 0;
        }
    }

    @Override
    public void close() {
        channel.close();
        logger.info("finished, total {}", total);
        count = 0;
        total = 0;
    }
}
