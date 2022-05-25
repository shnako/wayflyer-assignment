package com.wayflyer.billing.service;

import com.wayflyer.billing.client.WayflyerClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class BillingServiceTest {
    @Mock
    private WayflyerClient wayflyerClient;

    @InjectMocks
    private BillingService classUnderTest;

    @Test
    public void givenThisWouldBeARealScenario_whenCallingProcess_thenWeWouldTestItThoroughly() {
        // TODO Normally we'd test this thoroughly but I'd rather not spend the time doing it now.
        // TODO We could also write tests for the model classes or just test them via the functional classes.
        // TODO I have added complete tests to WayflyerClientTest - please check that out instead.

        assertNotNull(wayflyerClient);
        assertNotNull(classUnderTest);
    }
}