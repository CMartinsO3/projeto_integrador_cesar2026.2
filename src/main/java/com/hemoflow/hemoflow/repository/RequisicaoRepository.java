package com.hemoflow.hemoflow.repository;

import com.hemoflow.hemoflow.domain.entity.Requisicao;
import com.hemoflow.hemoflow.domain.enums.StatusRequisicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequisicaoRepository extends JpaRepository<Requisicao, Long> {
    List<Requisicao> findByStatus(StatusRequisicao status);
}
