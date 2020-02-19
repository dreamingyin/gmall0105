package com.atguigu.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /**
         * 拦截代码
         */

        //判断被拦截请求的访问方法的注解是否需要拦截
        HandlerMethod mh = (HandlerMethod) handler;
        LoginRequired methodAnnotation = mh.getMethodAnnotation(LoginRequired.class);

        StringBuffer url = request.getRequestURL();
        //是否拦截
        if(methodAnnotation==null){
            return true;
        }

        String token="";

        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);

        if(StringUtils.isNotBlank(oldToken)){
            token=oldToken;
        }
        String newToken = request.getParameter("token");
        if(StringUtils.isNotBlank(newToken)){
            token=newToken;
        }


        /**
         * 该请求是否登录成功
         */
        boolean loginSuccess = methodAnnotation.loginSuccess();

        //请用认证中心进行验证
        String success="fail";
        Map<String,String> successMap=new HashMap<String, String>();
        if(StringUtils.isNotBlank(token)){
            String ip = request.getHeader("x-forwarded-for");
            //从request中获取IP
            if(StringUtils.isBlank(ip)) {
                ip=request.getRemoteAddr();
                if(StringUtils.isBlank(ip)) {
                    ip="127.0.0.1";
                }
            }
            String successJson = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token+"&currentIp="+ip);
            successMap = JSON.parseObject(successJson, Map.class);

            success=successMap.get("status");
        }
        if(loginSuccess){
            //必须登陆成功才能使用
           if(!success.equals("success")){
               //重定向passport登陆
               StringBuffer requestURL = request.getRequestURL();
               response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl="+requestURL);
               return false;
           }
               //验证通过，覆盖token
               request.setAttribute("memberId",successMap.get("memberId"));
               request.setAttribute("nickname",successMap.get("nickName"));
            //验证通过，覆盖cookie里的token
            if(StringUtils.isNotBlank(token)) {
                CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
            }

        }else{
            //没有登陆也可以用，但是必须校验
            if(success.equals("success")){
                //重定向passport登陆
                request.setAttribute("memberId",successMap.get("memberId"));
                request.setAttribute("nickname",successMap.get("nickName"));
            }

        }

        return true;
    }
}