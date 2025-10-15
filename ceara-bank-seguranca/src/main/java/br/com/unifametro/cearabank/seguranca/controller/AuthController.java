package br.com.unifametro.cearabank.seguranca.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.unifametro.cearabank.seguranca.model.User;
import br.com.unifametro.cearabank.seguranca.model.RegistrationRequest;
import br.com.unifametro.cearabank.seguranca.model.LoginRequest;
import br.com.unifametro.cearabank.seguranca.repository.UserRepository;
import br.com.unifametro.cearabank.seguranca.service.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // Injeção de dependências no construtor
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                          AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // --- ENDPOINT 1/12: REGISTRO DE USUÁRIO (POST /auth/users) ---
    @PostMapping("/users")
    public ResponseEntity<String> registrarUsuario(@RequestBody RegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return new ResponseEntity<>("Username já está em uso!", HttpStatus.BAD_REQUEST);
        }

        // 1. Criptografa a senha antes de salvar
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUsername(), encodedPassword);

        userRepository.save(user);

        return new ResponseEntity<>("Usuário registrado com sucesso!", HttpStatus.CREATED);
    }

    // --- ENDPOINT 2/12: LOGIN E GERAÇÃO DE JWT (POST /auth/login) ---
    @PostMapping("/login")
    public ResponseEntity<String> authenticateUser(@RequestBody LoginRequest loginRequest) {
        // 1. Tenta autenticar o usuário usando o Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        // 2. Se a autenticação for bem-sucedida, gera o token JWT
        // (A classe UserDetailsService, que você ainda criará, será chamada no 'authenticate')
        String jwt = jwtService.generateToken((User) authentication.getPrincipal());

        return ResponseEntity.ok(jwt);
    }

    // --- ENDPOINT 3/12: VALIDAÇÃO DO TOKEN (GET /auth/validate) ---
    // Este endpoint será usado pelos outros microsserviços para validar tokens
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok("Token Válido para o usuário: " + authentication.getName());
        }
        return new ResponseEntity<>("Token Inválido ou Ausente.", HttpStatus.UNAUTHORIZED);
    }

}