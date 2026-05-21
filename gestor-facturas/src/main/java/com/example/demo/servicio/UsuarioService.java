package com.example.demo.servicio;

import com.example.demo.modelo.Factura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService implements UserDetailsService { // [cite: 565]

    @Autowired
    private FacturaService facturaService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. USUARIO ADMINISTRADOR (Fijo)
        if ("admin".equals(username)) {
            return User.builder()
                    .username("admin")
                    .password("{noop}1234")
                    .roles("ADMIN")
                    .build();
        }

        // 2. USUARIOS CLIENTES (Dinámicos basados en facturas)
        List<Factura> facturas = facturaService.findAll();

        for (Factura f : facturas) {
            // Si el nombre del cliente coincide con el usuario introducido
            if (f.getClienteNombre().equalsIgnoreCase(username)) {

                // Creamos el usuario:
                // Username = Nombre Cliente
                // Password = {noop} + NIF del cliente
                // Rol = USER
                return User.builder()
                        .username(f.getClienteNombre())
                        .password("{noop}" + f.getClienteNif())
                        .roles("USER")
                        .build();
            }
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }
}