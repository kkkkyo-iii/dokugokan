package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
public class DokugokanPackageApplication {

	public static void main(String[] args) {
		  // .envファイルをロードしてシステムプロパティとして設定する

        
		SpringApplication.run(DokugokanPackageApplication.class, args);
	}

}
