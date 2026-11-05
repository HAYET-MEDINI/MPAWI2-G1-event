
package tn.esprit.eventsproject.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        Event event = new Event();

        event.setIdEvent(1);
        event.setDescription("Conference");
        event.setDateDebut(LocalDate.of(2026, 11, 10));
        event.setDateFin(LocalDate.of(2026, 11, 12));
        event.setCout(150.5f);

        assertEquals(1, event.getIdEvent());
        assertEquals("Conference", event.getDescription());
        assertEquals(LocalDate.of(2026, 11, 10), event.getDateDebut());
        assertEquals(LocalDate.of(2026, 11, 12), event.getDateFin());
        assertEquals(150.5f, event.getCout(), 0.001f);
    }

    @Test
    void testAllArgsConstructor() {
        LocalDate debut = LocalDate.of(2026, 11, 10);
        LocalDate fin = LocalDate.of(2026, 11, 12);
        Set<Participant> participants = new HashSet<>();
        Set<Logistics> logistics = new HashSet<>();

        Event event = new Event(
                1, "Conference", debut, fin, 150.5f,
                participants, logistics
        );

        assertEquals(1, event.getIdEvent());
        assertEquals("Conference", event.getDescription());
        assertEquals(debut, event.getDateDebut());
        assertEquals(fin, event.getDateFin());
        assertEquals(150.5f, event.getCout(), 0.001f);
        assertSame(participants, event.getParticipants());
        assertSame(logistics, event.getLogistics());
    }

    @Test
    void testSetParticipantsAndLogistics() {
        Event event = new Event();

        Set<Participant> participants = new HashSet<>();
        Set<Logistics> logistics = new HashSet<>();

        event.setParticipants(participants);
        event.setLogistics(logistics);

        assertSame(participants, event.getParticipants());
        assertSame(logistics, event.getLogistics());
    }
}
