package com.hemoflow.hemoflow.persistencia;
import com.hemoflow.hemoflow.dominio.Campanha;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CampanhaRepository extends JpaRepository<Campanha, Long> {
    List<Campanha> findByAtivaTrue();
}