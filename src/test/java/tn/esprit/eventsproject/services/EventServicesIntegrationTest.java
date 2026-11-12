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
        "spring.datasource.url=jdbc:h2:mem:event-test;DB_CLOSE_DELAY=-1;NON_KEYWORDS=EVENT",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(EventServicesImpl.class)
class EventServicesIntegrationTest {

    @Autowired
    private IEventServices eventServices;

    @Autowired
    private ParticipantRepository participantRepository;

    @Test
    void addParticipantPersistsParticipantUsingRealRepository() {
        Participant participant = new Participant();
        participant.setNom("Tounsi");
        participant.setPrenom("Ahmed");

        Participant saved = eventServices.addParticipant(participant);

        assertTrue(saved.getIdPart() > 0);
        Participant persisted = participantRepository.findById(saved.getIdPart()).orElseThrow(AssertionError::new);
        assertEquals("Tounsi", persisted.getNom());
        assertEquals("Ahmed", persisted.getPrenom());
    }
}