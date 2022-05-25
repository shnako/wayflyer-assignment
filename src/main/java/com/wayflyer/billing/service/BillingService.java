package com.wayflyer.billing.service;

import com.wayflyer.billing.client.WayflyerClient;
import com.wayflyer.billing.model.Advance;
import com.wayflyer.billing.model.Advances;
import com.wayflyer.billing.model.Charge;
import com.wayflyer.billing.model.Revenue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BillingService {
    @Value("${config.maxDailyCharge}")
    private BigDecimal maxDailyCharge;

    private final WayflyerClient wayflyerClient;

    @Getter
    private final Map<Integer, Advance> advances;

    private final List<Charge> outstandingCharges;

    @Autowired
    public BillingService(WayflyerClient wayflyerClient) {
        this.wayflyerClient = wayflyerClient;

        advances = new HashMap<>();
        outstandingCharges = new ArrayList<>();
    }

    public void process(LocalDate today) {
        processOutstandingCharges(today);

        retrieveAdvances(today);

        Map<Integer, Revenue> revenueMap = retrieveRevenues(today, advances.values());

        for (Advance advance : advances.values()) {
            billBasedOnRevenue(today, advance, revenueMap.getOrDefault(advance.getCustomerId(), null));
        }

        processCompletedAdvances(today, advances.values());
        log.info("End of day outstanding advance amounts report: {}", advances.values());
    }

    private void billBasedOnRevenue(LocalDate today, Advance advance, Revenue revenue) {
        if (advance.isCompleted()) {
            return;
        }

        if (today.isBefore(advance.getRepaymentStartDate())) {
            return;
        }

        if (revenue == null) {
            outstandingCharges.add(new Charge(advance, today, null, null));
            return;
        }

        BigDecimal chargeAmount = calculateCharge(advance, revenue);

        var charge = new Charge(advance, today, null, chargeAmount);

        applyCharge(today, charge);
    }

    private void processOutstandingCharges(LocalDate today) {
        calculateChargesForDelayedRevenues(today);

        var chargesToProcess = new ArrayList<>(outstandingCharges);
        for (Charge charge : chargesToProcess) {
            if (charge.getAmount() != null) {
                outstandingCharges.remove(charge);
                applyCharge(today, charge);
            }
        }
    }

    private void calculateChargesForDelayedRevenues(LocalDate today) {
        List<Revenue> delayedRevenues = retrieveDelayedRevenues(today);

        for (Charge charge : outstandingCharges) {
            if (charge.getAmount() == null) {
                Revenue revenue = delayedRevenues
                        .stream()
                        .filter(rev -> charge.getAdvance().getCustomerId() == rev.getCustomerId() && charge.getDateFor().equals(rev.getDate()))
                        .findFirst()
                        .orElse(null);
                if (revenue != null) {
                    BigDecimal chargeAmount = calculateCharge(charge.getAdvance(), revenue);
                    charge.setAmount(chargeAmount);
                }
            }
        }
    }

    private List<Revenue> retrieveDelayedRevenues(LocalDate today) {
        return outstandingCharges
                .stream()
                .filter(charge -> charge.getAmount() == null)
                .map(charge -> Pair.of(charge.getAdvance().getCustomerId(), charge.getDateFor()))
                .collect(Collectors.toSet())
                .stream()
                .map(pair -> retrieveRevenues(today, Set.of(pair.getLeft()), pair.getRight()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private void applyCharge(LocalDate today, Charge charge) {
        var advance = charge.getAdvance();

        if (advance.isCompleted() || advance.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
            log.info("Advance {} has been fully repaid so cancelling charge {}.", advance.getId(), charge);
            return;
        }

        if (advance.getOutstandingAmount().compareTo(charge.getAmount()) < 0) {
            log.info("Advance {} has outstanding amount {}, which is less than the charge of {}. Will charge the outstanding amount instead.", advance.getId(), advance.getOutstandingAmount(), charge.getAmount());
            charge.setAmount(advance.getOutstandingAmount());
        }

        BigDecimal maxAmountChargeableToday = maxDailyCharge.subtract(advance.getAmountChargedOnDate(today));
        if (maxAmountChargeableToday.compareTo(charge.getAmount()) < 0) {
            log.info("We're splitting {} in order to avoid charging more than the {} daily limit.", charge, maxDailyCharge);
            var outstandingCharge = new Charge(charge.getAdvance(), charge.getDateFor(), null, charge.getAmount().subtract(maxAmountChargeableToday));
            outstandingCharges.add(outstandingCharge);
            charge.setAmount(maxAmountChargeableToday);

            if (charge.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                log.info("We'll charge the entire amount of {} in the future as we've already reached the daily limit.", outstandingCharge.getAmount());
                return;
            } else {
                log.info("We're charging {} today and creating an outstanding charge of {}.", charge.getAmount(), outstandingCharge.getAmount());
            }
        }

        boolean chargeSuccessful = wayflyerClient.charge(today, charge);
        if (chargeSuccessful) {
            advance.applyCharge(charge);
        } else {
            outstandingCharges.add(charge);
        }
    }

    private void retrieveAdvances(LocalDate today) {
        Advances retrievedAdvances = wayflyerClient.getAdvances(today);
        for (Advance advance : retrievedAdvances.getAdvances()) {
            if (!advances.containsKey(advance.getId())) {
                advances.put(advance.getId(), advance);
            }
        }
    }

    private Map<Integer, Revenue> retrieveRevenues(LocalDate today, Collection<Advance> advances) {
        var customerIds = advances
                .stream()
                .filter(advance -> !advance.isCompleted())
                .filter(advance -> !today.isBefore(advance.getRepaymentStartDate()))
                .map(Advance::getCustomerId)
                .collect(Collectors.toSet());

        List<Revenue> revenues = retrieveRevenues(today, customerIds, today.minusDays(1));

        return revenues
                .stream()
                .filter(revenue -> revenue.getAmount() != null)
                .collect(Collectors.toMap(Revenue::getCustomerId, revenue -> revenue));
    }

    private List<Revenue> retrieveRevenues(LocalDate today, Set<Integer> customerIds, LocalDate forDate) {
        return customerIds
                .parallelStream()
                .map(customerId -> wayflyerClient.getRevenue(today, customerId, forDate))
                .filter(revenue -> revenue.getAmount() != null)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateCharge(Advance advance, Revenue revenue) {
        return revenue
                .getAmount()
                .multiply(advance.getRepaymentPercentage())
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
    }

    private void processCompletedAdvances(LocalDate today, Collection<Advance> advances) {
        advances
                .stream()
                .filter(advance -> !advance.isCompleted())
                .filter(advance -> advance.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0)
                .collect(Collectors.toList())
                .parallelStream()
                .forEach(advance -> {
                    boolean reportCompleteSuccessful = wayflyerClient.reportBillingComplete(today, advance.getId());
                    if (reportCompleteSuccessful) {
                        advance.setCompleted(true);
                    }
                });
    }
}
