package com.hemoflow.hemoflow.persistencia;
import com.hemoflow.hemoflow.dominio.Agendamento;
import com.hemoflow.hemoflow.dominio.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Collection;
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    List<Agendamento> findByDoadorId(Long doadorId);
    List<Agendamento> findByHospitalId(Long hospitalId);
    long countByHospital_IdAndDataHoraAndStatus(
            Long hospitalId, LocalDateTime dataHora, StatusAgendamento status);
    List<Agendamento> findByHospital_IdAndDataHoraInAndStatus(
            Long hospitalId, Collection<LocalDateTime> horarios, StatusAgendamento status);
}