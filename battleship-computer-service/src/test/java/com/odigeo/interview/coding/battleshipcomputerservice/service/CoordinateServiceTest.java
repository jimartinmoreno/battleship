package com.odigeo.interview.coding.battleshipcomputerservice.service;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test
public class CoordinateServiceTest {

    @Inject
    private CoordinateService coordinateService;

    @BeforeMethod
    public void beforeClass() {
        coordinateService = new CoordinateService();
    }

    @Test
    public void testRandomCoordinate() {
        String coordinate = coordinateService.randomCoordinate();
        assertNotNull(coordinate);
        assertTrue(coordinate.matches("^([A-Z])(\\d+)$"));
    }

}