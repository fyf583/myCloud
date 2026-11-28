package com.fyf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MyCloudDiaryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyCloudDiaryServiceApplication.class, args);
    }

}
