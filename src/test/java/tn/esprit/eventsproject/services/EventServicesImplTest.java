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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void addParticipantSavesAndReturnsParticipant() {
        Participant participant = new Participant();
        when(participantRepository.save(participant)).thenReturn(participant);

        Participant result = eventServices.addParticipant(participant);

        assertSame(participant, result);
        verify(participantRepository).save(participant);
    }

    @Test
    void getLogisticsDatesReturnsOnlyReservedLogistics() {
        LocalDate start = LocalDate.of(2026, 11, 10);
        LocalDate end = LocalDate.of(2026, 11, 12);
        Logistics reserved = new Logistics(1, "Projector", true, 20.0f, 2);
        Logistics notReserved = new Logistics(2, "Chairs", false, 5.0f, 10);
        Event event = new Event();
        event.setLogistics(new HashSet<>(Arrays.asList(reserved, notReserved)));
        when(eventRepository.findByDateDebutBetween(start, end)).thenReturn(Collections.singletonList(event));

        List<Logistics> result = eventServices.getLogisticsDates(start, end);

        assertEquals(Collections.singletonList(reserved), result);
        verify(eventRepository).findByDateDebutBetween(start, end);
    }

    @Test
    void calculCoutSumsReservedLogisticsAndPersistsEvent() {
        Logistics reserved = new Logistics(1, "Projector", true, 10.0f, 2);
        Logistics notReserved = new Logistics(2, "Chairs", false, 5.0f, 10);
        Event event = new Event();
        event.setDescription("Conference");
        Set<Logistics> logistics = new HashSet<>(Arrays.asList(reserved, notReserved));
        event.setLogistics(logistics);
        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR)).thenReturn(Collections.singletonList(event));

        eventServices.calculCout();

        assertEquals(20.0f, event.getCout(), 0.001f);
        verify(eventRepository).save(event);
    }
}