package com.gemini.security_.dyn.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/user/hello")
    public String userHello(){
        return "user hello";
    }
    @GetMapping("/admin/hello")
    public String adminHello(){
        return "admin hello";
    }
    @GetMapping("/index")
    public String success(){
        return "index";
    }
}
