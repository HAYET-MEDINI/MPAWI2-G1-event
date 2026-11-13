package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventServicesImplIntegrationTest {

    private EventRepository eventRepository;
    private ParticipantRepository participantRepository;
    private LogisticsRepository logisticsRepository;
    private EventServicesImpl eventServices;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        participantRepository = mock(ParticipantRepository.class);
        logisticsRepository = mock(LogisticsRepository.class);
        eventServices = new EventServicesImpl(eventRepository, participantRepository, logisticsRepository);
    }

    @Test
    void associatesEventWithParticipantWithAndWithoutExistingEvents() {
        Event firstEvent = new Event();
        Event secondEvent = new Event();
        Participant participant = new Participant();
        participant.setEvents(null);
        when(participantRepository.findById(3)).thenReturn(Optional.of(participant));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertSame(firstEvent, eventServices.addAffectEvenParticipant(firstEvent, 3));
        assertEquals(Collections.singleton(firstEvent), participant.getEvents());

        assertSame(secondEvent, eventServices.addAffectEvenParticipant(secondEvent, 3));
        assertEquals(new HashSet<>(Arrays.asList(firstEvent, secondEvent)), participant.getEvents());
        verify(eventRepository).save(firstEvent);
        verify(eventRepository).save(secondEvent);
    }

    @Test
    void associatesEventWithEveryParticipant() {
        Event event = new Event();
        Participant participantWithoutEvents = new Participant();
        participantWithoutEvents.setIdPart(1);
        Participant participantWithEvents = new Participant();
        participantWithEvents.setIdPart(2);
        Event existingEvent = new Event();
        participantWithEvents.setEvents(new HashSet<>(Collections.singleton(existingEvent)));
        event.setParticipants(new HashSet<>(Arrays.asList(participantWithoutEvents, participantWithEvents)));
        when(participantRepository.findById(1)).thenReturn(Optional.of(participantWithoutEvents));
        when(participantRepository.findById(2)).thenReturn(Optional.of(participantWithEvents));
        when(eventRepository.save(event)).thenReturn(event);

        assertSame(event, eventServices.addAffectEvenParticipant(event));

        assertEquals(Collections.singleton(event), participantWithoutEvents.getEvents());
        assertEquals(new HashSet<>(Arrays.asList(existingEvent, event)), participantWithEvents.getEvents());
        verify(eventRepository).save(event);
    }

    @Test
    void addsLogisticsAndSavesEventOnlyWhenItHasNoLogisticsYet() {
        Logistics firstLogistics = new Logistics();
        Logistics secondLogistics = new Logistics();
        Event eventWithoutLogistics = new Event();
        eventWithoutLogistics.setLogistics(null);
        Event eventWithLogistics = new Event();
        eventWithLogistics.setLogistics(new HashSet<>(Collections.singleton(firstLogistics)));
        when(eventRepository.findByDescription("First")).thenReturn(eventWithoutLogistics);
        when(eventRepository.findByDescription("Second")).thenReturn(eventWithLogistics);
        when(logisticsRepository.save(any(Logistics.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertSame(firstLogistics, eventServices.addAffectLog(firstLogistics, "First"));
        assertEquals(Collections.singleton(firstLogistics), eventWithoutLogistics.getLogistics());

        assertSame(secondLogistics, eventServices.addAffectLog(secondLogistics, "Second"));
        assertEquals(new HashSet<>(Arrays.asList(firstLogistics, secondLogistics)),
                eventWithLogistics.getLogistics());
        verify(eventRepository).save(eventWithoutLogistics);
        verify(eventRepository, never()).save(eventWithLogistics);
        verify(logisticsRepository).save(firstLogistics);
        verify(logisticsRepository).save(secondLogistics);
    }

    @Test
    void returnsReservedLogisticsWithinDateRange() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 28);
        Logistics reserved = new Logistics();
        reserved.setReserve(true);
        Logistics notReserved = new Logistics();
        notReserved.setReserve(false);
        Event event = new Event();
        event.setLogistics(new HashSet<>(Arrays.asList(reserved, notReserved)));
        when(eventRepository.findByDateDebutBetween(start, end)).thenReturn(Collections.singletonList(event));

        List<Logistics> result = eventServices.getLogisticsDates(start, end);

        assertEquals(Collections.singletonList(reserved), result);
        verify(eventRepository).findByDateDebutBetween(start, end);
    }

    @Test
    void returnsNullWhenAnEventHasNoLogistics() {
        Event event = new Event();
        event.setLogistics(Collections.emptySet());
        when(eventRepository.findByDateDebutBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(event));

        assertNull(eventServices.getLogisticsDates(LocalDate.MIN, LocalDate.MAX));
    }

    @Test
    void returnsEmptyListWhenNoEventsMatchDates() {
        when(eventRepository.findByDateDebutBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        assertEquals(Collections.emptyList(),
                eventServices.getLogisticsDates(LocalDate.MIN, LocalDate.MAX));
    }

    @Test
    void calculatesCostFromReservedLogisticsAndSavesEvent() {
        Logistics reserved = new Logistics();
        reserved.setReserve(true);
        reserved.setPrixUnit(12.5f);
        reserved.setQuantite(2);
        Logistics notReserved = new Logistics();
        notReserved.setReserve(false);
        notReserved.setPrixUnit(100f);
        notReserved.setQuantite(1);
        Event event = new Event();
        event.setDescription("Workshop");
        event.setLogistics(new HashSet<>(Arrays.asList(reserved, notReserved)));
        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR)).thenReturn(Collections.singletonList(event));

        eventServices.calculCout();

        assertEquals(25f, event.getCout());
        verify(eventRepository).save(event);
    }

    @Test
    void doesNotSaveWhenThereAreNoEventsToCalculate() {
        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR)).thenReturn(new ArrayList<>());

        eventServices.calculCout();

        verify(eventRepository, never()).save(any(Event.class));
    }
}
