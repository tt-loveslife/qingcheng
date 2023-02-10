package com.qingcheng.service.order;

import com.qingcheng.pojo.order.OrderReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface OrderReportService {
    public List<OrderReport> categoryReport(LocalDate date);

    public void createData();

    public List<Map> category1Count(String date1, String date2);
}
