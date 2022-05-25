package com.wayflyer.billing.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayflyer.billing.model.Advances;
import com.wayflyer.billing.model.Charge;
import com.wayflyer.billing.model.Revenue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static com.wayflyer.billing.testutil.TestConstants.TEST_ADVANCE_ID;
import static com.wayflyer.billing.testutil.TestConstants.TEST_CUSTOMER_ID;
import static com.wayflyer.billing.testutil.TestConstants.TEST_REPAYMENT_START_DATE;
import static com.wayflyer.billing.testutil.TestConstants.TEST_REVENUE_AMOUNT;
import static com.wayflyer.billing.testutil.TestConstants.TEST_STRING;
import static com.wayflyer.billing.testutil.TestConstants.TEST_TODAY_DATE;
import static com.wayflyer.billing.testutil.TestConstants.TEST_URL;
import static com.wayflyer.billing.testutil.TestObjects.getTestAdvances;
import static com.wayflyer.billing.testutil.TestObjects.getTestCharge;
import static com.wayflyer.billing.testutil.TestObjects.getTestRevenue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WayflyerClientTest {
    private static final String URL_ADVANCES = "https://example.com/v2/advances";
    private static final String URL_REVENUES = "https://example.com/v2/customers/1/revenues/2022-01-07";
    private static final String URL_CHARGE = "https://example.com/v2/mandates/102/charge";
    private static final String URL_BILLING_COMPLETE = "https://example.com/v2/advances/1001/billing_complete";

    @Mock
    private RestTemplate restTemplateMock;

    private WayflyerClient classUnderTest;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

    @Captor
    private ArgumentCaptor<HttpMethod> httpMethodCaptor;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        classUnderTest = new WayflyerClient(TEST_URL, restTemplateMock, objectMapper);
    }

    // region getAdvances tests

    private void mockAdvancesCall() throws Exception {
        when(restTemplateMock.exchange(urlCaptor.capture(), httpMethodCaptor.capture(), httpEntityCaptor.capture(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(objectMapper.writeValueAsString(getTestAdvances())));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void givenAValidDate_whenCallingGetAdvances_thenTheCorrectRequestIsMade() throws Exception {
        mockAdvancesCall();

        classUnderTest.getAdvances(TEST_TODAY_DATE);

        assertEquals(URL_ADVANCES, urlCaptor.getValue());
        assertEquals(HttpMethod.GET, httpMethodCaptor.getValue());
        assertEquals(TEST_TODAY_DATE.toString(), httpEntityCaptor.getValue().getHeaders().get("Today").get(0));
    }

    @Test
    public void givenAValidDate_whenCallingGetAdvances_thenAValidResponseIsReturned() throws Exception {
        mockAdvancesCall();

        Advances advances = classUnderTest.getAdvances(TEST_TODAY_DATE);

        assertNotNull(advances);
        assertEquals(1, advances.getAdvances().size());
        assertEquals(TEST_ADVANCE_ID, advances.getAdvances().get(0).getId());
    }

    @Test
    public void givenTheRestCallIsFailing_whenCallingGetAdvances_thenNullIsReturned() {
        when(restTemplateMock.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.internalServerError().body(TEST_STRING));

        Advances advances = classUnderTest.getAdvances(TEST_TODAY_DATE);

        assertNull(advances);
    }

    @Test
    public void givenTheRestCallIsThrowingAnException_whenCallingGetAdvances_thenNullIsReturned() {
        when(restTemplateMock.exchange(anyString(), any(), any(), eq(String.class)))
                .thenThrow(RuntimeException.class);

        Advances advances = classUnderTest.getAdvances(TEST_TODAY_DATE);

        assertNull(advances);
    }

    // endregion

    // region getRevenue tests

    private void mockRevenuesCall() throws Exception {
        when(restTemplateMock.exchange(urlCaptor.capture(), httpMethodCaptor.capture(), httpEntityCaptor.capture(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(objectMapper.writeValueAsString(getTestRevenue())));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void givenAValidDate_whenCallingGetRevenue_thenTheCorrectRequestIsMade() throws Exception {
        mockRevenuesCall();

        classUnderTest.getRevenue(TEST_TODAY_DATE, TEST_CUSTOMER_ID, TEST_REPAYMENT_START_DATE);

        assertEquals(URL_REVENUES, urlCaptor.getValue());
        assertEquals(HttpMethod.GET, httpMethodCaptor.getValue());
        assertEquals(TEST_TODAY_DATE.toString(), httpEntityCaptor.getValue().getHeaders().get("Today").get(0));
    }

    @Test
    public void givenAValidDate_whenCallingGetRevenue_thenAValidResponseIsReturned() throws Exception {
        mockRevenuesCall();

        Revenue revenue = classUnderTest.getRevenue(TEST_TODAY_DATE, TEST_CUSTOMER_ID, TEST_REPAYMENT_START_DATE);

        assertNotNull(revenue);
        assertEquals(TEST_CUSTOMER_ID, revenue.getCustomerId());
        assertEquals(TEST_REPAYMENT_START_DATE, revenue.getDate());
        assertEquals(TEST_REVENUE_AMOUNT, revenue.getAmount());
    }

    @Test
    public void givenTheRestCallReturnsA530_whenCallingGetRevenue_thenARevenueWithANullAmountIsReturned() {
        when(restTemplateMock.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.status(530).body(TEST_STRING));

        Revenue revenue = classUnderTest.getRevenue(TEST_TODAY_DATE, TEST_CUSTOMER_ID, TEST_REPAYMENT_START_DATE);

        assertNotNull(revenue);
        assertEquals(TEST_CUSTOMER_ID, revenue.getCustomerId());
        assertEquals(TEST_REPAYMENT_START_DATE, revenue.getDate());
        assertNull(revenue.getAmount());
    }

    @Test
    public void givenTheRestCallIsFailing_whenCallingGetRevenue_thenARevenueWithANullAmountIsReturned() {
        when(restTemplateMock.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.internalServerError().body(TEST_STRING));

        Revenue revenue = classUnderTest.getRevenue(TEST_TODAY_DATE, TEST_CUSTOMER_ID, TEST_REPAYMENT_START_DATE);

        assertNotNull(revenue);
        assertEquals(TEST_CUSTOMER_ID, revenue.getCustomerId());
        assertEquals(TEST_REPAYMENT_START_DATE, revenue.getDate());
        assertNull(revenue.getAmount());
    }

    @Test
    public void givenTheRestCallIsThrowingAnException_whenCallingGetRevenue_thenARevenueWithANullAmountIsReturned() {
        when(restTemplateMock.exchange(anyString(), any(), any(), eq(String.class)))
                .thenThrow(RuntimeException.class);

        Revenue revenue = classUnderTest.getRevenue(TEST_TODAY_DATE, TEST_CUSTOMER_ID, TEST_REPAYMENT_START_DATE);

        assertNotNull(revenue);
        assertEquals(TEST_CUSTOMER_ID, revenue.getCustomerId());
        assertEquals(TEST_REPAYMENT_START_DATE, revenue.getDate());
        assertNull(revenue.getAmount());
    }

    // endregion

    // region charge tests

    private void mockChargeCall() {
        when(restTemplateMock.exchange(urlCaptor.capture(), httpMethodCaptor.capture(), httpEntityCaptor.capture(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(TEST_STRING));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void givenAValidDate_whenCallingCharge_thenTheCorrectRequestIsMade() {
        mockChargeCall();

        classUnderTest.charge(TEST_TODAY_DATE, getTestCharge());

        assertEquals(URL_CHARGE, urlCaptor.getValue());
        assertEquals(HttpMethod.POST, httpMethodCaptor.getValue());
        assertEquals(TEST_TODAY_DATE.toString(), httpEntityCaptor.getValue().getHeaders().get("Today").get(0));
    }

    @Test
    public void givenAValidDate_whenCallingCharge_thenAValidResponseIsReturned() {
        mockChargeCall();

        boolean result = classUnderTest.charge(TEST_TODAY_DATE, getTestCharge());
        assertTrue(result);
    }

    @Test
    public void givenAValidDate_whenCallingCharge_thenTheDateChargedIsUpdated() {
        mockChargeCall();

        Charge testCharge = getTestCharge();
        assertNull(testCharge.getDateCharged());

        classUnderTest.charge(TEST_TODAY_DATE, testCharge);
        assertEquals(TEST_TODAY_DATE, testCharge.getDateCharged());
    }

    @Test
    public void givenTheRestCallReturnsA530_whenCallingCharge_thenARevenueWithANullAmountIsReturned() {
        when(restTemplateMock.exchange(eq(URL_CHARGE), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.status(530).body(TEST_STRING));

        boolean result = classUnderTest.charge(TEST_TODAY_DATE, getTestCharge());
        assertFalse(result);
    }

    @Test
    public void givenTheRestCallReturnsA530_whenCallingCharge_thenTheDateChargedIsNotUpdated() {
        when(restTemplateMock.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.status(530).body(TEST_STRING));

        Charge testCharge = getTestCharge();
        assertNull(testCharge.getDateCharged());

        classUnderTest.charge(TEST_TODAY_DATE, getTestCharge());

        assertNull(testCharge.getDateCharged());
    }

    @Test
    public void givenTheRestCallIsFailing_whenCallingCharge_thenFalseIsReturned() {
        when(restTemplateMock.exchange(eq(URL_CHARGE), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.internalServerError().body(TEST_STRING));

        boolean result = classUnderTest.charge(TEST_TODAY_DATE, getTestCharge());
        assertFalse(result);
    }

    @Test
    public void givenTheRestCallIsThrowingAnException_whenCallingCharge_thenFalseIsReturned() {
        when(restTemplateMock.exchange(eq(URL_CHARGE), any(), any(), eq(String.class)))
                .thenThrow(RuntimeException.class);

        boolean result = classUnderTest.charge(TEST_TODAY_DATE, getTestCharge());
        assertFalse(result);
    }

    // endregion

    // region reportBillingComplete tests

    private void mockBillingCompleteOkCall() {
        when(restTemplateMock.exchange(urlCaptor.capture(), httpMethodCaptor.capture(), httpEntityCaptor.capture(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(TEST_STRING));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void givenAValidDate_whenCallingReportBillingComplete_thenTheCorrectRequestIsMade() {
        mockBillingCompleteOkCall();

        classUnderTest.reportBillingComplete(TEST_TODAY_DATE, TEST_ADVANCE_ID);

        assertEquals(URL_BILLING_COMPLETE, urlCaptor.getValue());
        assertEquals(HttpMethod.POST, httpMethodCaptor.getValue());
        assertEquals(TEST_TODAY_DATE.toString(), httpEntityCaptor.getValue().getHeaders().get("Today").get(0));
    }

    @Test
    public void givenAValidDate_whenCallingReportBillingComplete_thenTrueIsReturned() {
        mockBillingCompleteOkCall();

        boolean result = classUnderTest.reportBillingComplete(TEST_TODAY_DATE, TEST_ADVANCE_ID);
        assertTrue(result);
    }

    @Test
    public void givenTheRestCallIsFailing_whenCallingReportBillingComplete_thenFalseIsReturned() {
        when(restTemplateMock.exchange(eq(URL_BILLING_COMPLETE), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.internalServerError().body(TEST_STRING));

        boolean result = classUnderTest.reportBillingComplete(TEST_TODAY_DATE, TEST_ADVANCE_ID);
        assertFalse(result);
    }

    @Test
    public void givenTheRestCallIsThrowingAnException_whenCallingReportBillingComplete_thenFalseIsReturned() {
        when(restTemplateMock.exchange(eq(URL_BILLING_COMPLETE), any(), any(), eq(String.class)))
                .thenThrow(RuntimeException.class);

        boolean result = classUnderTest.reportBillingComplete(TEST_TODAY_DATE, TEST_ADVANCE_ID);
        assertFalse(result);
    }

    // endregion
}