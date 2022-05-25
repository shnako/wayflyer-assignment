package com.wayflyer.billing.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Charge {
    @JsonIgnore
    private Advance advance;

    @JsonIgnore
    private LocalDate dateFor;

    @JsonIgnore
    private LocalDate dateCharged;

    @JsonProperty("amount")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amount;

    @Override
    public String toString() {
        if (dateCharged != null) {
            return String.format("%s for advance %d, for %s, on %s", amount, advance.getId(), dateFor, dateCharged);
        } else {
            return String.format("%s for advance %d, for %s, not yet charged", amount, advance.getId(), dateFor);
        }
    }
}
