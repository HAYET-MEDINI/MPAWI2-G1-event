package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(EventServicesImpl.class)
class EventServicesImplIntegrationTest {

    @Autowired
    private IEventServices eventServices;

    @Autowired
    private ParticipantRepository participantRepository;

    @Test
    void addParticipantPersistsParticipantWithoutMocks() {
        Participant participant = new Participant();
        participant.setNom("Tounsi");
        participant.setPrenom("Ahmed");

        Participant savedParticipant = eventServices.addParticipant(participant);

        assertTrue(participantRepository.findById(savedParticipant.getIdPart()).isPresent());
        assertEquals("Tounsi", savedParticipant.getNom());
        assertEquals("Ahmed", savedParticipant.getPrenom());
    }
}
