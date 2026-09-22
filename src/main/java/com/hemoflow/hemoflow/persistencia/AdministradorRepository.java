package com.hemoflow.hemoflow.persistencia;

import com.hemoflow.hemoflow.dominio.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByUsuario(String usuario);
}
