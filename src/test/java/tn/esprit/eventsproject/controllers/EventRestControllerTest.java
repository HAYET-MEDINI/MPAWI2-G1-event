
package tn.esprit.eventsproject.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.services.IEventServices;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventRestControllerTest {

    @Mock
    private IEventServices eventServices;

    @InjectMocks
    private EventRestController eventRestController;

    @Test
    void testAddParticipant() {
        Participant participant = mock(Participant.class);

        when(eventServices.addParticipant(participant))
                .thenReturn(participant);

        Participant result =
                eventRestController.addParticipant(participant);

        assertSame(participant, result);
        verify(eventServices).addParticipant(participant);
    }

    @Test
    void testAddEventPart() {
        Event event = mock(Event.class);
        int idParticipant = 1;

        when(eventServices.addAffectEvenParticipant(event, idParticipant))
                .thenReturn(event);

        Event result =
                eventRestController.addEventPart(event, idParticipant);

        assertSame(event, result);
        verify(eventServices)
                .addAffectEvenParticipant(event, idParticipant);
    }

    @Test
    void testAddEvent() {
        Event event = mock(Event.class);

        when(eventServices.addAffectEvenParticipant(event))
                .thenReturn(event);

        Event result = eventRestController.addEvent(event);

        assertSame(event, result);
        verify(eventServices).addAffectEvenParticipant(event);
    }

    @Test
    void testAddAffectLog() {
        Logistics logistics = mock(Logistics.class);
        String description = "Concert";

        when(eventServices.addAffectLog(logistics, description))
                .thenReturn(logistics);

        Logistics result =
                eventRestController.addAffectLog(logistics, description);

        assertSame(logistics, result);
        verify(eventServices).addAffectLog(logistics, description);
    }

    @Test
    void testGetLogistiquesDates() {
        LocalDate dateDebut = LocalDate.of(2026, 1, 1);
        LocalDate dateFin = LocalDate.of(2026, 12, 31);

        when(eventServices.getLogisticsDates(dateDebut, dateFin))
                .thenReturn(Collections.emptyList());

        var result = eventRestController.getLogistiquesDates(
                dateDebut, dateFin);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(eventServices).getLogisticsDates(dateDebut, dateFin);
    }
}
