package com.hemoflow.hemoflow.repository;

import com.hemoflow.hemoflow.domain.entity.BolsaComponente;
import com.hemoflow.hemoflow.domain.enums.StatusBolsa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BolsaComponenteRepository extends JpaRepository<BolsaComponente, Long> {
    List<BolsaComponente> findByStatus(StatusBolsa status);
}
