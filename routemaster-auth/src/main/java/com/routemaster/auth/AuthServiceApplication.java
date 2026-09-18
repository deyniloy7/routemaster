package com.routemaster.auth;

import com.routemaster.common.constants.ApiPaths;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.routemaster.auth", "com.routemaster.common"})
public class AuthServiceApplication {
    public static void main(String[] args) {
        System.out.println("register path -------------------------------> " + ApiPaths.Auth.REGISTER);
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
