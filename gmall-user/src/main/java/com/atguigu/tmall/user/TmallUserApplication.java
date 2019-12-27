package com.atguigu.tmall.user;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.tmall.user.mapper")
public class TmallUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(TmallUserApplication.class, args);
	}

}
