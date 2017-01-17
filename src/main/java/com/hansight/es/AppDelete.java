package com.hansight.es;

import com.hansight.es.domain.ESDoc;
import com.hansight.es.domain.EsConfig;
import com.hansight.es.utils.ESUtils;
import com.hansight.es.utils.FasterXmlUtils;
import org.apache.commons.cli.*;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by evan on 2016/9/28.
 */
public class AppDelete {
    private static final Logger logger = LoggerFactory.getLogger(AppDelete.class);
    private static EsConfig config;

    private static TransportClient client = null;
    private static FileWriter dfw = null;
    private static BufferedWriter dbw = null;

    private static String backupPath;

    public static void main(String[] args) {

        if (command(args)) {
            try {
                client = ESUtils.getNewClient(config.getClusterName(), config.getNodes());
                String[] indices = config.getIndices().toArray(new String[]{});
                String[] types = config.getTypes().toArray(new String[]{});
                SearchResponse response = client.prepareSearch(indices)
                        .setTypes(types)
                        .setScroll("1m")
                        .setSize(100)
                        .get("5m");
                String scrollId = response.getScrollId();
                dfw = new FileWriter(new File(backupPath + "/data_backup.log"));
                dbw = new BufferedWriter(dfw);
                write(response.getHits().hits());
                while (true) {
                    response = client.prepareSearchScroll(scrollId).setScroll("1m").get("5m");
                    scrollId = response.getScrollId();
                    System.out.println(response.getHits().hits().length);
                    if (response.getHits().hits().length == 0) break;
                    else {
                        write(response.getHits().hits());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (client != null) client.close();
                try {
                    dbw.close();
                    dfw.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        } else logger.error("start failed");
    }

    protected static boolean command(String[] args) {
        if (config == null) config = new EsConfig();
        Options options = new Options();
        Option clusterOption = new Option("c", "cluster", true, "es cluster name");
        clusterOption.setRequired(false);
        options.addOption(clusterOption);

        Option nodesOption = new Option("n", "nodes", true, "es cluster nodes address; many use , to split");
        nodesOption.setRequired(false);
        options.addOption(nodesOption);

        Option indexOption = new Option("i", "index", true, "es index name");
        indexOption.setRequired(false);
        options.addOption(indexOption);

        Option typeOption = new Option("t", "types", true, "es type names; many use , to split");
        typeOption.setRequired(false);
        options.addOption(typeOption);

        Option backupPathOption = new Option("bp", "backupPath", true, "es data backup path, eg: /opt/cspec/data/backup");
        backupPathOption.setRequired(false);
        options.addOption(backupPathOption);

        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse(options, args);
            Option[] opts = commandLine.getOptions();
            for (Option opt : opts) {
                switch (opt.getLongOpt()) {
                    case "cluster": {
                        config.setClusterName(opt.getValue());
                        break;
                    }
                    case "nodes": {
                        String value = opt.getValue();
                        List<String> nodes = new ArrayList<>();
                        if (value != null && value.length() != 0) {
                            nodes = new ArrayList<>();
                            for (String node : value.split(",")) {
                                nodes.add(node);
                            }
                        }
                        config.setNodes(nodes);
                        break;
                    }
                    case "index": {
                        String value = opt.getValue();
                        List<String> indices = new ArrayList<>();
                        if (value != null && value.length() != 0) {
                            indices = new ArrayList<>();
                            for (String index : value.split(",")) {
                                indices.add(index);
                            }
                        }
                        config.setIndices(indices);
                        break;
                    }
                    case "types": {
                        String value = opt.getValue();
                        List<String> types = new ArrayList<>();
                        if (value != null && value.length() != 0) {
                            types = new ArrayList<>();
                            for (String type : value.split(",")) {
                                types.add(type);
                            }
                        }
                        config.setTypes(types);
                        break;
                    }
                    case "backupPath": {
                        backupPath = opt.getValue();
                        break;
                    }
                }
            }
            return true;
        } catch (ParseException e) {
            logger.error("command line parameter missing", e);
            hf.printHelp(App.class.getClass().getSimpleName(), options, true);
        }
        return false;
    }

    private static void write(SearchHit[] hits) {
        try {
            for (SearchHit hit : hits) {
                dbw.write(FasterXmlUtils.toJson(new ESDoc(hit.getIndex(), hit.getType(), hit.getId(), hit.getSource())));
                dbw.newLine();
                client.prepareDelete(hit.getIndex(), hit.getType(), hit.getId()).get("1m");
            }
            if (hits != null && hits.length > 0) dbw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
