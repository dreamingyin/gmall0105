package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Steven on 2019/12/13.
 */

@Controller
public class IndexController {
    @RequestMapping("index")
    @ResponseBody
    public String index(){
        return "Hello SpringBoot Index!!!";
    }
}
