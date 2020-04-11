package com.atguigu.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsSearchParam;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.constant.GmallConstant;
import com.atguigu.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private JestClient jestClient;

    @Override
    public List<PmsSearchSkuInfo> search(PmsSearchParam pmsSearchParam) {
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();

        // 查询的dsl的封装对象
        String query = "";
        query = getMySearchSourceBuilder(pmsSearchParam);
        Search search = new Search.Builder(query).addIndex(GmallConstant.ATTR_INDEX_NAME).addType(GmallConstant.ATTR_INDEX_TYPE).build();
        // 执行search
        SearchResult execute = null;
        try {
            execute = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 获取搜索到的所有信息
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        // 遍历搜索到的信息
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            // 获取每个信息的具体内容
            PmsSearchSkuInfo source = hit.source;
            // 添加到集合中
            pmsSearchSkuInfoList.add(source);
        }
        return pmsSearchSkuInfoList;
    }

    public String getMySearchSourceBuilder(PmsSearchParam pmsSearchParam){
        // 查询条件
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueIds = pmsSearchParam.getValueId();

        // _search
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 三级分类id查询
        if(StringUtils.isNotBlank(catalog3Id)){
            // term
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder(GmallConstant.ATTR_TERM_CATALOG,catalog3Id);
            // filter
            boolQueryBuilder.filter(termQueryBuilder);
        }
        // 属性值id查询
        if(valueIds != null && valueIds.length > 0){
            for (String valueId : valueIds) {
                // term
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder(GmallConstant.ATTR_TERM_VALUEIDS,valueId);
                // filter
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        // 关键字查询
        if(StringUtils.isNotBlank(keyword)){
            // match
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder(GmallConstant.ATTR_MATCH_SKUNAME,keyword);
            // must
            boolQueryBuilder.must(matchQueryBuilder);
        }

        // query
        searchSourceBuilder.query(boolQueryBuilder);

        // from size
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(200);
        return searchSourceBuilder.toString();
    }
}
