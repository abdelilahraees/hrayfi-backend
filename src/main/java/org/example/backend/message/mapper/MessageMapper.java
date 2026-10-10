package org.example.backend.message.mapper;

import org.example.backend.message.dto.MessageDTO;
import org.example.backend.message.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MessageMapper {

    @Mapping(target = "expediteurId",   source = "expediteur.id")
    @Mapping(target = "destinataireId", source = "destinataire.id")
    MessageDTO toDto(Message entity);
}