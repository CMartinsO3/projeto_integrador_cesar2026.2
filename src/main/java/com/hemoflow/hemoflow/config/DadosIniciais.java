package com.hemoflow.hemoflow.config;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.Hospital;
import com.hemoflow.hemoflow.dominio.Ligacao;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.Requisicao;
import com.hemoflow.hemoflow.dominio.TipoNo;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;
import com.hemoflow.hemoflow.persistencia.BolsaRepository;
import com.hemoflow.hemoflow.persistencia.HospitalRepository;
import com.hemoflow.hemoflow.persistencia.LigacaoRepository;
import com.hemoflow.hemoflow.persistencia.NoRedeRepository;
import com.hemoflow.hemoflow.persistencia.RequisicaoRepository;

@Component
public class DadosIniciais implements CommandLineRunner {

    private final NoRedeRepository noRedeRepository;
    private final LigacaoRepository ligacaoRepository;
    private final HospitalRepository hospitalRepository;
    private final BolsaRepository bolsaRepository;
    private final RequisicaoRepository requisicaoRepository;

    public DadosIniciais(
            NoRedeRepository noRedeRepository,
            LigacaoRepository ligacaoRepository,
            HospitalRepository hospitalRepository,
            BolsaRepository bolsaRepository,
            RequisicaoRepository requisicaoRepository
    ) {
        this.noRedeRepository = noRedeRepository;
        this.ligacaoRepository = ligacaoRepository;
        this.hospitalRepository = hospitalRepository;
        this.bolsaRepository = bolsaRepository;
        this.requisicaoRepository = requisicaoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (noRedeRepository.count() > 0) {
            return;
        }

        NoRede hc = noRedeRepository.save(new NoRede("HC", "Hemocentro Central", TipoNo.HEMOCENTRO));
        NoRede h1 = noRedeRepository.save(new NoRede("H1", "Hospital A", TipoNo.HOSPITAL));
        NoRede h2 = noRedeRepository.save(new NoRede("H2", "Hospital B", TipoNo.HOSPITAL));
        NoRede h3 = noRedeRepository.save(new NoRede("H3", "Hospital C", TipoNo.HOSPITAL));
        NoRede h4 = noRedeRepository.save(new NoRede("H4", "Hospital D", TipoNo.HOSPITAL));
        NoRede h5 = noRedeRepository.save(new NoRede("H5", "Hospital E", TipoNo.HOSPITAL));
        NoRede intermediario = noRedeRepository.save(new NoRede("INT", "Nó intermediário", TipoNo.INTERMEDIARIO));

        ligacaoRepository.save(new Ligacao(hc, h1, 25));
        ligacaoRepository.save(new Ligacao(hc, h2, 40));
        ligacaoRepository.save(new Ligacao(hc, intermediario, 15));
        ligacaoRepository.save(new Ligacao(intermediario, h3, 20));
        ligacaoRepository.save(new Ligacao(hc, h4, 60));
        ligacaoRepository.save(new Ligacao(hc, h5, 35));

        Hospital hospitalA = hospitalRepository.save(new Hospital("Hospital A", h1));
        hospitalRepository.save(new Hospital("Hospital B", h2));
        hospitalRepository.save(new Hospital("Hospital C", h3));
        hospitalRepository.save(new Hospital("Hospital D", h4));
        hospitalRepository.save(new Hospital("Hospital E", h5));

        LocalDate hoje = LocalDate.now();
        bolsaRepository.save(new Bolsa(TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS, hoje.minusDays(20), hoje.plusDays(5), "L-001", hc));
        bolsaRepository.save(new Bolsa(TipoSanguineo.O_POS, Hemocomponente.HEMACIAS, hoje.minusDays(10), hoje.plusDays(12), "L-002", hc));
        bolsaRepository.save(new Bolsa(TipoSanguineo.A_POS, Hemocomponente.PLAQUETAS, hoje.minusDays(2), hoje.plusDays(3), "L-003", hc));
        bolsaRepository.save(new Bolsa(TipoSanguineo.AB_POS, Hemocomponente.PLASMA, hoje.minusDays(30), hoje.plusDays(330), "L-004", hc));

        requisicaoRepository.save(new Requisicao(
                hospitalA,
                TipoSanguineo.A_POS,
                Hemocomponente.HEMACIAS,
                1,
                LocalDateTime.now().plusHours(6)
        ));
    }
}
