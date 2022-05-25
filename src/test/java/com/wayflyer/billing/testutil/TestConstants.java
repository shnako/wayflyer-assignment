package com.wayflyer.billing.testutil;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TestConstants {
    public static final int TEST_ADVANCE_ID = 1001;
    public static final BigDecimal TEST_CHARGE_AMOUNT = new BigDecimal("1234.56");
    public static final LocalDate TEST_CREATED_DATE = LocalDate.of(2022, 1, 2);
    public static final int TEST_CUSTOMER_ID = 1;
    public static final BigDecimal TEST_FEE = new BigDecimal("2000.00");
    public static final int TEST_MANDATE_ID = 102;
    public static final BigDecimal TEST_REPAYMENT_PERCENTAGE = new BigDecimal("11");
    public static final LocalDate TEST_REPAYMENT_START_DATE = LocalDate.of(2022, 1, 7);
    public static final BigDecimal TEST_REVENUE_AMOUNT = new BigDecimal("1234.56");
    public static final LocalDate TEST_TODAY_DATE = LocalDate.of(2022, 1, 8);
    public static final BigDecimal TEST_TOTAL_ADVANCED = new BigDecimal("60000.00");

    public static final String TEST_STRING = "TEST";

    public static final LocalDate TEST_START_DATE = LocalDate.of(2022, 1, 1);
    public static final LocalDate TEST_END_DATE = LocalDate.of(2022, 1, 10);
    public static final String TEST_URL = "example.com";

    private TestConstants() {
    }
}
