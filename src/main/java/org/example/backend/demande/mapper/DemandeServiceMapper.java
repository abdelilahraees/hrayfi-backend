package org.example.backend.demande.mapper;

import org.example.backend.demande.dto.DemandeServiceDTO;
import org.example.backend.demande.entity.DemandeService;
import org.example.backend.user.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface DemandeServiceMapper {

    @Mapping(target = "clientId",         source = "client.id")
    @Mapping(target = "clientNomComplet", source = "client", qualifiedByName = "fullName")
    @Mapping(target = "artisanId",        source = "artisan.id")
    @Mapping(target = "artisanNomComplet", source = "artisan", qualifiedByName = "fullName")
    @Mapping(target = "annonceId",         source = "annonce.id")
    @Mapping(target = "annonceTitre",      source = "annonce.titre")
    DemandeServiceDTO toDto(DemandeService entity);

    @Named("fullName")
    default String fullName(AppUser user) {
        if (user == null) return null;
        return (user.getPrenom() + " " + user.getNom()).trim();
    }
}
