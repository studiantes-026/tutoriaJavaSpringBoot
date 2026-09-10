package com.example.project.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse respose,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            if (jwtUtils.validarToken(token)) {

                String username = jwtUtils.obtenerUsernameDelToken(token);

                // Extraer el rol empaquetado en el token-Claim personalizado donde se enpaqueta el Rol.
                String rol = jwtUtils.obtenerRolDelToken(token);

                // Creamos la lista de autoridades que spring security necesita para autenticar al usuario.
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(rol));

                // Instanciar el objeto de autenticacion registrando las autoridades.
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

                // Registrar el objeto de autenticacion en el contexto de seguridad.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, respose);
    }
}
