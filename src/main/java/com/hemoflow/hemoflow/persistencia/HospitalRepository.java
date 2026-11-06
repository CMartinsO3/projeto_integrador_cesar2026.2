package com.hemoflow.hemoflow.persistencia;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hemoflow.hemoflow.dominio.Hospital;
import com.hemoflow.hemoflow.dominio.NoRede;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByLocalizacao(NoRede localizacao);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Hospital h where h.id = :id")
    Optional<Hospital> findByIdForUpdate(@Param("id") Long id);
}
