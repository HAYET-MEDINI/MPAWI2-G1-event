
package tn.esprit.eventsproject.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.services.IEventServices;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventRestControllerTest {

    @Mock
    private IEventServices eventServices;

    @InjectMocks
    private EventRestController controller;

    private Participant participant;
    private Event event;
    private Logistics logistics;

    @BeforeEach
    void setUp() {
        participant = new Participant();
        event = new Event();
        logistics = new Logistics();
    }

    @Test
    void testAddParticipant() {
        when(eventServices.addParticipant(participant))
                .thenReturn(participant);

        Participant result = controller.addParticipant(participant);

        assertSame(participant, result);
        verify(eventServices).addParticipant(participant);
    }

    @Test
    void testAddEventPart() {
        int idParticipant = 1;

        when(eventServices.addAffectEvenParticipant(event, idParticipant))
                .thenReturn(event);

        Event result = controller.addEventPart(event, idParticipant);

        assertSame(event, result);
        verify(eventServices)
                .addAffectEvenParticipant(event, idParticipant);
    }

    @Test
    void testAddEvent() {
        when(eventServices.addAffectEvenParticipant(event))
                .thenReturn(event);

        Event result = controller.addEvent(event);

        assertSame(event, result);
        verify(eventServices).addAffectEvenParticipant(event);
    }

    @Test
    void testAddAffectLog() {
        String description = "Conference";

        when(eventServices.addAffectLog(logistics, description))
                .thenReturn(logistics);

        Logistics result = controller.addAffectLog(logistics, description);

        assertSame(logistics, result);
        verify(eventServices).addAffectLog(logistics, description);
    }

    @Test
    void testGetLogistiquesDates() {
        LocalDate dateDebut = LocalDate.of(2026, 1, 1);
        LocalDate dateFin = LocalDate.of(2026, 12, 31);

        List<Logistics> expected = new ArrayList<>();
        expected.add(logistics);

        when(eventServices.getLogisticsDates(dateDebut, dateFin))
                .thenReturn(expected);

        List<Logistics> result =
                controller.getLogistiquesDates(dateDebut, dateFin);

        assertSame(expected, result);
        verify(eventServices).getLogisticsDates(dateDebut, dateFin);
    }
}
