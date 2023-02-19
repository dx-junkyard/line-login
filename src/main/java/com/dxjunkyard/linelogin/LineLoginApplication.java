package com.dxjunkyard.linelogin;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dxjunkyard.linelogin.repository.dao.mapper")
public class LineLoginApplication {

	public static void main(String[] args) {
		SpringApplication.run(LineLoginApplication.class, args);
	}

}
