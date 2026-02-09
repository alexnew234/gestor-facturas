package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity //
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        // 1. AÑADIDO: "/" (Home) y "/cambiar-tema" son públicos
                        .requestMatchers("/", "/css/**", "/js/**", "/uploads/**", "/webjars/**", "/login", "/cambiar-tema").permitAll()

                        // 2. TEMA 13: La API solo para administradores
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .requestMatchers("/nueva", "/guardar", "/editar/**", "/borrar/**", "/enviar/**").hasRole("ADMIN")

                        // 3. El resto requiere estar logueado
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        // 4. CAMBIO IMPORTANTE: Al entrar, vamos al Dashboard, no a la Home
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    // IMPORTANTE: NO pongas aquí ningún @Bean de UserDetailsService ni PasswordEncoder.
    // Así Spring usará automáticamente tu UsuarioService con {noop}.
}