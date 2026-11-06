package com.hemoflow.hemoflow.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.api.RegraNegocioException;
import com.hemoflow.hemoflow.dominio.Doador;
import com.hemoflow.hemoflow.dominio.Hospital;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.TipoNo;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;
import com.hemoflow.hemoflow.persistencia.DoadorRepository;
import com.hemoflow.hemoflow.persistencia.HospitalRepository;
import com.hemoflow.hemoflow.persistencia.NoRedeRepository;

@SpringBootTest
@Transactional
class AgendamentoServiceTest {

    @Autowired
    private AgendamentoService agendamentoService;
    @Autowired
    private DoadorRepository doadorRepository;
    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private NoRedeRepository noRedeRepository;

    private Hospital hospital;
    private LocalDate dataAtendimento;

    @BeforeEach
    void prepararHospitalEDiaUtil() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        NoRede no = noRedeRepository.save(new NoRede(
                "AGENDA-" + sufixo, "Hospital de teste " + sufixo, TipoNo.HOSPITAL));
        hospital = hospitalRepository.save(new Hospital("Hospital de teste " + sufixo, no));
        dataAtendimento = proximoDiaDeAtendimento();
    }

    @Test
    void disponibilidadeListaVinteHorariosDeMeiaEmMeiaHora() {
        List<AgendamentoService.HorarioDisponivel> horarios =
                agendamentoService.consultarDisponibilidade(hospital.getId(), dataAtendimento);

        assertEquals(20, horarios.size());
        assertEquals(LocalTime.of(8, 0), horarios.get(0).horario());
        assertEquals(LocalTime.of(17, 30), horarios.get(19).horario());
        assertTrue(horarios.stream().allMatch(
                horario -> horario.vagasDisponiveis() == AgendamentoService.CAPACIDADE_POR_HORARIO));
    }

    @Test
    void impedeMaisDeCincoDoadoresNoMesmoHorario() {
        LocalDateTime horario = dataAtendimento.atTime(8, 0);
        for (int i = 0; i < AgendamentoService.CAPACIDADE_POR_HORARIO; i++) {
            Doador doador = doadorRepository.save(new Doador(
                    "Doador " + i + " " + UUID.randomUUID(),
                    "agenda-" + UUID.randomUUID() + "@hemoflow.test",
                    "senha",
                    TipoSanguineo.O_NEG,
                    LocalDate.now().minusYears(30)));
            agendamentoService.criar(doador.getId(), hospital.getId(), horario);
        }

        assertThrows(RegraNegocioException.class, () -> {
            Doador doador = doadorRepository.save(new Doador(
                    "Doador excedente",
                    "agenda-excedente-" + UUID.randomUUID() + "@hemoflow.test",
                    "senha",
                    TipoSanguineo.O_NEG,
                    LocalDate.now().minusYears(30)));
            agendamentoService.criar(doador.getId(), hospital.getId(), horario);
        });

        List<AgendamentoService.HorarioDisponivel> restantes =
                agendamentoService.consultarDisponibilidade(hospital.getId(), dataAtendimento);
        assertTrue(restantes.stream().noneMatch(slot -> slot.horario().equals(LocalTime.of(8, 0))));
    }

    @Test
    void rejeitaDomingoEHorarioForaDoAtendimento() {
        Doador doador = doadorRepository.save(new Doador(
                "Doador inválido " + UUID.randomUUID(),
                "agenda-invalida-" + UUID.randomUUID() + "@hemoflow.test",
                "senha",
                TipoSanguineo.O_NEG,
                LocalDate.now().minusYears(30)));
        LocalDate domingo = dataAtendimento;
        while (domingo.getDayOfWeek() != DayOfWeek.SUNDAY) {
            domingo = domingo.plusDays(1);
        }
        LocalDate dataDomingo = domingo;

        assertThrows(RegraNegocioException.class,
                () -> agendamentoService.consultarDisponibilidade(hospital.getId(), dataDomingo));
        assertThrows(RegraNegocioException.class,
                () -> agendamentoService.criar(
                        doador.getId(), hospital.getId(), dataAtendimento.atTime(18, 0)));
    }

    private LocalDate proximoDiaDeAtendimento() {
        LocalDate data = LocalDate.now().plusDays(1);
        while (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            data = data.plusDays(1);
        }
        return data;
    }
}
