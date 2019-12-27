package com.atguigu.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsBaseSaleAttr;
import com.atguigu.gmall.bean.PmsProductImage;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.service.SpuService;
import com.atguigu.gmall.utils.PmsUploadUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Created by Steven on 2019/12/19.
 */

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuService;


    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId)  {
        List<PmsProductImage>pmsProductImages=spuService.spuImageList(spuId);
        return pmsProductImages;
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId)  {
        List<PmsProductSaleAttr> pmsProductSaleAttrs=spuService.spuSaleAttrList(spuId);
        return pmsProductSaleAttrs;
    }

    /**
     * 将图片或视频上传到分布式存储系统并将图片路径返回给页面
     * @param multipartFile 将表单上传类型file转为springMvc上传图片类型MultiPartFile类型
     * @return
     */
    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        //将图片上传到分布式存储系统
        String urlImamge= PmsUploadUtils.uploadImage(multipartFile);
        return urlImamge;
    }

    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo productInfo){
        spuService.saveSpuInfo(productInfo);
        return "success";
    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){
        List<PmsProductInfo> productInfos=spuService.spuList(catalog3Id);
        return productInfos;
    }

}
