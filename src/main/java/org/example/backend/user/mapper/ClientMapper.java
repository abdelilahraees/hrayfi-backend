package org.example.backend.user.mapper;

import org.example.backend.user.dto.ClientDTO;
import org.example.backend.user.entity.Client;
import org.mapstruct.Mapper;

@Mapper
public interface ClientMapper {
    ClientDTO toDto(Client client);
}