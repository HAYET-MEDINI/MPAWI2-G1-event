package tn.esprit.eventsproject.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.controllers.EventRestController;
import tn.esprit.eventsproject.services.IEventServices;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventControllerTest {

    private IEventServices eventServices;
    private EventRestController eventController;

    @BeforeEach
    void setUp() {
        eventServices = mock(IEventServices.class);
        eventController = new EventRestController(eventServices);
    }

    @Test
    void addParticipantDelegatesToService() {
        Participant participant = new Participant();
        Participant savedParticipant = new Participant();
        when(eventServices.addParticipant(participant)).thenReturn(savedParticipant);

        assertSame(savedParticipant, eventController.addParticipant(participant));
        verify(eventServices).addParticipant(participant);
    }

    @Test
    void addEventForParticipantDelegatesToService() {
        Event event = new Event();
        Event savedEvent = new Event();
        when(eventServices.addAffectEvenParticipant(event, 7)).thenReturn(savedEvent);

        assertSame(savedEvent, eventController.addEventPart(event, 7));
        verify(eventServices).addAffectEvenParticipant(event, 7);
    }

    @Test
    void addEventDelegatesToService() {
        Event event = new Event();
        Event savedEvent = new Event();
        when(eventServices.addAffectEvenParticipant(event)).thenReturn(savedEvent);

        assertSame(savedEvent, eventController.addEvent(event));
        verify(eventServices).addAffectEvenParticipant(event);
    }

    @Test
    void addLogisticsDelegatesToService() {
        Logistics logistics = new Logistics();
        Logistics savedLogistics = new Logistics();
        when(eventServices.addAffectLog(logistics, "Conference")).thenReturn(savedLogistics);

        assertSame(savedLogistics, eventController.addAffectLog(logistics, "Conference"));
        verify(eventServices).addAffectLog(logistics, "Conference");
    }

    @Test
    void getLogisticsForDatesDelegatesToService() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);
        when(eventServices.getLogisticsDates(start, end)).thenReturn(Collections.emptyList());

        assertSame(Collections.emptyList(), eventController.getLogistiquesDates(start, end));
        verify(eventServices).getLogisticsDates(start, end);
    }
}
