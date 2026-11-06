package com.hemoflow.hemoflow.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hemoflow.hemoflow.dominio.ContaOperacional;

public interface ContaOperacionalRepository extends JpaRepository<ContaOperacional, Long> {
    Optional<ContaOperacional> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);
}
