package com.hemoflow.hemoflow.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hemoflow.hemoflow.dominio.Hospital;
import com.hemoflow.hemoflow.dominio.NoRede;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByLocalizacao(NoRede localizacao);
}
