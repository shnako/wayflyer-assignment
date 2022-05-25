package com.wayflyer.billing.testutil;

import com.wayflyer.billing.model.Advance;
import com.wayflyer.billing.model.Advances;
import com.wayflyer.billing.model.Charge;
import com.wayflyer.billing.model.Revenue;

import java.util.List;

import static com.wayflyer.billing.testutil.TestConstants.TEST_ADVANCE_ID;
import static com.wayflyer.billing.testutil.TestConstants.TEST_CHARGE_AMOUNT;
import static com.wayflyer.billing.testutil.TestConstants.TEST_CREATED_DATE;
import static com.wayflyer.billing.testutil.TestConstants.TEST_CUSTOMER_ID;
import static com.wayflyer.billing.testutil.TestConstants.TEST_FEE;
import static com.wayflyer.billing.testutil.TestConstants.TEST_MANDATE_ID;
import static com.wayflyer.billing.testutil.TestConstants.TEST_REPAYMENT_PERCENTAGE;
import static com.wayflyer.billing.testutil.TestConstants.TEST_REPAYMENT_START_DATE;
import static com.wayflyer.billing.testutil.TestConstants.TEST_REVENUE_AMOUNT;
import static com.wayflyer.billing.testutil.TestConstants.TEST_TODAY_DATE;
import static com.wayflyer.billing.testutil.TestConstants.TEST_TOTAL_ADVANCED;

public class TestObjects {
    public static Advance getTestAdvance() {
        return Advance.builder()
                .id(TEST_ADVANCE_ID)
                .customerId(TEST_CUSTOMER_ID)
                .created(TEST_CREATED_DATE)
                .totalAdvanced(TEST_TOTAL_ADVANCED)
                .fee(TEST_FEE)
                .mandateId(TEST_MANDATE_ID)
                .repaymentStartDate(TEST_REPAYMENT_START_DATE)
                .repaymentPercentage(TEST_REPAYMENT_PERCENTAGE)
                .build();
    }

    public static Advances getTestAdvances() {
        return Advances.builder()
                .advances(List.of(getTestAdvance()))
                .build();
    }

    public static Revenue getTestRevenue() {
        return Revenue.builder()
                .amount(TEST_REVENUE_AMOUNT)
                .build();
    }

    public static Charge getTestCharge() {
        return Charge.builder()
                .advance(getTestAdvance())
                .amount(TEST_CHARGE_AMOUNT)
                .dateFor(TEST_TODAY_DATE)
                .build();
    }

    private TestObjects() {
    }
}
