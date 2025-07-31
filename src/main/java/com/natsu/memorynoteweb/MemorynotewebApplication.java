package com.natsu.memorynoteweb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.natsu.memorynoteweb.mapper")
public class MemorynotewebApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemorynotewebApplication.class, args);
    }

}
