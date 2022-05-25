package com.wayflyer.billing;

import com.wayflyer.billing.service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static com.wayflyer.billing.testutil.TestConstants.TEST_END_DATE;
import static com.wayflyer.billing.testutil.TestConstants.TEST_START_DATE;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BillingSimulatorTest {
    @Mock
    private BillingService billingServiceMock;

    private BillingSimulator classUnderTest;

    @Captor
    private ArgumentCaptor<LocalDate> simulationDateCaptor;

    @BeforeEach
    public void setup() {
        classUnderTest = new BillingSimulator(billingServiceMock, TEST_START_DATE, TEST_END_DATE);
    }

    @Test
    void givenASimulationPeriod_whenCallingSimulate_thenTheCorrectDatesAreSimulated() {
        classUnderTest.simulate();

        int expectedDays = (int) DAYS.between(TEST_START_DATE, TEST_END_DATE);
        verify(billingServiceMock, times(expectedDays)).process(simulationDateCaptor.capture());

        assertEquals(TEST_START_DATE, simulationDateCaptor.getAllValues().get(0));
        assertEquals(TEST_END_DATE.minusDays(1), simulationDateCaptor.getAllValues().get(simulationDateCaptor.getAllValues().size() - 1));
    }
}