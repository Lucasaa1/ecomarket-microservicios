package com.ecomarket.auth.config;

import com.ecomarket.auth.model.Usuario;
import com.ecomarket.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String ADMIN_CORREO = "admin@ecomarket.cl";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByCorreo(ADMIN_CORREO).isPresent()) {
            return;
        }

        Usuario admin = Usuario.builder()
                .nombre("Administrador EcoMarket")
                .correo(ADMIN_CORREO)
                .password(passwordEncoder.encode("admin123"))
                .rol("ADMIN")
                .build();

        usuarioRepository.save(admin);
        log.info("Administrador por defecto creado con correo {}", ADMIN_CORREO);
    }
}
