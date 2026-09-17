package com.hemoflow.hemoflow.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.StatusBolsa;

public interface BolsaRepository extends JpaRepository<Bolsa, Long> {
    List<Bolsa> findByStatus(StatusBolsa status);

    List<Bolsa> findByStatusAndHemocomponente(StatusBolsa status, Hemocomponente hemocomponente);
}
