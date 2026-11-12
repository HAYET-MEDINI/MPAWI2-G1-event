package tn.esprit.eventsproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.eventsproject.entities.Logistics;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsDTO implements Serializable {
    private int idLog;
    private String description;
    private boolean reserve;
    private float prixUnit;
    private int quantite;

    public LogisticsDTO(Logistics logistics) {
        if (logistics != null) {
            this.idLog = logistics.getIdLog();
            this.description = logistics.getDescription();
            this.reserve = logistics.isReserve();
            this.prixUnit = logistics.getPrixUnit();
            this.quantite = logistics.getQuantite();
        }
    }

    public static LogisticsDTO fromEntity(Logistics logistics) {
        return logistics == null ? null : new LogisticsDTO(logistics);
    }

    public Logistics toEntity() {
        Logistics logistics = new Logistics();
        logistics.setIdLog(this.idLog);
        logistics.setDescription(this.description);
        logistics.setReserve(this.reserve);
        logistics.setPrixUnit(this.prixUnit);
        logistics.setQuantite(this.quantite);
        return logistics;
    }
}
