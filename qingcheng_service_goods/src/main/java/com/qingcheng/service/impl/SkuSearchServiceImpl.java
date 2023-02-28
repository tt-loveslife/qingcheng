package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.service.goods.SkuSearchService;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkuSearchServiceImpl implements SkuSearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private final String SKU_CATEGORY_AGGR_NAME = "sku_category";

    public Map search(Map<String, String> searchMap) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("sku");
        searchRequest.types("doc");

        // 关键字查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", searchMap.get("keywords"));
        boolQueryBuilder.must(matchQueryBuilder);

        // 分类筛选
        if (searchMap.containsKey("category")){
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("categoryName", searchMap.get("category"));
            boolQueryBuilder.filter(termQueryBuilder);
        }

        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(SKU_CATEGORY_AGGR_NAME).field("categoryName");
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.aggregation(termsAggregationBuilder);
        searchRequest.source(searchSourceBuilder);

        Map resultMap = new HashMap();
        try{
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();
            Integer totalHitsCount = (int) searchHits.getTotalHits();

            // 获取Sku列表内容
            List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
            for (SearchHit hit:searchHits.getHits()){
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                resultList.add(sourceAsMap);
            }

            // 获取 聚合类型
            Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().getAsMap();
            Terms terms = (Terms) aggregationMap.get(SKU_CATEGORY_AGGR_NAME);
            List<String> categoryList = new ArrayList<String>();
            for (Terms.Bucket bucket:terms.getBuckets()){
                categoryList.add(bucket.getKeyAsString());
            }

            resultMap.put("rows", resultList);
            resultMap.put("total", totalHitsCount);
            resultMap.put("categoryList", categoryList);

        }catch (Exception e){
            e.printStackTrace();
        }

        return resultMap;
    }
}
