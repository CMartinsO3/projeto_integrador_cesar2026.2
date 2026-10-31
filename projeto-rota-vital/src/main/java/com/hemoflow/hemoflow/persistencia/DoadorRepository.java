package com.hemoflow.hemoflow.persistencia;
import com.hemoflow.hemoflow.dominio.Doador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface DoadorRepository extends JpaRepository<Doador, Long> {
    Optional<Doador> findByEmail(String email);
}