package com.example.report_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ReportServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReportServerApplication.class, args);
	}

}
