package com.atguigu.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

	@Reference
	SkuService skuService; //查询MySQL

	@Autowired
	JestClient jestClient;
	@Test
	public void contextLoads() throws IOException {

		/*//jest的工具
		SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();

		//bool
		BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();
		//filter
		TermQueryBuilder termQueryBuilder=new TermQueryBuilder("skuAttrValueList.valueId","39");
		boolQueryBuilder.filter(termQueryBuilder);
		//must
		MatchQueryBuilder matchQueryBuilder=new MatchQueryBuilder("skuName","Apple");
		boolQueryBuilder.must(matchQueryBuilder);
		//query
		searchSourceBuilder.query(boolQueryBuilder);

		//from
		searchSourceBuilder.from(0);

		//size
		searchSourceBuilder.size(20);
		//hightlighter
		searchSourceBuilder.highlighter();

		String delStr=searchSourceBuilder.toString();
		System.out.println(delStr);

		//用API进行复杂查询
		List<PmsSearchSkuInfo> pmsSearchSkuInfoList=new ArrayList<>();

		Search search = new Search.Builder(delStr).addIndex("gmall0105").addType("PmsSkuInfo").build();
		SearchResult execute = jestClient.execute(search);

		List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);

		for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
			PmsSearchSkuInfo source = hit.source;
			pmsSearchSkuInfoList.add(source);
		}
		System.out.println(pmsSearchSkuInfoList.size());*/
		put();
	}

	//在elasticSearch中导入数据
	public void  put() throws IOException {
		//查询MySQL数据库
		List<PmsSkuInfo> pmsSkuInfoList=new ArrayList<>();
		pmsSkuInfoList=skuService.getAllSku();
		//转化为es的数据结构
		List<PmsSearchSkuInfo> pmsSearchSkuInfoList=new ArrayList<>();

		for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
			PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
			BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
			pmsSearchSkuInfo.setId(Long.parseLong(pmsSkuInfo.getId()));
			pmsSearchSkuInfoList.add(pmsSearchSkuInfo);
		}
		//导入es

		for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
			Index put = new Index.Builder(pmsSearchSkuInfo).index("gmall0105").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()+"").build();
			jestClient.execute(put);
		}
	}
	
	

}
