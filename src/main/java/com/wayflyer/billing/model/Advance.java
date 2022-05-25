package com.wayflyer.billing.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Advance {
    @JsonProperty("id")
    private int id;

    @JsonProperty("customer_id")
    private int customerId;

    @JsonProperty("created")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate created;

    @JsonProperty("total_advanced")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalAdvanced;

    @JsonProperty("fee")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal fee;

    @JsonProperty("mandate_id")
    private int mandateId;

    @JsonProperty("repayment_start_date")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate repaymentStartDate;

    @JsonProperty("repayment_percentage")
    private BigDecimal repaymentPercentage;

    @JsonIgnore
    private boolean completed;

    @JsonIgnore
    @Setter(AccessLevel.PRIVATE)
    private BigDecimal outstandingAmount;

    @JsonIgnore
    public BigDecimal getOutstandingAmount() {
        if (outstandingAmount == null) {
            outstandingAmount = totalAdvanced.add(fee);
        }

        return outstandingAmount;
    }

    @JsonIgnore
    @Builder.Default
    private List<Charge> chargesApplied = new ArrayList<>();

    public void applyCharge(Charge charge) {
        setOutstandingAmount(getOutstandingAmount().subtract(charge.getAmount()));
        getChargesApplied().add(charge);
    }

    @JsonIgnore
    public BigDecimal getAmountChargedOnDate(LocalDate date) {
        return getChargesApplied()
                .stream()
                .filter(charge -> charge.getDateCharged().equals(date))
                .map(Charge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        return String.format("%s: %s", getId(), getOutstandingAmount());
    }
}
