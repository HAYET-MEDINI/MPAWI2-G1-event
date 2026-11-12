
package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;

import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServicesImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @InjectMocks
    private EventServicesImpl eventServices;

    // 1. Tester addParticipant
    @Test
    void testAddParticipant() {
        Participant participant = mock(Participant.class);

        when(participantRepository.save(participant))
                .thenReturn(participant);

        Participant result = eventServices.addParticipant(participant);

        assertNotNull(result);
        assertEquals(participant, result);

        verify(participantRepository).save(participant);
    }

    // 2. Tester affectation d'un participant
    // Cas où events est null
    @Test
    void testAddAffectEvenParticipantWithNullEvents() {
        Event event = mock(Event.class);
        Participant participant = mock(Participant.class);

        when(participantRepository.findById(1))
                .thenReturn(Optional.of(participant));

        when(participant.getEvents()).thenReturn(null);
        when(eventRepository.save(event)).thenReturn(event);

        Event result = eventServices.addAffectEvenParticipant(event, 1);

        assertEquals(event, result);

        verify(participant).setEvents(anySet());
        verify(eventRepository).save(event);
    }

    // Cas où events existe déjà
    @Test
    void testAddAffectEvenParticipantWithExistingEvents() {
        Event event = mock(Event.class);
        Participant participant = mock(Participant.class);

        Set<Event> events = new HashSet<>();

        when(participantRepository.findById(1))
                .thenReturn(Optional.of(participant));

        when(participant.getEvents()).thenReturn(events);
        when(eventRepository.save(event)).thenReturn(event);

        Event result = eventServices.addAffectEvenParticipant(event, 1);

        assertEquals(event, result);
        assertTrue(events.contains(event));

        verify(eventRepository).save(event);
    }

    // 3. Tester l'affectation de plusieurs participants
    @Test
    void testAddAffectEvenParticipantForMultipleParticipants() {
        Event event = mock(Event.class);
        Participant participant = mock(Participant.class);

        Set<Participant> participants = new HashSet<>();
        participants.add(participant);

        when(event.getParticipants()).thenReturn(participants);
        when(participant.getIdPart()).thenReturn(1);
        when(participantRepository.findById(1))
                .thenReturn(Optional.of(participant));
        when(participant.getEvents()).thenReturn(null);
        when(eventRepository.save(event)).thenReturn(event);

        Event result = eventServices.addAffectEvenParticipant(event);

        assertEquals(event, result);

        verify(participant).setEvents(anySet());
        verify(eventRepository).save(event);
    }

    // 4. Tester addAffectLog
    // Cas où logistics est null
    @Test
    void testAddAffectLogWithNullLogistics() {
        Logistics logistics = mock(Logistics.class);
        Event event = mock(Event.class);

        when(eventRepository.findByDescription("Concert"))
                .thenReturn(event);
        when(event.getLogistics()).thenReturn(null);
        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        Logistics result = eventServices.addAffectLog(logistics, "Concert");

        assertEquals(logistics, result);

        verify(event).setLogistics(anySet());
        verify(eventRepository).save(event);
        verify(logisticsRepository).save(logistics);
    }

    // Cas où logistics existe déjà
    @Test
    void testAddAffectLogWithExistingLogistics() {
        Logistics logistics = mock(Logistics.class);
        Event event = mock(Event.class);

        Set<Logistics> logisticsSet = new HashSet<>();

        when(eventRepository.findByDescription("Concert"))
                .thenReturn(event);
        when(event.getLogistics()).thenReturn(logisticsSet);
        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        Logistics result = eventServices.addAffectLog(logistics, "Concert");

        assertEquals(logistics, result);
        assertTrue(logisticsSet.contains(logistics));

        verify(eventRepository, never()).save(event);
        verify(logisticsRepository).save(logistics);
    }

    // 5. Tester getLogisticsDates
    // Cas où les événements n'ont pas de logistique
    @Test
    void testGetLogisticsDatesWithEmptyLogistics() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);

        Event event = mock(Event.class);

        when(eventRepository.findByDateDebutBetween(start, end))
                .thenReturn(Collections.singletonList(event));

        when(event.getLogistics()).thenReturn(new HashSet<>());

        List<Logistics> result =
                eventServices.getLogisticsDates(start, end);

        assertNull(result);
    }

    // Cas avec logistique réservée
    @Test
    void testGetLogisticsDatesWithReservedLogistics() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);

        Event event = mock(Event.class);
        Logistics reserved = mock(Logistics.class);
        Logistics notReserved = mock(Logistics.class);

        Set<Logistics> logistics = new HashSet<>();
        logistics.add(reserved);
        logistics.add(notReserved);

        when(eventRepository.findByDateDebutBetween(start, end))
                .thenReturn(Collections.singletonList(event));

        when(event.getLogistics()).thenReturn(logistics);
        when(reserved.isReserve()).thenReturn(true);
        when(notReserved.isReserve()).thenReturn(false);

        List<Logistics> result =
                eventServices.getLogisticsDates(start, end);

        assertEquals(1, result.size());
        assertTrue(result.contains(reserved));
        assertFalse(result.contains(notReserved));
    }

    // 6. Tester calculCout
    @Test
    void testCalculCout() {
        Event event = mock(Event.class);
        Logistics reserved = mock(Logistics.class);
        Logistics notReserved = mock(Logistics.class);

        Set<Logistics> logistics = new HashSet<>();
        logistics.add(reserved);
        logistics.add(notReserved);

        when(eventRepository
                .findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                        "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(Collections.singletonList(event));

        when(event.getLogistics()).thenReturn(logistics);
        when(event.getDescription()).thenReturn("Concert");

        when(reserved.isReserve()).thenReturn(true);
        when(reserved.getPrixUnit()).thenReturn(10f);
        when(reserved.getQuantite()).thenReturn(2);

        when(notReserved.isReserve()).thenReturn(false);

        eventServices.calculCout();

        verify(event).setCout(20f);
        verify(eventRepository).save(event);
    }

    // Aucun événement à calculer
    @Test
    void testCalculCoutWithoutEvents() {
        when(eventRepository
                .findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                        "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> eventServices.calculCout());

        verify(eventRepository, never()).save(any(Event.class));
    }
}
