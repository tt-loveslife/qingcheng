package com.qingcheng.dao;

import com.qingcheng.pojo.order.OrderReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OrderReportMapper extends Mapper<OrderReport> {

    @Select("SELECT category_id1 AS categoryId1, category_id2 AS categoryId2, category_id3 AS categoryId3, DATE_FORMAT(o.pay_time, '%Y-%m-%d') AS countDate, SUM(o.pay_money) AS money, SUM(oi.num) AS num " +
            "FROM tb_order AS o, tb_order_item AS oi " +
            "WHERE o.id = oi.order_id  AND o.pay_status='1' AND DATE_FORMAT(pay_time, '%Y-%m-%d')= #{date} " +
            "GROUP BY oi.category_id1 , oi.category_id2 , oi.category_id3 , DATE_FORMAT(pay_time, '%Y-%m-%d');")
    public List<OrderReport> categoryReport(@Param("date") LocalDate date);

    @Select("SELECT category_id1 AS categoryId1, v.name AS name,SUM(money) AS money, SUM(num) AS num " +
            "FROM tb_category_report as r, v_category as v " +
            "WHERE r.category_id1 = v.id AND r.count_date >=#{date1}  AND r.count_date <=#{date2} " +
            "GROUP BY r.category_id1, v.name;")
    List<Map> category1Count(@Param("date1") String date1, @Param("date2") String date2);
}
