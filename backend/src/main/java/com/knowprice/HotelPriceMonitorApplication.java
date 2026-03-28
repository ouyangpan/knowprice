package com.knowprice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HotelPriceMonitorApplication {
    public static void main(String[] args) {
        SpringApplication.run(HotelPriceMonitorApplication.class, args);
    }
}