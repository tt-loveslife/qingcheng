package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.order.OrderReportService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderTask {

    @Reference
    private OrderReportService orderReportService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void createData(){
        System.out.println("生成报告的定时任务");
        orderReportService.createData();
    }
}