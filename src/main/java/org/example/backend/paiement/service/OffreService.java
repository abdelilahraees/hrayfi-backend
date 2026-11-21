package org.example.backend.paiement.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.annonce.entity.Annonce;
import org.example.backend.annonce.repository.AnnonceRepository;
import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.paiement.dto.OffreDTO;
import org.example.backend.paiement.dto.PaiementMapper;
import org.example.backend.paiement.repository.PaiementRepository;
import org.example.backend.user.entity.Artisan;
import org.example.backend.user.repository.ArtisanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OffreService {

    private final ArtisanRepository artisanRepository;
    private final AnnonceRepository annonceRepository;
    private final PaiementRepository paiementRepository;

    @Transactional(readOnly = true)
    public OffreDTO monOffre(Long artisanId) {
        Artisan a = artisanRepository.findById(artisanId).orElseThrow(() -> ResourceNotFoundException.of("Artisan", artisanId));
        return new OffreDTO(
                a.getStatutPro(), a.getProJusquAu(), a.isProOffert(),
                annonceRepository.findByArtisanIdOrderByCreatedAtDesc(artisanId).stream()
                        .filter(Annonce::isSponsorisee)
                        .map(an -> new OffreDTO.AnnonceMiseEnAvant(an.getId(), an.getTitre(), an.getSponsoriseJusquAu()))
                        .toList(),
                paiementRepository.findByArtisanIdOrderByCreatedAtDesc(artisanId).stream().map(PaiementMapper::toDto).toList());
    }
}
