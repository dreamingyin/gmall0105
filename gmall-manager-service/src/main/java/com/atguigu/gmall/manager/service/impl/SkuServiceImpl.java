package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.PmsSkuAttrValue;
import com.atguigu.gmall.bean.PmsSkuImage;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.manager.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manager.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manager.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manager.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.manager.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Steven on 2019/12/27.
 */
@Service
public class SkuServiceImpl implements SkuService{

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    RedisUtil redisUtil;


    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
       //插入skuInfo
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId=pmsSkuInfo.getId();

        //插入平台属性
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);

        }
        //插入销售属性的关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }
        //插入图片
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

        //发出商品的缓存同步消息
        //发出商品搜索引擎的同步消息

    }

    public PmsSkuInfo getSkuByIdFromDb(String skuId){
        PmsSkuInfo pmsSkuInfo=new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //skud的图片集合
        PmsSkuImage skuImage=new PmsSkuImage();
        skuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(pmsSkuImages);
        return skuInfo;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId,String remoteAddr) {

        PmsSkuInfo pmsSkuInfo=new PmsSkuInfo();
        System.out.println("IP地址为："+remoteAddr+"线程："+Thread.currentThread().getName()+"进入商品访问页");
        //连接缓存
        Jedis jedis = redisUtil.getJedis();
        // 查询缓存
        String skuKey="sku:"+skuId+":info";
        String skuJson=jedis.get(skuKey);

        if(StringUtils.isNotEmpty(skuJson)){
            System.out.println("IP地址为："+remoteAddr+"线程："+Thread.currentThread().getName()+"从缓存中读取");
            pmsSkuInfo=JSON.parseObject(skuJson,PmsSkuInfo.class);
        }else{
            //缓存中没有
            System.out.println("IP地址为："+remoteAddr+"线程："+Thread.currentThread().getName()+"发现缓存中没有，申请分布式锁"+"sku:" + skuId + ":lock");
            //设置分布式锁
            String token= UUID.randomUUID().toString();
            String OK = jedis.set("sku:" + skuId + ":lock", token, "nx", "px", 10*1000);
            if(StringUtils.isNotEmpty(OK) && OK.equals("OK")){
                //设置成功有权10s内访问数据库
                System.out.println("IP地址为："+remoteAddr+"线程："+Thread.currentThread().getName()+"设置成功有权10s内访问数据库");
                pmsSkuInfo= getSkuByIdFromDb(skuId);
                //在归还锁之前睡眠5s
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(pmsSkuInfo!=null){
                    //将MySQL数据同步到redis中
                    jedis.set(skuKey, JSON.toJSONString(pmsSkuInfo));
                }else {
                    //数据库中不存在该sku
                    //为防止缓存穿透，null或空字符串值设置给redis
                    jedis.setex(skuKey, 60 * 3, JSON.toJSONString(""));
                }
                //访问MySQL之后要释放分布式锁
                System.out.println("IP地址为："+remoteAddr+"线程："+Thread.currentThread().getName()+"使用完毕将所归还");
                String lockToken = jedis.get("sku:" + skuId + ":lock");
                if(StringUtils.isNotEmpty(lockToken)&& lockToken.equals(token)) {
                    //jedis.eval("lua");
                    //token确认删除的是自己sku的锁
                    jedis.del("sku:" + skuId + "lock");
                }
            }else {
                //设置失败,自旋(该线程在睡眠几秒后重新访问数据库)
                System.out.println("IP地址为："+remoteAddr+"线程："+Thread.currentThread().getName()+"没有拿到锁开始自旋");
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                     return getSkuById(skuId,remoteAddr);
            }

        }
        jedis.close();

        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        List<PmsSkuInfo> pmsSkuInfoList=pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfoList;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuId = pmsSkuInfo.getId();
            PmsSkuAttrValue pmsSkuAttrValue=new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
        }
        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal productPrice) {
        boolean flag=false;
        PmsSkuInfo pmsSkuInfo=new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        BigDecimal price = skuInfo.getPrice();
        if(price.compareTo(productPrice)==0){
            flag=true;
        }
        return flag;
    }

}
