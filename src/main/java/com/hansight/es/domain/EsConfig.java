package com.hansight.es.domain;

import java.util.List;

/**
 * ProviderConfig
 *
 * @author shuyu
 * @date 2016/6/16
 */
public class EsConfig {
    private String clusterName;
    private List<String> nodes;
    private List<String> indices;
    private List<String> types;
    private int size;
    private int port;
    private String root;
    private String providerName;
    private String scrollAlive;
    private String mappingFileName;
    private String dataFileName;
    private String channel;
    private List<String> settingsIncludedFields;
    private String cmd;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public List<String> getIndices() {
        return indices;
    }

    public void setIndices(List<String> indices) {
        this.indices = indices;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getScrollAlive() {
        return scrollAlive;
    }

    public void setScrollAlive(String scrollAlive) {
        this.scrollAlive = scrollAlive;
    }

    public String getMappingFileName() {
        return mappingFileName;
    }

    public void setMappingFileName(String mappingFileName) {
        this.mappingFileName = mappingFileName;
    }

    public String getDataFileName() {
        return dataFileName;
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public List<String> getSettingsIncludedFields() {
        return settingsIncludedFields;
    }

    public void setSettingsIncludedFields(List<String> settingsIncludedFields) {
        this.settingsIncludedFields = settingsIncludedFields;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
}
