package com.hemoflow.hemoflow.estoque;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.api.RecursoNaoEncontradoException;
import com.hemoflow.hemoflow.api.RegraNegocioException;
import com.hemoflow.hemoflow.dominio.Agendamento;
import com.hemoflow.hemoflow.dominio.Doador;
import com.hemoflow.hemoflow.dominio.Hospital;
import com.hemoflow.hemoflow.dominio.StatusAgendamento;
import com.hemoflow.hemoflow.persistencia.AgendamentoRepository;
import com.hemoflow.hemoflow.persistencia.DoadorRepository;
import com.hemoflow.hemoflow.persistencia.HospitalRepository;

@Service
public class AgendamentoService {

    public static final int CAPACIDADE_POR_HORARIO = 5;
    private static final Clock RELOGIO_RECIFE = Clock.system(ZoneId.of("America/Recife"));
    private static final int INTERVALO_MINUTOS = 30;
    private static final int DIAS_ANTECEDENCIA = 30;
    private static final LocalTime INICIO_ATENDIMENTO = LocalTime.of(8, 0);
    private static final LocalTime FIM_ATENDIMENTO = LocalTime.of(18, 0);

    private final AgendamentoRepository agendamentoRepository;
    private final DoadorRepository doadorRepository;
    private final HospitalRepository hospitalRepository;
    private final Clock relogio;

    @Autowired
    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            DoadorRepository doadorRepository,
            HospitalRepository hospitalRepository) {
        this(agendamentoRepository, doadorRepository, hospitalRepository, RELOGIO_RECIFE);
    }

    AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            DoadorRepository doadorRepository,
            HospitalRepository hospitalRepository,
            Clock relogio) {
        this.agendamentoRepository = agendamentoRepository;
        this.doadorRepository = doadorRepository;
        this.hospitalRepository = hospitalRepository;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public List<HorarioDisponivel> consultarDisponibilidade(Long hospitalId, LocalDate data) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Hospital não encontrado: " + hospitalId));
        validarHospitalAtivo(hospital);
        validarData(data);

        List<LocalDateTime> horarios = horariosDoDia(data).toList();
        if (horarios.isEmpty()) {
            return List.of();
        }

        Map<LocalDateTime, Long> ocupacao = agendamentoRepository
                .findByHospital_IdAndDataHoraInAndStatus(
                        hospitalId, horarios, StatusAgendamento.AGENDADO)
                .stream()
                .collect(Collectors.groupingBy(
                        Agendamento::getDataHora,
                        Collectors.counting()));

        return horarios.stream()
                .map(horario -> new HorarioDisponivel(
                        horario.toLocalTime(),
                        CAPACIDADE_POR_HORARIO - ocupacao.getOrDefault(horario, 0L).intValue()))
                .filter(horario -> horario.vagasDisponiveis() > 0)
                .toList();
    }

    @Transactional
    public Agendamento criar(Long doadorId, Long hospitalId, LocalDateTime dataHora) {
        Hospital hospital = hospitalRepository.findByIdForUpdate(hospitalId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Hospital não encontrado: " + hospitalId));
        validarHospitalAtivo(hospital);
        Doador doador = doadorRepository.findById(doadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Doador não encontrado: " + doadorId));
        if (!doador.isAtivo()) {
            throw new RegraNegocioException("Doador inativo não pode realizar agendamento");
        }

        validarDataHora(dataHora);
        long agendados = agendamentoRepository.countByHospital_IdAndDataHoraAndStatus(
                hospitalId, dataHora, StatusAgendamento.AGENDADO);
        if (agendados >= CAPACIDADE_POR_HORARIO) {
            throw new RegraNegocioException("Este horário atingiu o limite de 5 doadores");
        }

        return agendamentoRepository.save(new Agendamento(doador, hospital, dataHora));
    }

    private void validarHospitalAtivo(Hospital hospital) {
        if (!hospital.isAtivo()) {
            throw new RegraNegocioException("Hospital inativo não pode receber agendamentos");
        }
    }

    private void validarDataHora(LocalDateTime dataHora) {
        if (dataHora == null || !horariosDoDia(dataHora.toLocalDate()).anyMatch(
                horario -> horario.toLocalTime().equals(dataHora.toLocalTime()))) {
            throw new RegraNegocioException(
                    "Escolha um horário de segunda a sábado, entre 8h e 18h, em intervalos de 30 minutos");
        }
    }

    private void validarData(LocalDate data) {
        if (data == null || data.isBefore(LocalDate.now(relogio))
                || data.isAfter(LocalDate.now(relogio).plusDays(DIAS_ANTECEDENCIA))) {
            throw new RegraNegocioException("A data deve estar entre hoje e os próximos 30 dias");
        }
        DayOfWeek diaDaSemana = data.getDayOfWeek();
        if (diaDaSemana == DayOfWeek.SUNDAY) {
            throw new RegraNegocioException("Os agendamentos são realizados de segunda a sábado");
        }
    }

    private Stream<LocalDateTime> horariosDoDia(LocalDate data) {
        validarData(data);
        LocalDateTime agora = LocalDateTime.now(relogio);
        return Stream.iterate(
                        data.atTime(INICIO_ATENDIMENTO),
                        horario -> horario.plusMinutes(INTERVALO_MINUTOS))
                .takeWhile(horario -> horario.toLocalTime().isBefore(FIM_ATENDIMENTO))
                .filter(horario -> horario.isAfter(agora));
    }

    public record HorarioDisponivel(LocalTime horario, int vagasDisponiveis) {
    }
}
