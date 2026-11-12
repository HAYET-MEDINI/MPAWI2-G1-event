package tn.esprit.eventsproject.dto;

import org.junit.jupiter.api.Test;
import tn.esprit.eventsproject.entities.Logistics;

import static org.junit.jupiter.api.Assertions.*;

class LogisticsDtoTest {

    @Test
    void testFromEntityAndToEntity() {
        Logistics logistics = new Logistics(1, "Chaises", true, 12.5f, 50);

        LogisticsDTO dto = LogisticsDTO.fromEntity(logistics);

        assertEquals(1, dto.getIdLog());
        assertEquals("Chaises", dto.getDescription());
        assertTrue(dto.isReserve());
        assertEquals(12.5f, dto.getPrixUnit(), 0.001f);
        assertEquals(50, dto.getQuantite());

        Logistics mapped = dto.toEntity();

        assertEquals(logistics.getIdLog(), mapped.getIdLog());
        assertEquals(logistics.getDescription(), mapped.getDescription());
        assertEquals(logistics.isReserve(), mapped.isReserve());
        assertEquals(logistics.getPrixUnit(), mapped.getPrixUnit(), 0.001f);
        assertEquals(logistics.getQuantite(), mapped.getQuantite());
    }
}
