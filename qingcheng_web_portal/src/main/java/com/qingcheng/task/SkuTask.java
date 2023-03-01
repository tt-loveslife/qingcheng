package com.qingcheng.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.SkuSearchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SkuTask {
    @Reference
    private SkuSearchService skuSearchService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void loadSkuCateToSpec(){
        skuSearchService.loadSkuCateToSpec();
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void loadSkuCateToBrand(){
        skuSearchService.loadSkuCateToBrand();
    }
}
