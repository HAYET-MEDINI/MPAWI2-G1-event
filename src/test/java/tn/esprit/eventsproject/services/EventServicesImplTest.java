
package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.BeforeEach;
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

    private Participant participant;
    private Event event;
    private Logistics logistics;

    @BeforeEach
    void setUp() {
        participant = new Participant();
        participant.setIdPart(1);
        participant.setNom("Tounsi");
        participant.setPrenom("Ahmed");

        event = new Event();
        event.setIdEvent(1);
        event.setDescription("Conference");
        event.setDateDebut(LocalDate.of(2026, 5, 10));
        event.setDateFin(LocalDate.of(2026, 5, 11));

        logistics = new Logistics();
        logistics.setIdLog(1);
        logistics.setDescription("Projecteur");
        logistics.setReserve(true);
        logistics.setPrixUnit(100);
        logistics.setQuantite(2);
    }

    // 1. Ajouter un participant
    @Test
    void testAddParticipant() {
        when(participantRepository.save(participant))
                .thenReturn(participant);

        Participant result = eventServices.addParticipant(participant);

        assertSame(participant, result);
        verify(participantRepository).save(participant);
    }

    // 2. Affecter un événement à un participant
    @Test
    void testAddAffectEvenParticipantById() {
        when(participantRepository.findById(1))
                .thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        Event result =
                eventServices.addAffectEvenParticipant(event, 1);

        assertSame(event, result);
        assertNotNull(participant.getEvents());
        assertTrue(participant.getEvents().contains(event));
        verify(eventRepository).save(event);
    }

    // 3. Affecter un événement à plusieurs participants
    @Test
    void testAddAffectEvenParticipant() {
        Set<Participant> participants = new HashSet<>();
        participants.add(participant);
        event.setParticipants(participants);

        when(participantRepository.findById(1))
                .thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        Event result = eventServices.addAffectEvenParticipant(event);

        assertSame(event, result);
        assertNotNull(participant.getEvents());
        assertTrue(participant.getEvents().contains(event));
        verify(eventRepository).save(event);
    }

    // 4. Affecter une logistique à un événement
    @Test
    void testAddAffectLog() {
        when(eventRepository.findByDescription("Conference"))
                .thenReturn(event);
        when(logisticsRepository.save(logistics))
                .thenReturn(logistics);

        Logistics result =
                eventServices.addAffectLog(logistics, "Conference");

        assertSame(logistics, result);
        assertNotNull(event.getLogistics());
        assertTrue(event.getLogistics().contains(logistics));
        verify(logisticsRepository).save(logistics);
        verify(eventRepository).save(event);
    }

    // 5. Récupérer les logistiques réservées entre deux dates
    @Test
    void testGetLogisticsDates() {
        Set<Logistics> logisticsSet = new HashSet<>();
        logisticsSet.add(logistics);
        event.setLogistics(logisticsSet);

        when(eventRepository.findByDateDebutBetween(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)))
                .thenReturn(Collections.singletonList(event));

        List<Logistics> result = eventServices.getLogisticsDates(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(logistics));
    }

    // 6. Aucun événement trouvé
    @Test
    void testGetLogisticsDatesWithoutEvents() {
        when(eventRepository.findByDateDebutBetween(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)))
                .thenReturn(Collections.emptyList());

        List<Logistics> result = eventServices.getLogisticsDates(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // 7. Calculer le coût d'un événement
    @Test
    void testCalculCout() {
        Set<Logistics> logisticsSet = new HashSet<>();
        logisticsSet.add(logistics);
        event.setLogistics(logisticsSet);

        when(eventRepository
                .findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                        "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(Collections.singletonList(event));

        eventServices.calculCout();

        assertEquals(200.0f, event.getCout(), 0.01f);
        verify(eventRepository).save(event);
    }
}
