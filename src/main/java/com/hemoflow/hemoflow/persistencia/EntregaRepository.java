package com.hemoflow.hemoflow.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hemoflow.hemoflow.dominio.Entrega;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {
}
