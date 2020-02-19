package com.atguigu.gmall.search.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.BaseAttrInfoService;
import com.atguigu.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

/**
 * Created by Steven on 2020/1/17.
 */
@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    BaseAttrInfoService baseAtrrInfoService;

    //三级分类Id,关键字
    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){
        //调用搜索服务，返回查询结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos=searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList",pmsSearchSkuInfos);

        //抽取搜索结果中平台属性的集合
        Set<String> valueIdSet=new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }

        //根据valueId将属性列表查询出来
        List<PmsBaseAttrInfo> baseAttrInfos= baseAtrrInfoService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList",baseAttrInfos);

        //平台属性集合进一步处理，去掉当前属性valueId集合所在的集合
        String[] delValueIds = pmsSearchParam.getValueId();
        if(delValueIds!=null) {
            //面包屑
            //pmsSearchParam
            //delValueId
            List<PmsSearchCrumb> pmsSearchCrumbs=new ArrayList<>();
            for (String delValueId : delValueIds) {
                /**
                 * 平台属性集合
                 */
                Iterator<PmsBaseAttrInfo> iterator = baseAttrInfos.iterator();
                PmsSearchCrumb pmsSearchCrumb=new PmsSearchCrumb();
                //生成面包屑的参数
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setUrlParam(getUrlParam(pmsSearchParam,delValueId));
            while (iterator.hasNext()) {
                PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                    String valueId = pmsBaseAttrValue.getId();
                    if (valueId.equals(delValueId)) {
                        //查找面包屑的属性名称
                        pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                        //删除该属性所在的属性数组
                        iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
            modelMap.put("attrValueSelectedList",pmsSearchCrumbs);
        }
        String urlParam=getUrlParam(pmsSearchParam);
        modelMap.put("urlParam",urlParam);
        String keyword = pmsSearchParam.getKeyword();
        if(StringUtils.isNotBlank(keyword)){
            modelMap.put("keyword",urlParam);
        }

        return "list";
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam,String ...delValueId) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam="";
        if(StringUtils.isNotBlank(keyword)){
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if (StringUtils.isNotBlank(catalog3Id)){
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam=urlParam+"catalog3Id="+catalog3Id;
        }

        if (skuAttrValueList!=null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                if (!pmsSkuAttrValue.equals(delValueId)){
                    urlParam=urlParam+"&valueId="+pmsSkuAttrValue;
                }
            }
        }
        return urlParam;
    }

    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String index(){
        return "index";
    }
}
