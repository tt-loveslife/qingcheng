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
            if (searchMap.get("sortName") == null){
                searchMap.put("sortName", "");
            }
            if (searchMap.get("sortOrder")== null){
                searchMap.put("sortOrder", "DESC");
            }
            // 分页
            if (searchMap.get("pageNo") == null){
                searchMap.put("pageNo", "1");
            }
            Map search = skuSearchService.search(searchMap);

            // 构建请求url
            StringBuilder url = new StringBuilder("/search.do?");
            for (Map.Entry entry:searchMap.entrySet()){
                url.append("&" + entry.getKey() + "=" + entry.getValue());
            }

            int pageNo = Integer.parseInt(searchMap.get("pageNo"));
            model.addAttribute("pageNo", pageNo);
            Long totalPages = (Long) search.get("totalPages");
            int startPage = 1;
            int endPage = totalPages.intValue();
            if (totalPages > 5){
                startPage = pageNo - 2;
                if (startPage <= 0){
                    startPage = 1;
                }
                endPage = pageNo + 4;
                if (endPage > totalPages){
                    endPage = totalPages.intValue();
                }
            }

            model.addAttribute("result", search);
            model.addAttribute("url", url);
            model.addAttribute("searchMap", searchMap);
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);


            return "search";
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
