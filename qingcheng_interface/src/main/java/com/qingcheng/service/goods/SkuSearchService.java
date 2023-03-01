package com.qingcheng.service.goods;

import java.util.Map;

public interface SkuSearchService {
    public Map search(Map<String, String> searchMap);

    public void loadSkuCateToSpec();


    public void loadSkuCateToBrand();

}
