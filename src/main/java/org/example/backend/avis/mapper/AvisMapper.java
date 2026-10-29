package org.example.backend.avis.mapper;

import org.example.backend.avis.dto.AvisDTO;
import org.example.backend.avis.entity.AvisAnnonce;
import org.example.backend.user.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface AvisMapper {

    @Mapping(target = "annonceId",        source = "annonce.id")
    @Mapping(target = "annonceTitre",     source = "annonce.titre")
    @Mapping(target = "clientId",         source = "client.id")
    @Mapping(target = "clientNomComplet", source = "client", qualifiedByName = "fullName")
    AvisDTO toDto(AvisAnnonce entity);

    @Named("fullName")
    default String fullName(AppUser user) {
        if (user == null) return null;
        return (user.getPrenom() + " " + user.getNom()).trim();
    }
}
