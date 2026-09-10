package com.example.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springFramework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilitar las anataciones de seguridad como @PreAuthorize en los controllers etc.
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    // Rutas publicas
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.POST"/api/usuarios/**").permitAll()

                    // Resriccion de Roles:
                    // Solo el Rol ADMIN puede eliminar usuarios (DELETE)
                    .requestMatchers(HttpMethod.DELETE, "/api/usuario/**").hasRole("ADMIN")

                    // Solo el ROl ADMIN pueder ver el listado de usuarios
                    .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole("ADMIN")

                    // Los usuarios con rol user y admin pueden consultar y crear pedidos
                    .requestMatchers("/api/pedidos/**").hasAnyRole("USER", "ADMIN")

                    // Cualquier otra solicitud requiere estar autenticado
                    .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
