
package tn.esprit.eventsproject.entities;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        Participant participant = new Participant();

        participant.setIdPart(1);
        participant.setNom("Ben Ali");
        participant.setPrenom("Sami");
        participant.setTache(Tache.ORGANISATEUR);

        assertEquals(1, participant.getIdPart());
        assertEquals("Ben Ali", participant.getNom());
        assertEquals("Sami", participant.getPrenom());
        assertEquals(Tache.ORGANISATEUR, participant.getTache());
    }

    @Test
    void testAllArgsConstructor() {
        Set<Event> events = new HashSet<>();

        Participant participant = new Participant(
                2, "Trabelsi", "Amira",
                Tache.ANIMATEUR, events
        );

        assertEquals(2, participant.getIdPart());
        assertEquals("Trabelsi", participant.getNom());
        assertEquals("Amira", participant.getPrenom());
        assertEquals(Tache.ANIMATEUR, participant.getTache());
        assertSame(events, participant.getEvents());
    }

    @Test
    void testSetEvents() {
        Participant participant = new Participant();
        Set<Event> events = new HashSet<>();

        participant.setEvents(events);

        assertSame(events, participant.getEvents());
    }
}
