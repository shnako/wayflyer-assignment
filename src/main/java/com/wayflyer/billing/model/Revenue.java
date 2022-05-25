package com.wayflyer.billing.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Revenue {
    private Integer customerId;

    private LocalDate date;

    @JsonProperty("amount")
    @JsonFormat(shape= JsonFormat.Shape.STRING)
    private BigDecimal amount;
}
