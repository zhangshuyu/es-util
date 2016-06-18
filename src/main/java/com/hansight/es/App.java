package com.hansight.es;

import com.hansight.es.domain.EsConfig;
import com.hansight.es.provider.Provider;
import com.hansight.es.utils.ClassUtil;
import com.hansight.es.utils.ConfigUtils;
import com.hansight.es.utils.DefaultConfig;
import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.collect.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * App
 *
 * @author shuyu
 * @date 2016/6/14
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static EsConfig config;

    public static void main(String[] args) {
        if (command(args) && init()) {
            Provider provider = null;
            try {
                logger.info("use provider: {}, use channel: {}", config.getProviderName(), config.getChannel());
                provider = ClassUtil.newInstance(config.getProviderName(), new Tuple<Class<?>, Object>(EsConfig.class, config));
                int cmdC = config.getCmd().indexOf("c", 0);
                int cmdO = config.getCmd().indexOf("o", 0);
                if (cmdC != -1) provider.config();
                if (cmdO != -1) provider.option();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (provider != null) provider.close();
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

        Option channelOption = new Option("tc", "channel", true, "es transport channel;java or http");
        channelOption.setRequired(false);
        options.addOption(channelOption);

        Option indexOption = new Option("i", "index", true, "es index name");
        indexOption.setRequired(false);
        options.addOption(indexOption);

        Option typeOption = new Option("t", "types", true, "es type names; many use , to split");
        typeOption.setRequired(false);
        options.addOption(typeOption);

        Option sizeOption = new Option("S", "size", true, "task size time, the unit od time is minute");
        sizeOption.setRequired(false);
        options.addOption(sizeOption);

        Option rootPathOption = new Option("r", "root", true, "process root path");
        rootPathOption.setRequired(false);
        options.addOption(rootPathOption);

        Option providerOption = new Option("p", "provider", true, "provider types, import or export");
        providerOption.setRequired(false);
        options.addOption(providerOption);

        Option keepAliveOption = new Option("a", "keepAlive", true, "es scroll id keep alive time");
        keepAliveOption.setRequired(false);
        options.addOption(keepAliveOption);

        Option mappingFileNameOption = new Option("mf", "mappingFile", true, "es mapping file name");
        mappingFileNameOption.setRequired(false);
        options.addOption(mappingFileNameOption);

        Option dataFileNameOption = new Option("df", "dataFile", true, "es data file name");
        dataFileNameOption.setRequired(false);
        options.addOption(dataFileNameOption);

        Option cmdOption = new Option("cmd", "cmd", true, "app cmd. c:config, o:option");
        cmdOption.setRequired(false);
        options.addOption(cmdOption);

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
                    case "size": {
                        config.setSize(Integer.parseInt(opt.getValue()));
                        break;
                    }
                    case "root": {
                        config.setRoot(opt.getValue());
                        break;
                    }
                    case "provider": {
                        config.setProviderName(opt.getValue());
                        break;
                    }
                    case "keepAlive": {
                        config.setScrollAlive(opt.getValue());
                        break;
                    }
                    case "mappingFile": {
                        config.setMappingFileName(opt.getValue());
                        break;
                    }
                    case "dataFile": {
                        config.setDataFileName(opt.getValue());
                        break;
                    }
                    case "channel": {
                        config.setChannel(opt.getValue());
                        break;
                    }
                    case "cmd": {
                        config.setCmd(opt.getValue());
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

    protected static boolean init() {
        try {
            if (config == null) return false;
            if (config.getNodes() == null || config.getNodes().size() == 0)
                config.setNodes(ConfigUtils.getStringListValue(ConfigUtils.ES_CLUSTER_NODES, DefaultConfig.NODES));
            if (config.getIndices() == null || config.getIndices().size() == 0)
                config.setIndices(ConfigUtils.getStringListValue("es.index", DefaultConfig.INDICES));
            if (config.getTypes() == null || config.getTypes().size() == 0)
                config.setTypes(ConfigUtils.getStringListValue("es.types", DefaultConfig.TYPES));
            if (config.getSize() <= 0)
                config.setSize(ConfigUtils.getIntValue("es.bulk.size", DefaultConfig.SIZE));
            if (StringUtils.isEmpty(config.getRoot()))
                config.setRoot(ConfigUtils.getStringValue("app.root", DefaultConfig.ROOT_PATH));
            if (StringUtils.isEmpty(config.getScrollAlive()))
                config.setScrollAlive(ConfigUtils.getStringValue("es.scroll.keepalive", DefaultConfig.KEEP_ALIVE));
            if (StringUtils.isEmpty(config.getMappingFileName()))
                config.setMappingFileName(ConfigUtils.getStringValue("app.mapping.filename", DefaultConfig.MAPPING_NAME));
            if (StringUtils.isEmpty(config.getDataFileName()))
                config.setDataFileName(ConfigUtils.getStringValue("app.data.filename", DefaultConfig.DATA_NAME));
            if (StringUtils.isEmpty(config.getCmd()))
                config.setCmd("co");
            if (StringUtils.isEmpty(config.getProviderName()))
                config.setProviderName(ConfigUtils.getStringValue(ConfigUtils.PROVIDER, DefaultConfig.PROVIDER));
            else {
                switch (config.getProviderName()) {
                    case "import":
                        config.setProviderName("com.hansight.es.provider.ImportProvider");
                        break;
                    case "export":
                        config.setProviderName("com.hansight.es.provider.ExportProvider");
                        break;
                    default:
                        return false;
                }
            }
            if (StringUtils.isEmpty(config.getChannel()))
                config.setChannel(ConfigUtils.getStringValue(ConfigUtils.CHANNEL, DefaultConfig.CHANNEL));
            else {
                switch (config.getChannel()) {
                    case "java":
                        config.setChannel("com.hansight.es.channel.JavaTransportChannel");
                        break;
                    case "http":
                        config.setChannel("com.hansight.es.channel.HttpTransportChannel");
                        break;
                    default:
                        return false;
                }
            }
            if (config.getSettingsIncludedFields() == null || config.getSettingsIncludedFields().size() == 0)
                config.setSettingsIncludedFields(ConfigUtils.getStringListValue("es.settings.included.fields", DefaultConfig.ES_SETTINGS_INCLUDED_FIELDS));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
