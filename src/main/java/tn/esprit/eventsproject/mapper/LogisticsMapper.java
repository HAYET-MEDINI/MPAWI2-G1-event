package tn.esprit.eventsproject.mapper;

import tn.esprit.eventsproject.dto.LogisticsDTO;
import tn.esprit.eventsproject.entities.Logistics;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LogisticsMapper {

    public LogisticsDTO toDto(Logistics logistics) {
        return LogisticsDTO.fromEntity(logistics);
    }

    public Logistics toEntity(LogisticsDTO logisticsDTO) {
        return logisticsDTO == null ? null : logisticsDTO.toEntity();
    }

    public List<LogisticsDTO> toDtoList(List<Logistics> logisticsList) {
        if (logisticsList == null || logisticsList.isEmpty()) {
            return Collections.emptyList();
        }
        return logisticsList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<Logistics> toEntityList(List<LogisticsDTO> logisticsDTOList) {
        if (logisticsDTOList == null || logisticsDTOList.isEmpty()) {
            return Collections.emptyList();
        }
        return logisticsDTOList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
