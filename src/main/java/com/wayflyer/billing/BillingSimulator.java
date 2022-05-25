package com.wayflyer.billing;

import com.wayflyer.billing.service.BillingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
public class BillingSimulator {
    private final LocalDate startDate;

    private final LocalDate endDate;

    private final BillingService billingService;

    @Autowired
    public BillingSimulator(BillingService billingService,
                            @Value("#{T(java.time.LocalDate).parse('${simulator.startDate}')}") LocalDate startDate,
                            @Value("#{T(java.time.LocalDate).parse('${simulator.endDate}')}") LocalDate endDate
    ) {
        this.billingService = billingService;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void simulate() {
        log.info("Simulating billing from {} to {}.", startDate, endDate);

        for (LocalDate today = startDate; today.isBefore(endDate); today = today.plusDays(1)) {
            log.info("Starting simulation for {}.", today);
            billingService.process(today);
            log.info("Finished simulation for {}\n", today);
        }

        log.info("Simulation complete.");
    }
}
