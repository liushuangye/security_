package com.gemini.security_;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.gemini.security_.mapper")
public class SecurityApplication{

    public static void main(String[] args) {
        SpringApplication.run(SecurityApplication.class, args);
    }

}
