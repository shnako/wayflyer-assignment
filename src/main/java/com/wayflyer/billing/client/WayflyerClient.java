package com.wayflyer.billing.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayflyer.billing.model.Advances;
import com.wayflyer.billing.model.Charge;
import com.wayflyer.billing.model.Revenue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class WayflyerClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String baseUrl;

    private final RestTemplate client;

    private final ObjectMapper objectMapper;

    @Autowired
    public WayflyerClient(@Value("${client.wayflyer.baseUrl}") String baseUrl,
                          RestTemplate restTemplate,
                          ObjectMapper objectMapper
    ) {
        this.baseUrl = baseUrl;
        this.client = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Advances getAdvances(LocalDate todayDate) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Today", FORMATTER.format(todayDate));
        var httpEntity = new HttpEntity<>(httpHeaders);

        String url = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(baseUrl)
                .pathSegment("v2", "advances")
                .toUriString();

        try {
            ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, httpEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readValue(response.getBody(), Advances.class);
            } else {
                log.error("Invalid response received while loading advances: {}", response.getStatusCodeValue());
                return null;
            }
        } catch (Exception ex) {
            log.error("Unexpected exception while loading advances.", ex);
            return null;
        }
    }

    public Revenue getRevenue(LocalDate todayDate, int customerId, LocalDate forDate) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Today", FORMATTER.format(todayDate));
        var httpEntity = new HttpEntity<>(httpHeaders);

        String url = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(baseUrl)
                .pathSegment("v2", "customers", String.valueOf(customerId), "revenues", FORMATTER.format(forDate))
                .toUriString();

        try {
            ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, httpEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                var revenue = objectMapper.readValue(response.getBody(), Revenue.class);
                revenue.setCustomerId(customerId);
                revenue.setDate(forDate);
                return revenue;
            } else if (response.getStatusCode().is5xxServerError()) {
                throw new HttpServerErrorException(response.getStatusCode());
            }
        } catch (RestClientResponseException ex) {
            log.warn("Revenue not available on {} for customer {} at {}.", todayDate, customerId, forDate);
        } catch (Exception ex) {
            log.error("Unexpected exception while loading revenue.", ex);
        }

        return Revenue.builder()
                .customerId(customerId)
                .date(forDate)
                .build();
    }

    public boolean charge(LocalDate todayDate, Charge charge) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Today", FORMATTER.format(todayDate));
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        var httpEntity = new HttpEntity<>(charge, httpHeaders);

        String url = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(baseUrl)
                .pathSegment("v2", "mandates", String.valueOf(charge.getAdvance().getMandateId()), "charge")
                .toUriString();

        try {
            ResponseEntity<String> response = client.exchange(url, HttpMethod.POST, httpEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                charge.setDateCharged(todayDate);
                log.info("Charged {}.", charge);
                return true;
            } else if (response.getStatusCode().is5xxServerError()) {
                throw new HttpServerErrorException(response.getStatusCode());
            }
        } catch (RestClientResponseException ex) {
            log.warn("Could not charge for advance {} the amount of {}", charge.getAdvance().getId(), charge.getAmount());
        } catch (Exception ex) {
            log.error("Unexpected exception while charging.", ex);
        }

        return false;
    }

    public boolean reportBillingComplete(LocalDate todayDate, int advanceId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Today", FORMATTER.format(todayDate));
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        var httpEntity = new HttpEntity<>(null, httpHeaders);

        String url = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(baseUrl)
                .pathSegment("v2", "advances", String.valueOf(advanceId), "billing_complete")
                .toUriString();

        try {
            ResponseEntity<String> response = client.exchange(url, HttpMethod.POST, httpEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Billing has been completed for advance {}.", advanceId);
                return true;
            } else {
                log.error("Invalid response received while reporting billing complete: {}", response.getStatusCodeValue());
                return false;
            }
        } catch (Exception ex) {
            log.error("Unexpected exception while reporting billing complete.", ex);
            return false;
        }
    }
}
