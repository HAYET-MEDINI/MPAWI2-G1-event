package tn.esprit.eventsproject.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.services.IEventServices;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logistics")
public class LogisticsRestController {
    private final IEventServices eventServices;

    @GetMapping
    public List<Logistics> getAllLogistics() {
        return eventServices.getAllLogistics();
    }

    @GetMapping("/{id}")
    public Logistics getLogisticsById(@PathVariable int id) {
        return eventServices.getLogisticsById(id);
    }

    @PostMapping
    public ResponseEntity<Logistics> createLogistics(@RequestBody Logistics logistics) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventServices.createLogistics(logistics));
    }

    @PutMapping("/{id}")
    public Logistics updateLogistics(
            @PathVariable int id,
            @RequestBody Logistics logistics) {
        return eventServices.updateLogistics(id, logistics);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLogistics(@PathVariable int id) {
        eventServices.deleteLogistics(id);
        return ResponseEntity.noContent().build();
    }
}
