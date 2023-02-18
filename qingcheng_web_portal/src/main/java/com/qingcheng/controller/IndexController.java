package com.qingcheng.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.qingcheng.pojo.business.Ad;
import com.qingcheng.service.business.AdService;
import com.qingcheng.service.goods.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Reference
    private AdService adService;

    @Reference
    private CategoryService categoryService;

    @GetMapping("/index")
    public String index(Model model){
        // 1. 查询分类
        List<Map> categoryTree = categoryService.findCategoryTree();
        System.out.println(JSON.toJSONString(categoryTree));
        List<Ad> indexLb = adService.findByPotision("web_index_lb");
        model.addAttribute("lbt", indexLb);
        model.addAttribute("categoryList", categoryTree);
        return "index";
    }
}
