package com.gemini.security_.dyn;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.gemini.security_.dyn.mapper")
public class DynApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynApplication.class, args);
    }

}
