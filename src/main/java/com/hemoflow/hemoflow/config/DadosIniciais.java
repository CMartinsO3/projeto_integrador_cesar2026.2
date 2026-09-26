package com.hemoflow.hemoflow.config;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hemoflow.hemoflow.dominio.*;
import com.hemoflow.hemoflow.persistencia.*;

@Component
public class DadosIniciais implements CommandLineRunner {

    private final NoRedeRepository noRedeRepository;
    private final LigacaoRepository ligacaoRepository;
    private final HospitalRepository hospitalRepository;
    private final BolsaRepository bolsaRepository;
    private final RequisicaoRepository requisicaoRepository;
    private final DoadorRepository doadorRepository;
    private final CampanhaRepository campanhaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final AdministradorRepository administradorRepository;
    private final DoacaoRealizadaRepository doacaoRealizadaRepository;

    public DadosIniciais(
            NoRedeRepository noRedeRepository,
            LigacaoRepository ligacaoRepository,
            HospitalRepository hospitalRepository,
            BolsaRepository bolsaRepository,
            RequisicaoRepository requisicaoRepository,
            DoadorRepository doadorRepository,
            CampanhaRepository campanhaRepository,
            AgendamentoRepository agendamentoRepository,
            AdministradorRepository administradorRepository,
            DoacaoRealizadaRepository doacaoRealizadaRepository
    ) {
        this.noRedeRepository = noRedeRepository;
        this.ligacaoRepository = ligacaoRepository;
        this.hospitalRepository = hospitalRepository;
        this.bolsaRepository = bolsaRepository;
        this.requisicaoRepository = requisicaoRepository;
        this.doadorRepository = doadorRepository;
        this.campanhaRepository = campanhaRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.administradorRepository = administradorRepository;
        this.doacaoRealizadaRepository = doacaoRealizadaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (hospitalRepository.count() > 0) {
            return;
        }

        // ── Administrador padrão ──────────────────────────────────────────────
        administradorRepository.save(new Administrador("admin", "admin", "Administrador HemoFlow"));

        // ── Nós de rede ────────────────────────────────────────────────────────
        NoRede nHemope   = noRedeRepository.save(new NoRede("HEMOPE",  "HEMOPE",                               TipoNo.HEMOCENTRO));
        NoRede nHr       = noRedeRepository.save(new NoRede("HR",      "Hospital da Restauracao",              TipoNo.HOSPITAL));
        NoRede nImip     = noRedeRepository.save(new NoRede("IMIP",    "IMIP",                                 TipoNo.HOSPITAL));
        NoRede nHuoc     = noRedeRepository.save(new NoRede("HUOC",    "Hospital Universitario Oswaldo Cruz",  TipoNo.HOSPITAL));
        NoRede nAgamen   = noRedeRepository.save(new NoRede("HAM",     "Hospital Agamenon Magalhaes",          TipoNo.HOSPITAL));
        NoRede nBarao    = noRedeRepository.save(new NoRede("HBL",     "Hospital Barao de Lucena",             TipoNo.HOSPITAL));
        NoRede nGetul    = noRedeRepository.save(new NoRede("HGV",     "Hospital Getulio Vargas",              TipoNo.HOSPITAL));
        NoRede nPelopid  = noRedeRepository.save(new NoRede("HPS",     "Hospital Pelopidas Silveira",          TipoNo.HOSPITAL));
        NoRede nPortug   = noRedeRepository.save(new NoRede("RHP",     "Real Hospital Portugues",              TipoNo.HOSPITAL));
        NoRede nEsperan  = noRedeRepository.save(new NoRede("HE",      "Hospital Esperanca",                   TipoNo.HOSPITAL));
        NoRede nSantaJ   = noRedeRepository.save(new NoRede("HSJ",     "Hospital Santa Joana",                 TipoNo.HOSPITAL));
        NoRede nMariaL   = noRedeRepository.save(new NoRede("HML",     "Hospital Maria Lucinda",               TipoNo.HOSPITAL));

        // ── Ligações ───────────────────────────────────────────────────────────
        ligacaoRepository.save(new Ligacao(nHemope, nHr,      5));
        ligacaoRepository.save(new Ligacao(nHemope, nImip,    5));
        ligacaoRepository.save(new Ligacao(nHemope, nHuoc,    4));
        ligacaoRepository.save(new Ligacao(nHemope, nAgamen, 10));
        ligacaoRepository.save(new Ligacao(nHemope, nBarao,   8));
        ligacaoRepository.save(new Ligacao(nHemope, nGetul,   7));
        ligacaoRepository.save(new Ligacao(nHemope, nPelopid, 9));
        ligacaoRepository.save(new Ligacao(nHemope, nPortug,  6));
        ligacaoRepository.save(new Ligacao(nHemope, nEsperan,12));
        ligacaoRepository.save(new Ligacao(nHemope, nSantaJ,  8));
        ligacaoRepository.save(new Ligacao(nHemope, nMariaL, 15));
        ligacaoRepository.save(new Ligacao(nHr,     nImip,    3));

        // ── Hospitais com coordenadas reais de Recife ─────────────────────────
        Hospital hemope   = criarHospital("HEMOPE",                              nHemope,  -8.0630, -34.8712);
        Hospital hr       = criarHospital("Hospital da Restauracao",             nHr,      -8.0609, -34.9119);
        Hospital imip     = criarHospital("IMIP",                                nImip,    -8.0640, -34.9099);
        Hospital huoc     = criarHospital("Hospital Universitario Oswaldo Cruz", nHuoc,    -8.0635, -34.8994);
        Hospital agamen   = criarHospital("Hospital Agamenon Magalhaes",         nAgamen,  -8.0298, -34.9268);
        Hospital barao    = criarHospital("Hospital Barao de Lucena",            nBarao,   -8.0832, -34.9201);
        Hospital getul    = criarHospital("Hospital Getulio Vargas",             nGetul,   -8.0522, -34.9244);
        Hospital pelopid  = criarHospital("Hospital Pelopidas Silveira",         nPelopid, -8.0557, -34.9027);
        Hospital portug   = criarHospital("Real Hospital Portugues",             nPortug,  -8.0478, -34.9001);
        Hospital esperan  = criarHospital("Hospital Esperanca",                  nEsperan, -8.0600, -34.8843);
        Hospital santaJ   = criarHospital("Hospital Santa Joana",                nSantaJ,  -8.0462, -34.8979);
        criarHospital("Hospital Maria Lucinda",                                  nMariaL,  -8.1030, -34.9350);

        // ── Bolsas de estoque normal ───────────────────────────────────────────
        LocalDate hoje = LocalDate.now();
        bolsaRepository.save(new Bolsa(TipoSanguineo.O_NEG,  Hemocomponente.HEMACIAS,        hoje.minusDays(20), hoje.plusDays(15),  "L-001", nHemope));
        bolsaRepository.save(new Bolsa(TipoSanguineo.O_POS,  Hemocomponente.HEMACIAS,        hoje.minusDays(10), hoje.plusDays(20),  "L-002", nHemope));
        bolsaRepository.save(new Bolsa(TipoSanguineo.A_POS,  Hemocomponente.PLAQUETAS,       hoje.minusDays(2),  hoje.plusDays(5),   "L-003", nHemope));
        bolsaRepository.save(new Bolsa(TipoSanguineo.AB_POS, Hemocomponente.PLASMA,          hoje.minusDays(30), hoje.plusDays(330), "L-004", nHemope));
        bolsaRepository.save(new Bolsa(TipoSanguineo.B_NEG,  Hemocomponente.HEMACIAS,        hoje.minusDays(5),  hoje.plusDays(25),  "L-005", nHr));
        bolsaRepository.save(new Bolsa(TipoSanguineo.A_NEG,  Hemocomponente.PLASMA,          hoje.minusDays(8),  hoje.plusDays(60),  "L-006", nImip));
        bolsaRepository.save(new Bolsa(TipoSanguineo.B_POS,  Hemocomponente.CRIOPRECIPITADO, hoje.minusDays(3),  hoje.plusDays(90),  "L-007", nHuoc));
        bolsaRepository.save(new Bolsa(TipoSanguineo.AB_NEG, Hemocomponente.HEMACIAS,        hoje.minusDays(15), hoje.plusDays(10),  "L-008", nAgamen));

        // ── Bolsas CRÍTICAS (próximas do vencimento) ─────────────────────────
        Bolsa v1 = bolsaRepository.save(new Bolsa(TipoSanguineo.O_NEG,  Hemocomponente.HEMACIAS,  hoje.minusDays(24), hoje.plusDays(1),  "VENC-001", nHemope));
        Bolsa v2 = bolsaRepository.save(new Bolsa(TipoSanguineo.A_POS,  Hemocomponente.PLAQUETAS, hoje.minusDays(4),  hoje.plusDays(2),  "VENC-002", nHr));
        Bolsa v3 = bolsaRepository.save(new Bolsa(TipoSanguineo.B_NEG,  Hemocomponente.PLASMA,    hoje.minusDays(28), hoje.plusDays(3),  "VENC-003", nImip));
        Bolsa v4 = bolsaRepository.save(new Bolsa(TipoSanguineo.AB_POS, Hemocomponente.HEMACIAS,  hoje.minusDays(22), hoje.plusDays(4),  "VENC-004", nHemope));
        // Bolsa já vencida (para teste de filtragem)
        bolsaRepository.save(new Bolsa(TipoSanguineo.O_POS,  Hemocomponente.HEMACIAS,  hoje.minusDays(30), hoje.minusDays(2), "VENC-000", nHemope));

        // ── Bolsas EM TRÂNSITO ────────────────────────────────────────────────
        Bolsa t1 = bolsaRepository.save(new Bolsa(TipoSanguineo.O_NEG,  Hemocomponente.HEMACIAS,  hoje.minusDays(5),  hoje.plusDays(20), "TRANS-001", nHr));
        Bolsa t2 = bolsaRepository.save(new Bolsa(TipoSanguineo.A_POS,  Hemocomponente.PLAQUETAS, hoje.minusDays(3),  hoje.plusDays(4),  "TRANS-002", nImip));
        Bolsa t3 = bolsaRepository.save(new Bolsa(TipoSanguineo.B_POS,  Hemocomponente.PLASMA,    hoje.minusDays(10), hoje.plusDays(45), "TRANS-003", nAgamen));
        t1.setStatus(StatusBolsa.EM_TRANSITO); bolsaRepository.save(t1);
        t2.setStatus(StatusBolsa.EM_TRANSITO); bolsaRepository.save(t2);
        t3.setStatus(StatusBolsa.EM_TRANSITO); bolsaRepository.save(t3);

        // ── Requisição de amostra ──────────────────────────────────────────────
        requisicaoRepository.save(new Requisicao(hemope, TipoSanguineo.A_POS, Hemocomponente.HEMACIAS, 1, LocalDateTime.now().plusHours(6)));

        // ── Campanhas ─────────────────────────────────────────────────────────
        campanhaRepository.save(new Campanha("Doe Sangue - Vida Nova",     "Campanha emergencial por estoque critico de O negativo.",    hoje,             hoje.plusDays(30),  hemope,  "O_NEG,O_POS"));
        campanhaRepository.save(new Campanha("Hemope em Acao",             "Venha salvar vidas no maior hemocentro de Pernambuco.",      hoje,             hoje.plusDays(60),  hemope,  "Todos"));
        campanhaRepository.save(new Campanha("Mutirao do IMIP",            "Doacao coletiva especial para pacientes pediatricos.",       hoje.plusDays(7), hoje.plusDays(45),  imip,    "A_POS,A_NEG,O_POS"));
        campanhaRepository.save(new Campanha("SOS Estoque Barao de Lucena","Reserva critica de hemacias — venha ajudar!",               hoje,             hoje.plusDays(15),  barao,   "O_NEG,B_NEG"));
        campanhaRepository.save(new Campanha("Natal Solidario",            "Campanha de fim de ano do Hospital Portugues.",             hoje.plusDays(90),hoje.plusDays(120), portug,  "Todos"));

        // ── Doadores de amostra ───────────────────────────────────────────────
        Doador maria = doadorRepository.save(new Doador("Maria Silva", "maria@exemplo.com", "senha123", TipoSanguineo.O_NEG, LocalDate.of(1990,  5, 15)));
        Doador joao  = doadorRepository.save(new Doador("Joao Santos", "joao@exemplo.com",  "senha123", TipoSanguineo.A_POS, LocalDate.of(1985, 11, 22)));

        // ── Histórico de doações dos doadores ────────────────────────────────
        doacaoRealizadaRepository.save(new DoacaoRealizada(maria, hemope,  hoje.minusDays(120), 450));
        doacaoRealizadaRepository.save(new DoacaoRealizada(maria, imip,    hoje.minusDays(240), 450));
        doacaoRealizadaRepository.save(new DoacaoRealizada(maria, hemope,  hoje.minusDays(360), 450));
        doacaoRealizadaRepository.save(new DoacaoRealizada(joao,  hr,      hoje.minusDays(90),  450));
        doacaoRealizadaRepository.save(new DoacaoRealizada(joao,  agamen,  hoje.minusDays(180), 450));

        // ── Agendamentos de amostra ───────────────────────────────────────────
        agendamentoRepository.save(new Agendamento(maria, hemope,  LocalDateTime.now().plusDays(7).withHour(9).withMinute(0)));
        agendamentoRepository.save(new Agendamento(joao,  esperan, LocalDateTime.now().plusDays(14).withHour(14).withMinute(30)));
    }

    private Hospital criarHospital(String nome, NoRede no, double lat, double lng) {
        Hospital h = hospitalRepository.save(new Hospital(nome, no));
        h.setLatitude(lat);
        h.setLongitude(lng);
        return hospitalRepository.save(h);
    }
}
