package org.example.backend.user.mapper;

import org.example.backend.user.dto.ArtisanDTO;
import org.example.backend.user.entity.Artisan;
import org.mapstruct.Mapper;

@Mapper
public interface ArtisanMapper {
    ArtisanDTO toDto(Artisan artisan);
}