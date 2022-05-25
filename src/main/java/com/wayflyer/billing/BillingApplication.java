package com.wayflyer.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BillingApplication {
    public static void main(String[] args) {
        var context = SpringApplication.run(BillingApplication.class, args);
        var billingSimulator = context.getBean(BillingSimulator.class);
        billingSimulator.simulate();
        context.stop();
    }
}
