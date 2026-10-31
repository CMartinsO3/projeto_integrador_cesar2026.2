package com.hemoflow.hemoflow.persistencia;
import com.hemoflow.hemoflow.dominio.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    List<Agendamento> findByDoadorId(Long doadorId);
    List<Agendamento> findByHospitalId(Long hospitalId);
}