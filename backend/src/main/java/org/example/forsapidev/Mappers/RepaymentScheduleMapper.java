package org.example.forsapidev.Mappers;

import org.example.forsapidev.DTO.CreditRefDTO;
import org.example.forsapidev.DTO.RepaymentScheduleDTO;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.example.forsapidev.entities.CreditManagement.RepaymentSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RepaymentScheduleMapper {

    @Mapping(target = "creditRequest", expression = "java(toCreditRefDTO(entity.getCreditRequest()))")
    RepaymentScheduleDTO toDto(RepaymentSchedule entity);

    default CreditRefDTO toCreditRefDTO(CreditRequest creditRequest) {
        if (creditRequest == null) {
            return null;
        }
        CreditRefDTO dto = new CreditRefDTO();
        dto.setId(creditRequest.getId());
        return dto;
    }
}
