package com.hemoflow.hemoflow.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hemoflow.hemoflow.dominio.Ligacao;

public interface LigacaoRepository extends JpaRepository<Ligacao, Long> {
}
