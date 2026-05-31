package com.ecomarket.catalog.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configure(http)) 
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permitir explícitamente las peticiones de prueba OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
                        
                        // 🌟 CORREGIDO: Ahora permite ver productos con o sin el prefijo /api/
                        .requestMatchers(HttpMethod.GET, 
                            "/api/productos/**", "/productos/**", 
                            "/api/categorias/**", "/categorias/**"
                        ).permitAll()

                        // Solo el ADMIN puede crear, editar o borrar productos
                        .requestMatchers(HttpMethod.POST, "/api/productos/**", "/productos/**", "/api/categorias/**", "/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**", "/productos/**", "/api/categorias/**", "/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**", "/productos/**", "/api/categorias/**", "/categorias/**").hasRole("ADMIN")
                        
                        // Cualquier otra petición debe estar autenticada
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}