package com.hansight.es;

import com.hansight.es.schema.FieldSchema;
import com.hansight.es.schema.IndexSchema;
import com.hansight.es.schema.TypeSchema;
import com.hansight.es.utils.ESUtils;
import com.hansight.es.utils.FasterXmlUtils;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.transport.TransportClient;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by evan on 2016/12/28.
 */
public class ElasticSearchSchema {
    public static void main(String[] args) {
        fromJson();
    }

    public static void fromJson() {
        FileReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new FileReader(new File("E:\\work\\IdeaProjects\\open\\es-util\\src\\test\\resources\\all.json"));
            bufferedReader = new BufferedReader(reader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            List<IndexSchema> indexSchema = FasterXmlUtils.fromJson(stringBuffer.toString(), List.class, IndexSchema.class);
            excel2(indexSchema);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void fromES() {
        TransportClient client = ESUtils.getClient();
        GetMappingsRequestBuilder mappingsRequestBuilder = client.admin().indices().prepareGetMappings()
                .setIndices("sentiment", "websites","sentiment_2016","sentiment_en_2016","sentiment_cn_2016");
        GetMappingsResponse mappingsResponse = mappingsRequestBuilder.get();
        final Map<String, Map<String, Map<String, String>>> schema = new HashMap<>();
        mappingsResponse.mappings().forEach(indexValue -> {
            schema.put(indexValue.key, new HashMap<>());
            indexValue.value.forEach((value) -> {
                if(!value.key.equals("_default_")){
                    try {
                        System.out.println("======= " + value.key + " =======");
                        schema.get(indexValue.key).put(value.key, new HashMap<>());
                        schema.get(indexValue.key).get(value.key).putAll(print(null, value.value.getSourceAsMap().get("properties")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        client.close();
        System.out.println(FasterXmlUtils.toJson(toSchema(schema)));
        try {
            excel2(toSchema(schema));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<IndexSchema> toSchema(Map<String, Map<String, Map<String, String>>> data) {
        List<IndexSchema> schema = new ArrayList<>();
        data.forEach((index, types) -> {
            IndexSchema indexSchema = new IndexSchema();
            List<TypeSchema> typeSchemaList = new ArrayList<>();
            types.forEach((type, fields) -> {
                TypeSchema typeSchema = new TypeSchema();
                List<FieldSchema> fieldSchemaList = new ArrayList<>();
                fields.forEach((field, fieldType) -> {
                    FieldSchema fieldSchema = new FieldSchema();
                    fieldSchema.setType(fieldType);
                    fieldSchema.setName(field);
                    fieldSchemaList.add(fieldSchema);
                });
                typeSchema.setName(type);
                typeSchema.setFields(fieldSchemaList);
                typeSchemaList.add(typeSchema);
            });
            indexSchema.setName(index);
            indexSchema.setTypes(typeSchemaList);
            schema.add(indexSchema);
        });
        return schema;
    }

    public static void excel2(List<IndexSchema> schema) throws Exception {
        WritableWorkbook excel = Workbook.createWorkbook(new File("E:\\work\\data\\es_schema\\schema2.xls"));
        int sheetNum = 2;
        for (IndexSchema index : schema) {
            WritableSheet sheet = excel.createSheet(index.getName(), sheetNum);
            int row = 0;
            for (TypeSchema type : index.getTypes()) {
                // 表名
                sheet.mergeCells(0, row, 2, row);
                sheet.addCell(new Label(0, row, type.getName() + ":" + type.getDescription()));
                row += 1;

                // 表头
                sheet.addCell(new Label(0, row, "字段名"));
                sheet.addCell(new Label(1, row, "字段类型"));
                sheet.addCell(new Label(2, row, "字段说明"));
                row += 1;

                // 字段
                for (FieldSchema field : type.getFields()) {
                    sheet.addCell(new Label(0, row, field.getName()));
                    sheet.addCell(new Label(1, row, field.getType()));
                    sheet.addCell(new Label(2, row, field.getDescription()));
                    row++;
                }
                row += 1;
            }
            sheetNum++;
        }
        // 写入数据
        excel.write();
        // 关闭文件
        excel.close();
    }

    public static void excel(Map<String, Map<String, Map<String, String>>> schema) throws Exception {
        WritableWorkbook excel = Workbook.createWorkbook(new File("E:\\work\\data\\es_schema\\schema.xls"));
        int sheetNum = 2;
        for (Map.Entry<String, Map<String, Map<String, String>>> index : schema.entrySet()) {
            WritableSheet sheet = excel.createSheet(index.getKey(), sheetNum);
            int row = 0;
            for (Map.Entry<String, Map<String, String>> type : index.getValue().entrySet()) {
                // 表名
                sheet.mergeCells(0, row, 1, row);
                sheet.addCell(new Label(0, row, "Type名称：" + type.getKey()));
                row += 1;

                // 表头
                sheet.addCell(new Label(0, row, "字段名"));
                sheet.addCell(new Label(1, row, "字段类型"));
                sheet.addCell(new Label(2, row, "字段说明"));
                row += 1;

                // 字段
                for (Map.Entry<String, String> field : type.getValue().entrySet()) {
                    sheet.addCell(new Label(0, row, field.getKey()));
                    sheet.addCell(new Label(1, row, field.getValue()));
                    row++;
                }
                row += 1;
            }
            sheetNum++;
        }
        // 写入数据
        excel.write();
        // 关闭文件
        excel.close();
    }

    public static Map<String, String> print(String key, Object object) {
        Map<String, String> schema = new HashMap<>();
        Map<String, Object> obj = (Map<String, Object>) object;
        obj.forEach((field, fieldValue) -> {
            Map<String, Object> fv = (Map<String, Object>) fieldValue;
            String fieldName = field;
            if (StringUtils.isNotEmpty(key)) fieldName = key + "." + field;
            if (fv.get("type") != null) {
                System.out.println(fieldName + "," + fv.get("type"));
                schema.put(fieldName, fv.get("type").toString());
            } else {
                schema.putAll(print(fieldName, fv.get("properties")));
            }
        });
        return schema;
    }
}
