package com.natsu.memorynoteweb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    // 使用?分隔路径和参数，使用&分隔多个参数
    // http://localhost:8080/hello?username=zhangsan&password=123456
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello(String username, String password){
        System.out.println(username);
        System.out.println(password);
        return "Hello, Memory Note Web!" + username + password;
    }

}