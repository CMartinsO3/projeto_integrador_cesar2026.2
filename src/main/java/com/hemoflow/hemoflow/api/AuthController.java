package com.hemoflow.hemoflow.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hemoflow.hemoflow.dominio.Administrador;
import com.hemoflow.hemoflow.dominio.ContaOperacional;
import com.hemoflow.hemoflow.dominio.Doador;
import com.hemoflow.hemoflow.dominio.Hospital;
import com.hemoflow.hemoflow.dominio.PerfilOperacional;
import com.hemoflow.hemoflow.persistencia.AdministradorRepository;
import com.hemoflow.hemoflow.persistencia.ContaOperacionalRepository;
import com.hemoflow.hemoflow.persistencia.DoadorRepository;
import com.hemoflow.hemoflow.persistencia.HospitalRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AdministradorRepository adminRepo;
    private final DoadorRepository doadorRepo;
    private final ContaOperacionalRepository contaRepo;
    private final HospitalRepository hospitalRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            AdministradorRepository adminRepo,
            DoadorRepository doadorRepo,
            ContaOperacionalRepository contaRepo,
            HospitalRepository hospitalRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.adminRepo = adminRepo;
        this.doadorRepo = doadorRepo;
        this.contaRepo = contaRepo;
        this.hospitalRepo = hospitalRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login-admin")
    public ResponseEntity<?> loginAdmin(@RequestBody Map<String, String> body, HttpSession session) {
        String usuario = body.get("usuario");
        String senha = body.get("senha");
        Optional<Administrador> admin = adminRepo.findByUsuario(usuario);
        if (admin.isPresent() && admin.get().getSenha().equals(senha)) {
            session.setAttribute("adminId", admin.get().getId());
            session.setAttribute("adminNome", admin.get().getNomeCompleto());
            session.setAttribute("role", "ADMIN");
            return ResponseEntity.ok(Map.of(
                "ok", true,
                "nome", admin.get().getNomeCompleto(),
                "redirect", "/dashboard-admin"
            ));
        }
        return ResponseEntity.status(401).body(Map.of("erro", "Usuário ou senha inválidos"));
    }

    @PostMapping("/login-doador")
    public ResponseEntity<?> loginDoador(@RequestBody Map<String, String> body, HttpSession session) {
        String email = body.get("email");
        String senha = body.get("senha");
        Optional<Doador> doador = doadorRepo.findByEmail(email);
        if (doador.isPresent() && doador.get().getSenha().equals(senha)) {
            session.setAttribute("doadorId", doador.get().getId());
            session.setAttribute("doadorNome", doador.get().getNome());
            session.setAttribute("role", "DOADOR");
            return ResponseEntity.ok(Map.of(
                "ok", true,
                "id", doador.get().getId(),
                "nome", doador.get().getNome(),
                "redirect", "/dashboard-usuario"
            ));
        }
        return ResponseEntity.status(401).body(Map.of("erro", "Email ou senha inválidos"));
    }

    @PostMapping("/login-operacional")
    public ResponseEntity<?> loginOperacional(
            @Valid @RequestBody CredenciaisOperacionais credenciais,
            HttpServletRequest request
    ) {
        ContaOperacional conta = contaRepo.findByUsuario(credenciais.usuario().trim())
                .filter(c -> c.getPerfil() == credenciais.perfil())
                .filter(c -> passwordEncoder.matches(credenciais.senha(), c.getSenhaHash()))
                .orElse(null);
        if (conta == null) {
            return ResponseEntity.status(401).body(Map.of("erro", "Usuário ou senha inválidos"));
        }
        if (!conta.isAprovado()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Cadastro aguardando aprovação da equipe clínica"));
        }

        String redirect = iniciarSessaoOperacional(conta, request);
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "nome", conta.getNome(),
                "redirect", redirect
        ));
    }

    @PostMapping("/cadastro-operacional")
    public ResponseEntity<?> cadastrarContaOperacional(
            @Valid @RequestBody CadastroContaOperacional cadastro,
            HttpServletRequest request
    ) {
        Optional<String> erroCadastro = validarCadastro(cadastro);
        if (erroCadastro.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("erro", erroCadastro.get()));
        }

        Hospital hospital = cadastro.perfil() == PerfilOperacional.HOSPITAL
                ? hospitalRepo.findById(cadastro.hospitalId()).orElseThrow()
                : null;
        boolean aprovado = cadastro.perfil() == PerfilOperacional.MOTORISTA;
        ContaOperacional conta = contaRepo.save(new ContaOperacional(
                cadastro.usuario().trim(),
                passwordEncoder.encode(cadastro.senha()),
                cadastro.nome().trim(),
                cadastro.perfil(),
                hospital,
                aprovado
        ));

        if (!aprovado) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                    "ok", true,
                    "pendente", true,
                    "mensagem", "Cadastro recebido. O acesso será liberado após aprovação da equipe clínica."
            ));
        }

        String redirect = iniciarSessaoOperacional(conta, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "ok", true,
                "pendente", false,
                "nome", conta.getNome(),
                "redirect", redirect
        ));
    }

    @GetMapping("/usuarios")
    public List<ContaOperacionalResposta> listarContasOperacionais() {
        return contaRepo.findAll().stream().map(ContaOperacionalResposta::de).toList();
    }

    @PostMapping("/usuarios")
    public ResponseEntity<?> criarContaOperacional(
            @Valid @RequestBody CadastroContaOperacional cadastro,
            HttpSession session
    ) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(403).body(Map.of("erro", "Somente administradores podem criar contas"));
        }
        Optional<String> erroCadastro = validarCadastro(cadastro);
        if (erroCadastro.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("erro", erroCadastro.get()));
        }

        Hospital hospital = cadastro.perfil() == PerfilOperacional.HOSPITAL
                ? hospitalRepo.findById(cadastro.hospitalId()).orElseThrow()
                : null;
        ContaOperacional conta = contaRepo.save(new ContaOperacional(
                cadastro.usuario().trim(),
                passwordEncoder.encode(cadastro.senha()),
                cadastro.nome().trim(),
                cadastro.perfil(),
                hospital
        ));
        return ResponseEntity.ok(ContaOperacionalResposta.de(conta));
    }

    @PostMapping("/usuarios/{id}/aprovar")
    public ResponseEntity<?> aprovarContaHospital(@PathVariable Long id, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Somente administradores podem aprovar contas"));
        }
        ContaOperacional conta = contaRepo.findById(id).orElse(null);
        if (conta == null) {
            return ResponseEntity.notFound().build();
        }
        if (conta.getPerfil() != PerfilOperacional.HOSPITAL) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Somente contas hospitalares precisam de aprovação"));
        }
        if (conta.getHospital() == null || !conta.getHospital().isAtivo()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O hospital associado não está ativo"));
        }
        conta.aprovar();
        return ResponseEntity.ok(ContaOperacionalResposta.de(contaRepo.save(conta)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("ok", true, "redirect", "/"));
    }

    @GetMapping("/sessao")
    public ResponseEntity<?> sessaoAtual(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (role == null) return ResponseEntity.ok(Map.of("logado", false));
        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(Map.of(
                "logado", true, "role", "ADMIN",
                "nome", session.getAttribute("adminNome")
            ));
        }
        if ("HOSPITAL".equals(role) || "MOTORISTA".equals(role)) {
            Map<String, Object> dados = new java.util.HashMap<>();
            dados.put("logado", true);
            dados.put("role", role);
            dados.put("id", session.getAttribute("operacionalId"));
            dados.put("nome", session.getAttribute("operacionalNome"));
            if ("HOSPITAL".equals(role)) {
                dados.put("hospitalId", session.getAttribute("hospitalId"));
                dados.put("hospitalNome", session.getAttribute("hospitalNome"));
            }
            return ResponseEntity.ok(dados);
        }
        return ResponseEntity.ok(Map.of(
            "logado", true, "role", "DOADOR",
            "id", session.getAttribute("doadorId"),
            "nome", session.getAttribute("doadorNome")
        ));
    }

    private String iniciarSessaoOperacional(ContaOperacional conta, HttpServletRequest request) {
        HttpSession sessaoAnterior = request.getSession(false);
        if (sessaoAnterior != null) {
            sessaoAnterior.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("operacionalId", conta.getId());
        session.setAttribute("operacionalNome", conta.getNome());
        session.setAttribute("role", conta.getPerfil().name());
        if (conta.getHospital() != null) {
            session.setAttribute("hospitalId", conta.getHospital().getId());
            session.setAttribute("hospitalNome", conta.getHospital().getNome());
        }
        return conta.getPerfil() == PerfilOperacional.HOSPITAL
                ? "/requisicoes-hospital"
                : "/motorista";
    }

    private Optional<String> validarCadastro(CadastroContaOperacional cadastro) {
        String usuario = cadastro.usuario().trim();
        if (contaRepo.existsByUsuario(usuario)) {
            return Optional.of("Já existe uma conta com esse usuário");
        }
        if (cadastro.perfil() == PerfilOperacional.HOSPITAL) {
            if (cadastro.hospitalId() == null) {
                return Optional.of("Informe o hospital da conta");
            }
            Hospital hospital = hospitalRepo.findById(cadastro.hospitalId()).orElse(null);
            if (hospital == null || !hospital.isAtivo()) {
                return Optional.of("Hospital não encontrado");
            }
        } else if (cadastro.hospitalId() != null) {
            return Optional.of("Conta de motorista não deve ter hospital");
        }
        return Optional.empty();
    }

    public record CredenciaisOperacionais(
            @NotBlank String usuario,
            @NotBlank String senha,
            @NotNull PerfilOperacional perfil
    ) {
    }

    public record CadastroContaOperacional(
            @NotBlank @Size(max = 120) String usuario,
            @NotBlank @Size(min = 8, max = 72) String senha,
            @NotBlank @Size(max = 160) String nome,
            @NotNull PerfilOperacional perfil,
            Long hospitalId
    ) {
    }

    public record ContaOperacionalResposta(
            Long id,
            String usuario,
            String nome,
            PerfilOperacional perfil,
            Long hospitalId,
            String hospitalNome,
            boolean aprovado
    ) {
        static ContaOperacionalResposta de(ContaOperacional conta) {
            return new ContaOperacionalResposta(
                    conta.getId(),
                    conta.getUsuario(),
                    conta.getNome(),
                    conta.getPerfil(),
                    conta.getHospital() == null ? null : conta.getHospital().getId(),
                    conta.getHospital() == null ? null : conta.getHospital().getNome(),
                    conta.isAprovado()
            );
        }
    }
}
