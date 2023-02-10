package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.OrderReportMapper;
import com.qingcheng.pojo.order.OrderReport;
import com.qingcheng.service.order.OrderReportService;
import com.qingcheng.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = OrderReportService.class)
public class OrderReportServiceImpl implements OrderReportService {

    @Autowired
    private OrderReportMapper orderReportMapper;

    @Override
    public List<OrderReport> categoryReport(LocalDate date) {
        return orderReportMapper.categoryReport(date);
    }

    @Override
    @Transactional
    public void createData() {
        LocalDate localDate = LocalDate.now().minusDays(1);
        List<OrderReport> orderReports = categoryReport(localDate);
        for (OrderReport orderReport:orderReports){
            orderReportMapper.insert(orderReport);
        }
    }

    @Override
    public List<Map> category1Count(String date1, String date2) {
        return orderReportMapper.category1Count(date1, date2);
    }
}
