package org.example.backend.user.repository;

import org.example.backend.user.entity.AppUser;
import org.example.backend.user.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);

    long countByRole(Role role);

    /** Administration : tous les comptes, filtrés par rôle (null = tous) et par texte (nom, prénom, e-mail). */
    @Query("""
           SELECT u FROM AppUser u
           WHERE (:role IS NULL OR u.role = :role)
             AND (:q = '' OR LOWER(u.email) LIKE :q OR LOWER(u.nom) LIKE :q OR LOWER(u.prenom) LIKE :q)
           """)
    Page<AppUser> adminSearch(@Param("role") Role role, @Param("q") String q, Pageable pageable);
}
