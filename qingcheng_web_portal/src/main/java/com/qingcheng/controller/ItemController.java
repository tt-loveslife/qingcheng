package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.qingcheng.pojo.goods.Category;
import com.qingcheng.pojo.goods.Goods;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.goods.Spu;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/item")
public class ItemController {
    @Reference
    private SpuService spuService;

    @Reference
    private CategoryService categoryService;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${pagePath}")
    private String pagePath;

    @RequestMapping("/createPages")
    public void createPages(String id){
        Goods goodsById = spuService.findGoodsById(id);
        Spu spu = goodsById.getSpu();
        List<Sku> skuList = goodsById.getSkuList();

        List<String> categoryList = new ArrayList<>();
        categoryList.add(categoryService.findById(spu.getCategory1Id()).getName());
        categoryList.add(categoryService.findById(spu.getCategory2Id()).getName());
        categoryList.add(categoryService.findById(spu.getCategory3Id()).getName());

        Map urlMap = new HashMap();
        for (Sku sku:skuList){
            String specJson = JSON.toJSONString(JSON.parseObject(sku.getSpec()), SerializerFeature.MapSortField);
            urlMap.put(specJson, sku.getId() + ".html");
        }

        for(Sku sku:skuList){
            Context context = new Context();
            Map paramItems= JSON.parseObject(spu.getParaItems());  // 获得统一参数
            Map specItems = JSON.parseObject(sku.getSpec());  // 获得具体规格

            Map<String, List> specMap = (Map) JSON.parse(spu.getSpecItems()); // 获取所有规格
            for(String key:specMap.keySet()){
                List<String> list = specMap.get(key);
                List<Map> mapList = new ArrayList<>();
                for (String value:list){
                    Map map = new HashMap();
                    map.put("option", value);
                    if (value.equals(specItems.get(key))){
                        map.put("checked", true);
                    }else{
                        map.put("checked", false);
                    }
                    Map spec = JSON.parseObject(sku.getSpec());
                    spec.put(key, value);
                    String specJson = JSON.toJSONString(spec, SerializerFeature.MapSortField);
                    map.put("url", urlMap.get(specJson));

                    mapList.add(map);
                }
                specMap.put(key, mapList);
            }

            HashMap<String, Object> dataModel = new HashMap<>();
            dataModel.put("spu", goodsById.getSpu());
            dataModel.put("sku", sku);
            dataModel.put("categoryList", categoryList);
            dataModel.put("skuImages", sku.getImages().split(","));
            dataModel.put("spuImages", spu.getImages().split(","));
            dataModel.put("paraItems", paramItems);
            dataModel.put("specItems", specItems);
            dataModel.put("specMap", specMap);
            context.setVariables(dataModel);

            File dir = new File(pagePath);
            if (!dir.exists()){
                dir.mkdirs();
            }
            File dest = new File(dir, sku.getId() + ".html");

            try{
                PrintWriter printWriter = new PrintWriter(dest);
                templateEngine.process("item", context, printWriter);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
