
package tn.esprit.eventsproject.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogisticsTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        Logistics logistics = new Logistics();

        logistics.setIdLog(1);
        logistics.setDescription("Chaises");
        logistics.setReserve(true);
        logistics.setPrixUnit(12.5f);
        logistics.setQuantite(50);

        assertEquals(1, logistics.getIdLog());
        assertEquals("Chaises", logistics.getDescription());
        assertTrue(logistics.isReserve());
        assertEquals(12.5f, logistics.getPrixUnit(), 0.001f);
        assertEquals(50, logistics.getQuantite());
    }

    @Test
    void testAllArgsConstructor() {
        Logistics logistics = new Logistics(
                2, "Tables", false, 25.0f, 10
        );

        assertEquals(2, logistics.getIdLog());
        assertEquals("Tables", logistics.getDescription());
        assertFalse(logistics.isReserve());
        assertEquals(25.0f, logistics.getPrixUnit(), 0.001f);
        assertEquals(10, logistics.getQuantite());
    }

    @Test
    void testUpdateReservation() {
        Logistics logistics = new Logistics();

        logistics.setReserve(false);
        assertFalse(logistics.isReserve());

        logistics.setReserve(true);
        assertTrue(logistics.isReserve());
    }
}
