package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.order.OrderReport;
import com.qingcheng.service.order.OrderReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orderReport")
public class OrderReportController {

    @Reference
    private OrderReportService orderReportService;

    @RequestMapping("/categoryReport")
    public List<OrderReport> categoryReport(String dateString){
        LocalDate localDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return orderReportService.categoryReport(localDate);
    }

    @RequestMapping("/yesterday")
    public List<OrderReport> yesterdayReport(){
        LocalDate localDate = LocalDate.now().minusDays(1);
        return orderReportService.categoryReport(localDate);
    }

    @RequestMapping("/category1Count")
    public List<Map> category1Count(String date1, String date2){
        return orderReportService.category1Count(date1, date2);
    }
}
