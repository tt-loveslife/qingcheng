package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.SkuSearchService;
import com.qingcheng.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class SkuSearchController {
    @Reference
    private SkuSearchService skuSearchService;

    @RequestMapping("/search")
    public String search(Model model, @RequestParam Map<String, String> searchMap){
        try{
            searchMap = WebUtil.convertCharsetToUTF8(searchMap);
            Map search = skuSearchService.search(searchMap);
            model.addAttribute("result", search);

            // 构建请求url
            StringBuilder url = new StringBuilder("/search.do?");
            for (Map.Entry entry:searchMap.entrySet()){
                url.append("&" + entry.getKey() + "=" + entry.getValue());
            }
            model.addAttribute("url", url);
            model.addAttribute("searchMap", searchMap);
            return "search";
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
