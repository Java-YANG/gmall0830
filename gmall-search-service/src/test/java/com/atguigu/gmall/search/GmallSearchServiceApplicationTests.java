package com.atguigu.gmall.search;



import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.search.bean.Movie;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallSearchServiceApplicationTests {
	@Autowired
	private JestClient jestClient;

	@Reference
	private SkuService skuService;

	@Test
	public void contextLoads() {
		// 查询所有Sku
		List<PmsSkuInfo> pmsSkuInfoList = skuService.getAllSku();

		// 数据封装到
		List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();
		for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
			PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();

			BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);

			Index index = new Index.Builder(pmsSearchSkuInfo).id(pmsSearchSkuInfo.getId()).type("pmsSearchSkuInfo").index("gmall0830").build();

			try {
				DocumentResult execute = jestClient.execute(index);
			} catch (IOException e) {
				e.printStackTrace();
			}
			pmsSearchSkuInfoList.add(pmsSearchSkuInfo);
		}
	}

	@Test
	public void test() throws IOException {
		Search search = new Search.Builder("{}").addIndex("movie_index").addType("movie").build();

		SearchResult execute = jestClient.execute(search);

		List<SearchResult.Hit<Movie, Void>> hits = execute.getHits(Movie.class);

		for (SearchResult.Hit<Movie, Void> hit : hits) {
			Movie source = hit.source;
			System.out.println(source.getName());
		}

//		System.out.println(execute);
	}


	@Test
	public void search() throws IOException {
		// 查询的dsl的封装对象
		String query = "";
		query = getMySearchSourceBuilder();
		System.out.println(query);
		Search search = new Search.Builder(query).addIndex("movie_index").addType("movie").build();
		// 执行search
		SearchResult execute = jestClient.execute(search);

		List<SearchResult.Hit<Movie, Void>> hits = execute.getHits(Movie.class);

		for (SearchResult.Hit<Movie, Void> hit : hits) {
			Movie source = hit.source;
			System.out.println(source.getName());
		}
		System.out.println(execute);
	}

	public String getMySearchSourceBuilder(){
		// _search
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		// bool
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

		// term
		TermQueryBuilder termQueryBuilder = new TermQueryBuilder("id","3");
		// filter
		boolQueryBuilder.filter(termQueryBuilder);

		// match
		MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("name","行动");
		// must
		boolQueryBuilder.must(matchQueryBuilder);

		// query
		searchSourceBuilder.query(boolQueryBuilder);

		return searchSourceBuilder.toString();
	}
}
