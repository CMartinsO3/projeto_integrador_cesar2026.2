package com.hemoflow.hemoflow.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.TipoNo;

public interface NoRedeRepository extends JpaRepository<NoRede, Long> {
    Optional<NoRede> findByCodigo(String codigo);

    Optional<NoRede> findFirstByTipo(TipoNo tipo);

    boolean existsByCodigo(String codigo);
}
