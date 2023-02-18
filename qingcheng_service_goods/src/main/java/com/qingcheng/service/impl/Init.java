package com.qingcheng.service.impl;

import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.util.CacheKey;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Init implements InitializingBean {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuService skuService;

    public void afterPropertiesSet() throws Exception {
        System.out.println("缓存预热：---商品分类---");
        categoryService.saveCategoryTreeToRedis();
        skuService.saveAllPriceToRedis();
    }
}
