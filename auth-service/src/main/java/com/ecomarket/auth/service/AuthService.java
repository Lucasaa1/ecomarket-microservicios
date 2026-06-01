package com.ecomarket.auth.service;

import com.ecomarket.auth.dto.LoginRequest;
import com.ecomarket.auth.dto.LoginResponse;
import com.ecomarket.auth.dto.RegistroRequest;
import com.ecomarket.auth.dto.UsuarioResponse;
import com.ecomarket.auth.exception.CredencialesInvalidasException;
import com.ecomarket.auth.exception.UsuarioYaExisteException;
import com.ecomarket.auth.model.Usuario;
import com.ecomarket.auth.repository.UsuarioRepository;
import com.ecomarket.auth.security.JwtService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        log.info("Iniciando registro de usuario para correo {}", request.correo());
        String correo = normalizarCorreo(request.correo());
        String rol = normalizarRol(request.rol());

        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            log.warn("Registro rechazado: correo {} ya existe", correo);
            throw new UsuarioYaExisteException("Ya existe un usuario con ese correo");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre().trim())
                .correo(correo)
                .password(passwordEncoder.encode(request.password()))
                .rol(rol)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado con correo {} y rol {}", guardado.getCorreo(), guardado.getRol());
        return usuarioService.mapearUsuarioResponse(guardado);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Iniciando login para correo {}", request.correo());
        String correo = normalizarCorreo(request.correo());
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> {
                    log.warn("Login fallido para correo {}", correo);
                    return new CredencialesInvalidasException("Credenciales invalidas");
                });

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            log.warn("Login fallido para correo {}", correo);
            throw new CredencialesInvalidasException("Credenciales invalidas");
        }

        String token = jwtService.generarToken(usuario);
        log.info("Login exitoso para correo {}", correo);
        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs(),
                usuarioService.mapearUsuarioResponse(usuario)
        );
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarRol(String rol) {
        String rolNormalizado = rol.trim().toUpperCase(Locale.ROOT);
        if (!rolNormalizado.equals("ADMIN") && !rolNormalizado.equals("CLIENTE")) {
            throw new IllegalArgumentException("Rol permitido: ADMIN o CLIENTE");
        }
        return rolNormalizado;
    }
}
