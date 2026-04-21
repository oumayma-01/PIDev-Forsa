package org.example.forsapidev.Mappers;

import org.example.forsapidev.DTO.CreditRequestDTO;
import org.example.forsapidev.DTO.CreditRequestUpdateDTO;
import org.example.forsapidev.DTO.UserRefDTO;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.entities.CreditManagement.CreditRequest;
import org.hibernate.Hibernate;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CreditRequestMapper {

    @Mapping(target = "user", expression = "java(toUserRefDTO(entity.getUser()))")
    CreditRequestDTO toDto(CreditRequest entity);

    default UserRefDTO toUserRefDTO(User user) {
        if (user == null) {
            return null;
        }

        UserRefDTO dto = new UserRefDTO();
        dto.setId(user.getId());

        // Si 'user' est un proxy Hibernate non initialisé, éviter d'accéder à username/email.
        if (Hibernate.isInitialized(user)) {
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
        }

        return dto;
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(CreditRequestUpdateDTO dto, @MappingTarget CreditRequest entity);
}
