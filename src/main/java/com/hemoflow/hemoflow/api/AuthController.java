package com.hemoflow.hemoflow.api;

import com.hemoflow.hemoflow.dominio.Administrador;
import com.hemoflow.hemoflow.dominio.Doador;
import com.hemoflow.hemoflow.persistencia.AdministradorRepository;
import com.hemoflow.hemoflow.persistencia.DoadorRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AdministradorRepository adminRepo;
    private final DoadorRepository doadorRepo;

    public AuthController(AdministradorRepository adminRepo, DoadorRepository doadorRepo) {
        this.adminRepo = adminRepo;
        this.doadorRepo = doadorRepo;
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
        return ResponseEntity.ok(Map.of(
            "logado", true, "role", "DOADOR",
            "id", session.getAttribute("doadorId"),
            "nome", session.getAttribute("doadorNome")
        ));
    }
}
