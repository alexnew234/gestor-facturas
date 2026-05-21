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
                        .requestMatchers("/", "/css/**", "/js/**", "/uploads/**", "/webjars/**", "/login", "/cambiar-tema").permitAll()

                        //La API solo para administradores
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .requestMatchers("/nueva", "/guardar", "/editar/**", "/borrar/**", "/enviar/**").hasRole("ADMIN")

                        //El resto requiere estar logueado
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        //Al entrar, vamos al Dashboard
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

}