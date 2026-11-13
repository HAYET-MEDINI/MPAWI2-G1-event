package tn.esprit.eventsproject.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.services.IEventServices;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventRestController.class)
class EventRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IEventServices eventServices;

    @Test
    void addParticipantDelegatesToServiceAndReturnsParticipant() throws Exception {
        Participant participant = new Participant();
        participant.setIdPart(4);
        participant.setNom("Doe");
        participant.setPrenom("Jane");
        when(eventServices.addParticipant(any(Participant.class))).thenReturn(participant);

        mockMvc.perform(post("/event/addPart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Doe\",\"prenom\":\"Jane\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPart").value(4))
                .andExpect(jsonPath("$.nom").value("Doe"));

        verify(eventServices).addParticipant(any(Participant.class));
    }

    @Test
    void addEventForParticipantPassesParticipantIdToService() throws Exception {
        Event event = new Event();
        event.setDescription("Conference");
        when(eventServices.addAffectEvenParticipant(any(Event.class), eq(7))).thenReturn(event);

        mockMvc.perform(post("/event/addEvent/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Conference\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Conference"));

        verify(eventServices).addAffectEvenParticipant(any(Event.class), eq(7));
    }

    @Test
    void addEventDelegatesEventToService() throws Exception {
        Event event = new Event();
        event.setDescription("Conference");
        when(eventServices.addAffectEvenParticipant(any(Event.class))).thenReturn(event);

        mockMvc.perform(post("/event/addEvent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Conference\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Conference"));

        verify(eventServices).addAffectEvenParticipant(any(Event.class));
    }

    @Test
    void addLogisticsPassesEventDescriptionToService() throws Exception {
        Logistics logistics = new Logistics(2, "Projector", true, 25.0f, 1);
        when(eventServices.addAffectLog(any(Logistics.class), eq("Conference"))).thenReturn(logistics);

        mockMvc.perform(put("/event/addAffectLog/Conference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Projector\",\"reserve\":true,\"prixUnit\":25.0,\"quantite\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Projector"))
                .andExpect(jsonPath("$.reserve").value(true));

        verify(eventServices).addAffectLog(any(Logistics.class), eq("Conference"));
    }

    @Test
    void getLogisticsDatesPassesDateRangeToService() throws Exception {
        LocalDate start = LocalDate.of(2026, 11, 10);
        LocalDate end = LocalDate.of(2026, 11, 12);
        Logistics logistics = new Logistics(2, "Projector", true, 25.0f, 1);
        when(eventServices.getLogisticsDates(start, end)).thenReturn(Collections.singletonList(logistics));

        mockMvc.perform(get("/event/getLogs/2026-11-10/2026-11-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Projector"))
                .andExpect(jsonPath("$[0].reserve").value(true));

        verify(eventServices).getLogisticsDates(start, end);
    }
}