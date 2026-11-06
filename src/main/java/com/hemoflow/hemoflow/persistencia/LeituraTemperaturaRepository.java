package com.hemoflow.hemoflow.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hemoflow.hemoflow.dominio.LeituraTemperatura;

public interface LeituraTemperaturaRepository extends JpaRepository<LeituraTemperatura, Long> {
    Optional<LeituraTemperatura> findTopByBolsaIdOrderByRegistradaEmDesc(Long bolsaId);
}
