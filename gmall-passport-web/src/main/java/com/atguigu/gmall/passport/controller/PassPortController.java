package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.HttpclientUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Steven on 2020/2/3.
 */
@Controller
public class PassPortController {

    @Reference
    UserService userService;

    @RequestMapping("vlogin")
    public String vlogin(String code,HttpServletRequest request){

        //授权码换取access_token
        String s3="https://api.weibo.com/oauth2/access_token?";
        //?client_id=3193708961&client_secret=df2b2370fbd3483cdd8a9eb422ca325&grant_type=authorization_code&redirect_uri=http://passport.gmall.com:8085/vlogin&code=CODE";

        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("client_id","3193708961");
        paramMap.put("client_secret","df2b2370fbd3483cdd8a9eb422ca3253");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://passport.gmall.com:8085/vlogin");
        paramMap.put("code",code);

        String access_token_json = HttpclientUtil.doPost(s3, paramMap);
        Map<String,String> access_map = JSON.parseObject(access_token_json, Map.class);

        //access_token获取用户信息
        String access_token = access_map.get("access_token");
        String uid = access_map.get("uid");
        String s4="https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid="+uid;
        String s = HttpclientUtil.doGet(s4);
        Map<String,Object> map = JSON.parseObject(s, Map.class);

        //将用户信息保存到数据库，将用户类型设置为微博用户
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceType("2");
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setNickname((String)map.get("screen_name"));
        umsMember.setSourceUid(((Long)map.get("id")));
        umsMember.setCity((String) map.get("location"));
        umsMember.setGender((String) (map.get("gender")));

        UmsMember umsCheck = new UmsMember();
        umsCheck.setSourceUid(umsMember.getSourceUid());
        UmsMember checkUmsMember = userService.checkOauthUser(umsCheck);
        if(checkUmsMember==null){
            umsMember=userService.addOldOauthUser(umsMember);
        }else{
            umsMember=umsCheck;
        }

        //生成JWT的token,并且重定向到首页，携带token
        String token=null;
        //rpc主键返回策略失效
        String memberId = umsMember.getId();
        String nickname = umsMember.getNickname();

        Map<String,Object> userMap=new HashMap<>();
        userMap.put("memberId",memberId);
        userMap.put("nickname",nickname);

        String ip = request.getHeader("x-forwarded-for");
        //从request中获取IP
        if(StringUtils.isBlank(ip)) {
            ip=request.getRemoteAddr();
            if(StringUtils.isBlank(ip)) {
                ip="127.0.0.1";
            }
        }

        //按照设计的算法对参数进行加密后，生成token
        token = JwtUtil.encode("2019gmall0105", userMap, ip);

        return "redirect:http://search.gmall.com:8083/index?token="+token;
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token,String currentIp){

        //通过jwt校验token真假
        Map<String,String> map=new HashMap<>();

        Map<String, Object> decode = JwtUtil.decode(token, "2019gmall0105", currentIp);
        if(decode!=null) {
            map.put("status", "success");
            map.put("memberId", (String) decode.get("memberId"));
            map.put("nickName", (String) decode.get("nickName"));
        }else{
            map.put("status", "fail");
        }
        return JSON.toJSONString(map);
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){

        String token="";
        //调用用户服务验证用户名和密码
        UmsMember userMemberLogin= userService.login(umsMember);
        if(userMemberLogin!=null){
            //登陆成功

            //用jwt制作token

            //将token存入redis一份
            String memberId = userMemberLogin.getId();
            String nickname = userMemberLogin.getNickname();
            Map<String,Object> userMap=new HashMap<>();
            userMap.put("memberId",memberId);
            userMap.put("nickname",nickname);

            String ip = request.getHeader("x-forwarded-for");
            //从request中获取IP
            if(StringUtils.isBlank(ip)) {
                ip=request.getRemoteAddr();
                if(StringUtils.isBlank(ip)) {
                    ip="127.0.0.1";
                }
            }

            //按照设计的算法对参数进行加密后，生成token
            token = JwtUtil.encode("2019gmall0105", userMap, ip);

            //将token放入redis一份
            userService.addUserToken(token,memberId);

        }else{
            //登陆失败
            token="fail";
        }
        return token;
    }

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap map){

        map.put("ReturnUrl",ReturnUrl);
        return "index";
    }


}
