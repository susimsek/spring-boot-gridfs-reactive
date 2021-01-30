package com.spring.gridfs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class GridfsReactiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(GridfsReactiveApplication.class, args);
    }

}
