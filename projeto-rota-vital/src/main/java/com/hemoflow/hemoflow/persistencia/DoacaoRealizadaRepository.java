package com.hemoflow.hemoflow.persistencia;

import com.hemoflow.hemoflow.dominio.DoacaoRealizada;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoacaoRealizadaRepository extends JpaRepository<DoacaoRealizada, Long> {
    List<DoacaoRealizada> findByDoadorIdOrderByDataDoacaoDesc(Long doadorId);
    long countByDoadorId(Long doadorId);
}
