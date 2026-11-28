package com.fyf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MyCloudAlbumServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyCloudAlbumServiceApplication.class, args);
    }

}
