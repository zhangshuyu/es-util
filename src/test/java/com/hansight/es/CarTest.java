package com.hansight.es;

import com.hansight.es.domain.ESDoc;
import com.hansight.es.utils.ESUtils;
import com.hansight.es.utils.FasterXmlUtils;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by evan on 2016/8/10.
 */
public class CarTest {
    private static String[] dateFields = {"latest_history_update_time", "first_sale_consult_finish_time", "fail_sale_time", "last_sale_consult_finish_time", "change_to_lead_date", "first_sale_consult_appointment_time", "last_follow_up_time", "lead_allocation_time", "first_test_drive_finish_time", "first_test_drive_appointment_time", "consult_date", "test_drive_date", "first_out_time", "last_out_time", "fail_sale_lead_activate_date", "test_test_drive_finish_time", "make_invoice_time", "buy_car_date", "pay_car_date",
            "status_date", "update_date", "follow_up_date", "lead_create_date", "quote_date", "sleep_lead_alert_date", "sleep_lead_out_time_date"};

    private static String[][] timeFields = {{"update_date", "update_time"}, {"follow_up_date", "follow_up_time"}, {"lead_create_date", "lead_create_time"}};


    public static void main(String[] args) throws Exception {
//        ESUtils.createIndexWithDefaultMappingCN("car");
        car();
//        StringBuffer sb = new StringBuffer();
//        for (int i = 0; i < dateFields.length; i++) {
////            "\"date\": {\"type\":   \"date\",\"format\": \"yyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis\"}"
////            dateFields.length
//                    sb.append("\"").append(dateFields[i]).append("\": {\"type\":   \"date\",\"format\": \"yyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis\"},");
//        }
//        System.out.println(sb.toString());
    }

    public static void car() throws Exception {
        List<String> nodes = new ArrayList<>();
        nodes.add("10.1.3.100:9300");
        client = ESUtils.getNewClient("sa_cspec_cluster", nodes);


        //创建一个list 用来存储读取的内容
        List list = new ArrayList();
        Workbook rwb = null;
        Cell cell = null;
        InputStream stream = new FileInputStream("K:\\项目分类\\汽车\\Raw Data in Test Case.xls");
        //获取Excel文件对象
        rwb = Workbook.getWorkbook(stream);

        //获取文件的指定工作表 默认的第一个
        Sheet sheet = rwb.getSheet(0);
        //行数(表头的目录不需要，从1开始)
        System.out.println("sheet.getRows()=" + sheet.getRows());

        String[] fields = new String[129];
        ArrayList<ESDoc> docs = new ArrayList<>();

        for (int i = 0; i < sheet.getRows(); i++) {
            if (i <= 1) {
                System.out.println("sheet.getColumns()=" + sheet.getColumns());
                for (int j = 0; j < 129; j++) {
                    //获取第i行，第j列的值
                    cell = sheet.getCell(j, i);
                    fields[j] = cell.getContents();
                }
                System.out.println(FasterXmlUtils.toJson(fields));
            } else {
                Map<String, Object> rowData = new HashMap<>();
                for (int j = 0; j < sheet.getColumns(); j++) {
                    //获取第i行，第j列的值
                    cell = sheet.getCell(j, i);
                    if ("".equals(cell.getContents().trim()))
                        rowData.put(fields[j], null);
                    else rowData.put(fields[j], cell.getContents().trim());
                    if (fields[j].equals("status_date")) System.out.println(cell.getContents());
                }
                for (int j = 0; j < timeFields.length; j++) {
                    if (rowData.get(timeFields[j][0]) != null && rowData.get(timeFields[j][1]) != null && !"".equals(rowData.get(timeFields[j][0])) && !"".equals(rowData.get(timeFields[j][1]))) {
                        String date = rowData.get(timeFields[j][0]).toString();
                        String time = rowData.get(timeFields[j][1]).toString();
                        rowData.put(timeFields[j][0], date + " " + time);
                    }
                }
                if ("0".equals(rowData.get("test_drive"))) {
                    rowData.put("test_drive", "试驾");
                } else {
                    rowData.put("test_drive", "不试驾");
                }
                if ("0".equals(rowData.get("car_show"))) {
                    rowData.put("car_show", "展示");
                } else {
                    rowData.put("car_show", "未展示");
                }
                if ("0".equals(rowData.get("order_finished"))) {
                    rowData.put("order_finished", "订单未完成");
                } else {
                    rowData.put("order_finished", "订单完成");
                }
                if ("0".equals(rowData.get("order_finished_car"))) {
                    rowData.put("order_finished_car", "订单未完成");
                } else {
                    rowData.put("order_finished_car", "订单完成");
                }
                ESDoc storage = new ESDoc("car", "car", null, rowData);
                docs.add(storage);
            }
            if (docs.size() >= 100) {
                saveBulk(docs);
                docs.clear();
            }
        }
        if (docs.size() > 0) {
            saveBulk(docs);
            docs.clear();
        }
    }

    private static TransportClient client;

    public static void saveBulk(List<ESDoc> docs) {
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            docs.forEach(e ->
                    bulkRequest.add(client.prepareIndex(e.getIndex(), e.getType(), e.getId()).setSource(e.getSource()))
            );
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                System.out.println("save docs has failures :" + bulkResponse.buildFailureMessage());
            }
        } catch (Exception ex) {
            System.out.println("save docs has error ");
        }
    }

    public static void index(String[] fields) {
        client.admin().indices().prepareCreate("car").addMapping("car", mapping(fields)).execute().actionGet();
    }

    public static XContentBuilder mapping(String[] fields) {

        return null;
    }
}
