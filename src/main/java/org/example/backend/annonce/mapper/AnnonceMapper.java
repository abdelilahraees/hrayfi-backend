package org.example.backend.annonce.mapper;

import org.example.backend.annonce.dto.AnnonceDTO;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.annonce.entity.AnnonceImage;
import org.example.backend.user.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper
public interface AnnonceMapper {

    @Mapping(target = "artisanId",         source = "artisan.id")
    @Mapping(target = "artisanNomComplet", source = "artisan", qualifiedByName = "fullName")
    @Mapping(target = "noteMoyenne",       source = "noteMoyenne", qualifiedByName = "toDouble")
    AnnonceDTO toDto(Annonce entity);

    AnnonceDTO.ImageDTO toDto(AnnonceImage image);

    @Named("fullName")
    default String fullName(AppUser user) {
        if (user == null) return null;
        return (user.getPrenom() + " " + user.getNom()).trim();
    }

    @Named("toDouble")
    default double toDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }
}
