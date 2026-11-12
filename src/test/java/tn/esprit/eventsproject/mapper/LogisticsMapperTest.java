package tn.esprit.eventsproject.mapper;

import org.junit.jupiter.api.Test;
import tn.esprit.eventsproject.dto.LogisticsDTO;
import tn.esprit.eventsproject.entities.Logistics;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogisticsMapperTest {

    @Test
    void testMapperEntityToDtoAndBack() {
        LogisticsMapper mapper = new LogisticsMapper();
        Logistics logistics = new Logistics(1, "Chaises", true, 12.5f, 50);

        LogisticsDTO dto = mapper.toDto(logistics);
        assertNotNull(dto);
        assertEquals(logistics.getIdLog(), dto.getIdLog());
        assertEquals(logistics.getDescription(), dto.getDescription());
        assertTrue(dto.isReserve());

        Logistics mapped = mapper.toEntity(dto);
        assertNotNull(mapped);
        assertEquals(logistics.getIdLog(), mapped.getIdLog());
        assertEquals(logistics.getDescription(), mapped.getDescription());
        assertEquals(logistics.getPrixUnit(), mapped.getPrixUnit(), 0.001f);
        assertEquals(logistics.getQuantite(), mapped.getQuantite());
    }

    @Test
    void testMapperListConversion() {
        LogisticsMapper mapper = new LogisticsMapper();
        List<Logistics> logisticsList = List.of(
                new Logistics(1, "Chaises", true, 12.5f, 50),
                new Logistics(2, "Tables", false, 25.0f, 10)
        );

        List<LogisticsDTO> dtoList = mapper.toDtoList(logisticsList);
        assertEquals(2, dtoList.size());
        assertEquals("Chaises", dtoList.get(0).getDescription());
        assertEquals("Tables", dtoList.get(1).getDescription());

        List<Logistics> mappedList = mapper.toEntityList(dtoList);
        assertEquals(2, mappedList.size());
        assertEquals(1, mappedList.get(0).getIdLog());
        assertEquals(2, mappedList.get(1).getIdLog());
    }
}
