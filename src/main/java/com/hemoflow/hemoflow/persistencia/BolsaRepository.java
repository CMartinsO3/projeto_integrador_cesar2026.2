package com.hemoflow.hemoflow.persistencia;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.StatusBolsa;

public interface BolsaRepository extends JpaRepository<Bolsa, Long> {
    List<Bolsa> findByStatus(StatusBolsa status);

    List<Bolsa> findByStatusAndHemocomponente(StatusBolsa status, Hemocomponente hemocomponente);

    @Query("SELECT b FROM Bolsa b WHERE b.status = 'DISPONIVEL' AND b.dataValidade <= :limite ORDER BY b.dataValidade ASC")
    List<Bolsa> findProximasDoVencimento(@Param("limite") LocalDate limite);

    List<Bolsa> findByStatusOrderByDataValidadeAsc(StatusBolsa status);
}
