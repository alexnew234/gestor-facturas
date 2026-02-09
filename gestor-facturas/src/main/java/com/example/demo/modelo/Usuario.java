package com.example.demo.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {
    private String username;
    private String password;
    private String rol;   // "ADMIN" o "CLIENTE"
    private String email; // Para vincularlo con las facturas
    private String nombre; // Para mostrar "Hola, Pepe"
}