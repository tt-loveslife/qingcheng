package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.StringUtil;
import com.qingcheng.dao.SkuMapper;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.service.goods.SkuSearchService;
import com.qingcheng.util.CacheKey;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkuSearchServiceImpl implements SkuSearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    private final String SKU_CATEGORY_AGGR_NAME = "sku_category";
    private final String SKU_SPEC_MAP_NAME = "name";
    private final String SKU_SPEC_MAP_OPTIONS = "options";

    public Map search(Map<String, String> searchMap) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("sku");
        searchRequest.types("doc");

        // ???????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", searchMap.get("keywords"));
        boolQueryBuilder.must(matchQueryBuilder);

        // ????????????
        if (searchMap.containsKey("category")){
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("categoryName", searchMap.get("category"));
            boolQueryBuilder.filter(termQueryBuilder);
        }

        // ????????????
        if (searchMap.containsKey("brand")){
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("brandName", searchMap.get("brand"));
            boolQueryBuilder.filter(termQueryBuilder);
        }

        // ??????????????????
        for (String key:searchMap.keySet()){
            if (key.startsWith("spec.")){
                TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(key + ".keyword", Strings.replace(searchMap.get(key), " ", "+"));
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        // ????????????
        if(searchMap.containsKey("startPrice") && !searchMap.get("startPrice").equals("0")){
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(searchMap.get("startPrice") + "00");
            boolQueryBuilder.must(rangeQueryBuilder);
        }
        if(searchMap.containsKey("endPrice") && !searchMap.get("endPrice").equals("*")){
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").lte(searchMap.get("endPrice") + "00");
            boolQueryBuilder.must(rangeQueryBuilder);
        }

        // ????????????
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(SKU_CATEGORY_AGGR_NAME).field("categoryName");
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.aggregation(termsAggregationBuilder);

        int pageNo = Integer.parseInt(searchMap.get("pageNo"));
        pageNo = pageNo <= 0?1:pageNo;
        int pageSize = 30;
        int from = (pageNo - 1) * pageSize;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(pageSize);

        // ??????
        String sortName = searchMap.get("sortName");
        String sortOrder = searchMap.get("sortOrder");
        if (!"".equals(sortName)){
            searchSourceBuilder.sort(sortName, SortOrder.valueOf(sortOrder));
        }

        // ??????
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name").preTags("<font style='color:red'>").postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);


        searchRequest.source(searchSourceBuilder);

        Map resultMap = new HashMap();
        try{
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();
            Integer totalHitsCount = (int) searchHits.getTotalHits();
            // ???????????????
            long pageCount = totalHitsCount % pageSize == 0?totalHitsCount / pageSize:totalHitsCount / pageSize + 1;

            // ??????Sku????????????
            List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
            for (SearchHit hit:searchHits.getHits()){
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField name = highlightFields.get("name");
                Text[] fragments = name.getFragments();
                sourceAsMap.put("name", fragments[0].toString());
                resultList.add(sourceAsMap);
            }

            // ?????? ????????????
            Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().getAsMap();
            Terms terms = (Terms) aggregationMap.get(SKU_CATEGORY_AGGR_NAME);
            List<String> categoryList = new ArrayList<String>();
            for (Terms.Bucket bucket:terms.getBuckets()){
                categoryList.add(bucket.getKeyAsString());
            }

            String categoryName = "";
            if (searchMap.containsKey("category")){
                categoryName = searchMap.get("category");
            }else{
                if (categoryList != null && categoryList.size() > 0){
                    categoryName = categoryList.get(0);
                }
            }
            List<Map> brandList = getBrandListByCategory(categoryName);

            List<Map> specByCategoryName = getSpecListByCategory(categoryName);
            for (Map spec:specByCategoryName){
                spec.put(SKU_SPEC_MAP_OPTIONS, ((String)spec.get(SKU_SPEC_MAP_OPTIONS)).split(","));
            }

            resultMap.put("brandList", brandList);
            resultMap.put("rows", resultList);
            resultMap.put("categoryList", categoryList);
            resultMap.put("totalPages", pageCount);
            resultMap.put("specList", specByCategoryName);

        }catch (Exception e){
            e.printStackTrace();
        }

        return resultMap;
    }

    public void loadSkuCateToSpec() {
        if(!redisTemplate.hasKey(CacheKey.CATEGORY_SPEC_MAP)){
            List<String> categoryList = skuMapper.getAllCategory();
            for (String category:categoryList){
                redisTemplate.boundHashOps(CacheKey.CATEGORY_SPEC_MAP).put(category, skuMapper.findSpecByCategoryName(category));
            }
        }
    }

    public void loadSkuCateToBrand() {
        if(!redisTemplate.hasKey(CacheKey.CATEGORY_BRAND_MAP)){
            List<String> categoryList = skuMapper.getAllCategory();
            for (String category:categoryList){
                redisTemplate.boundHashOps(CacheKey.CATEGORY_BRAND_MAP).put(category, skuMapper.findListByCategoryName(category));
            }
        }
    }

    public List<Map> getBrandListByCategory(String category){
        return (List<Map>) redisTemplate.boundHashOps(CacheKey.CATEGORY_BRAND_MAP).get(category);
    }

    public List<Map> getSpecListByCategory(String category){
        return (List<Map>) redisTemplate.boundHashOps(CacheKey.CATEGORY_SPEC_MAP).get(category);
    }
}
