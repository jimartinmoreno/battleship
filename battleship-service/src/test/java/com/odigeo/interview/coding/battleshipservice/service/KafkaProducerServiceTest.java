package com.odigeo.interview.coding.battleshipservice.service;

import com.odigeo.interview.coding.battleshipapi.event.GameCreatedEvent;
import com.odigeo.interview.coding.battleshipapi.event.GameFireEvent;
import com.odigeo.interview.coding.battleshipservice.exception.KafkaProducerException;
import fish.payara.cloud.connectors.kafka.api.KafkaConnection;
import fish.payara.cloud.connectors.kafka.api.KafkaConnectionFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.resource.ResourceException;

public class KafkaProducerServiceTest {

    @InjectMocks
    KafkaProducerService kafkaProducerService;

    @Mock
    KafkaConnectionFactory kafkaConnectionFactory;

    @Mock
    KafkaConnection kafkaConnection;


    @BeforeClass
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterMethod
    public void tearDown() {
        reset(kafkaConnection);
        reset(kafkaConnectionFactory);
    }

    @Test()
    public void testGameCreatedEvent() throws ResourceException {
        when(kafkaConnectionFactory.createConnection()).thenReturn(kafkaConnection);
        kafkaProducerService.setFactory(kafkaConnectionFactory);
        kafkaProducerService.publish(new GameCreatedEvent());
        verify(kafkaConnection, Mockito.times(1)).send(Mockito.any());
    }

    @Test()
    public void testGameFireEvent() throws ResourceException {
        when(kafkaConnectionFactory.createConnection()).thenReturn(kafkaConnection);
        kafkaProducerService.setFactory(kafkaConnectionFactory);
        kafkaProducerService.publish(new GameFireEvent());
        verify(kafkaConnection, Mockito.times(1)).send(Mockito.any());
    }


    @Test(expectedExceptions = KafkaProducerException.class)
    public void testGameCreatedEventException() throws ResourceException {
        when(kafkaConnectionFactory.createConnection()).thenThrow(new ResourceException("not connected"));
        kafkaProducerService.setFactory(kafkaConnectionFactory);
        kafkaProducerService.publish(new GameCreatedEvent());
    }

    @Test(expectedExceptions = KafkaProducerException.class)
    public void testGameFireEventException() throws ResourceException {
        when(kafkaConnectionFactory.createConnection()).thenThrow(new ResourceException("not connected"));
        kafkaProducerService.setFactory(kafkaConnectionFactory);
        kafkaProducerService.publish(new GameFireEvent());
    }
}
