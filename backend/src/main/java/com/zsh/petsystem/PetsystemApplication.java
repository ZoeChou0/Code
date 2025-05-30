package com.zsh.petsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zsh.petsystem.mapper")
@MapperScan("com.baomidou.mybatisplus.samples.quickstart.mapper")
public class PetsystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetsystemApplication.class, args);
    }
}