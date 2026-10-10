package org.example.backend.evaluation.mapper;

import org.example.backend.evaluation.dto.EvaluationDTO;
import org.example.backend.evaluation.entity.Evaluation;
import org.example.backend.user.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface EvaluationMapper {

    @Mapping(target = "demandeId",        source = "demande.id")
    @Mapping(target = "clientId",         source = "client.id")
    @Mapping(target = "clientNomComplet", source = "client", qualifiedByName = "fullName")
    @Mapping(target = "artisanId",        source = "artisan.id")
    EvaluationDTO toDto(Evaluation evaluation);

    @Named("fullName")
    default String fullName(AppUser user) {
        if (user == null) return null;
        return (user.getPrenom() + " " + user.getNom()).trim();
    }
}