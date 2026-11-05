
package tn.esprit.eventsproject.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TacheTest {

    @Test
    void testAllTasksExist() {
        Tache[] tasks = Tache.values();

        assertEquals(4, tasks.length);
        assertTrue(java.util.Arrays.asList(tasks).contains(Tache.INVITE));
        assertTrue(java.util.Arrays.asList(tasks).contains(Tache.ORGANISATEUR));
        assertTrue(java.util.Arrays.asList(tasks).contains(Tache.SERVEUR));
        assertTrue(java.util.Arrays.asList(tasks).contains(Tache.ANIMATEUR));
    }

    @Test
    void testTaskValueOf() {
        assertEquals(Tache.INVITE, Tache.valueOf("INVITE"));
        assertEquals(Tache.ORGANISATEUR, Tache.valueOf("ORGANISATEUR"));
        assertEquals(Tache.SERVEUR, Tache.valueOf("SERVEUR"));
        assertEquals(Tache.ANIMATEUR, Tache.valueOf("ANIMATEUR"));
    }
}
