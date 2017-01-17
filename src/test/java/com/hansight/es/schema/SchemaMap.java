package com.hansight.es.schema;

import com.hansight.es.utils.FasterXmlUtils;

import java.util.Map;

/**
 * Created by evan on 2016/12/28.
 */
public class SchemaMap {
    private static String sentiment = "{\n" +
            "\t\"sentiment\": {\n" +
            "\t\t\"country_classify\": {\n" +
            "\t\t\t\"parent\": \"string\",\n" +
            "\t\t\t\"code\": \"string\",\n" +
            "\t\t\t\"value\": \"string\"\n" +
            "\t\t},\n" +
            "\t\t\"dictionary\": {\n" +
            "\t\t\t\"similarity_cn\": \"string\",\n" +
            "\t\t\t\"en\": \"string\",\n" +
            "\t\t\t\"similarity_en\": \"string\",\n" +
            "\t\t\t\"cn\": \"string\"\n" +
            "\t\t},\n" +
            "\t\t\"system\": {\n" +
            "\t\t\t\"unit\": \"string\",\n" +
            "\t\t\t\"name\": \"string\",\n" +
            "\t\t\t\"modified\": \"boolean\",\n" +
            "\t\t\t\"id\": \"string\",\n" +
            "\t\t\t\"value\": \"string\",\n" +
            "\t\t\t\"group\": \"string\",\n" +
            "\t\t\t\"order\": \"long\"\n" +
            "\t\t},\n" +
            "\t\t\"recommend_keywords\": {\n" +
            "\t\t\t\"id\": \"string\",\n" +
            "\t\t\t\"keyword\": \"string\",\n" +
            "\t\t\t\"ref_times\": \"string\",\n" +
            "\t\t\t\"timestamp\": \"string\"\n" +
            "\t\t},\n" +
            "\t\t\"doc_classify\": {\n" +
            "\t\t\t\"parent\": \"string\",\n" +
            "\t\t\t\"code\": \"string\",\n" +
            "\t\t\t\"value\": \"string\"\n" +
            "\t\t},\n" +
            "\t\t\"technology\": {\n" +
            "\t\t\t\"parent\": \"string\",\n" +
            "\t\t\t\"code\": \"string\",\n" +
            "\t\t\t\"keyword\": \"string\",\n" +
            "\t\t\t\"value\": \"string\"\n" +
            "\t\t},\n" +
            "\t\t\"crawl_source\": {\n" +
            "\t\t\t\"country\": \"string\",\n" +
            "\t\t\t\"period\": \"long\",\n" +
            "\t\t\t\"src\": \"string\",\n" +
            "\t\t\t\"docType\": \"string\",\n" +
            "\t\t\t\"deNoising\": \"boolean\",\n" +
            "\t\t\t\"description\": \"string\",\n" +
            "\t\t\t\"language\": \"string\",\n" +
            "\t\t\t\"subscription\": \"string\",\n" +
            "\t\t\t\"valid\": \"boolean\",\n" +
            "\t\t\t\"depth\": \"long\",\n" +
            "\t\t\t\"kafka\": \"boolean\",\n" +
            "\t\t\t\"id\": \"string\",\n" +
            "\t\t\t\"sourceName\": \"string\",\n" +
            "\t\t\t\"ts\": \"date\"\n" +
            "\t\t},\n" +
            "\t\t\"crawl\": {\n" +
            "\t\t\t\"model.latest_ts\": \"date\",\n" +
            "\t\t\t\"model.language\": \"string\",\n" +
            "\t\t\t\"model.keywords.rule.total_frequency\": \"long\",\n" +
            "\t\t\t\"model.category.technical.frequency\": \"long\",\n" +
            "\t\t\t\"model.keywords.keyword\": \"string\",\n" +
            "\t\t\t\"model.first_ts\": \"date\",\n" +
            "\t\t\t\"model.from.sender\": \"string\",\n" +
            "\t\t\t\"action\": \"string\",\n" +
            "\t\t\t\"model.type\": \"string\",\n" +
            "\t\t\t\"model.period\": \"string\",\n" +
            "\t\t\t\"model.account\": \"string\",\n" +
            "\t\t\t\"model.keywords.rule.min_keyword\": \"long\",\n" +
            "\t\t\t\"model.depth\": \"long\",\n" +
            "\t\t\t\"model.category.document\": \"string\",\n" +
            "\t\t\t\"model.from.category.nation\": \"string\",\n" +
            "\t\t\t\"model.source\": \"string\",\n" +
            "\t\t\t\"model.from.language\": \"string\",\n" +
            "\t\t\t\"model.latest_visit\": \"string\",\n" +
            "\t\t\t\"model.from.category.technical.frequency\": \"long\",\n" +
            "\t\t\t\"dodry\": \"boolean\",\n" +
            "\t\t\t\"model.category.nation\": \"string\",\n" +
            "\t\t\t\"guid\": \"string\",\n" +
            "\t\t\t\"model.minword\": \"long\",\n" +
            "\t\t\t\"model.from.category.document\": \"string\",\n" +
            "\t\t\t\"model.from.category.technical.keyword\": \"string\",\n" +
            "\t\t\t\"model.publicaccount\": \"string\",\n" +
            "\t\t\t\"model.url\": \"string\",\n" +
            "\t\t\t\"model.category.technical.keyword\": \"string\",\n" +
            "\t\t\t\"ts\": \"date\"\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"websites\": {\n" +
            "\t\t\"website\": {\n" +
            "\t\t\t\"id\": \"string\",\n" +
            "\t\t\t\"source\": \"string\",\n" +
            "\t\t\t\"create_timestamp\": \"date\",\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"url\": \"string\"\n" +
            "\t\t}\n" +
            "\t}\n" +
            "}";

    public static Map<String,Map<String,Map<String,String>>> getSentiment(){
        return FasterXmlUtils.fromJson(sentiment, Map.class);
    }

    public static void main(String[] args) {
        System.out.println(FasterXmlUtils.toJson(SchemaMap.getSentiment().get("websites").get("website").get("id")));
    }
}
