package com.example.demo.modelo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*; // Importamos todas las validaciones
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Factura {

    private Long id;

    // --- DATOS DEL CLIENTE ---

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String clienteNombre;

    @NotBlank(message = "El NIF/CIF es obligatorio")
    @Size(min = 9, max = 9, message = "El NIF debe tener exactamente 9 caracteres")
    private String clienteNif;

    @NotBlank(message = "El email es obligatorio para el envío")
    @Email(message = "Formato de email incorrecto (ej: usuario@dominio.com)")
    private String clienteEmail;

    private String clienteDireccion;

    // --- DATOS ECONÓMICOS ---

    @NotBlank(message = "El concepto es obligatorio")
    @Size(min = 5, max = 100, message = "El concepto debe tener entre 5 y 100 caracteres")
    private String concepto;

    @NotNull(message = "La base imponible no puede estar vacía")
    @Positive(message = "El importe debe ser mayor que cero")
    private Double baseImponible;

    @NotNull(message = "El IVA es obligatorio")
    @Min(value = 0, message = "El IVA no puede ser negativo")
    @Max(value = 100, message = "El IVA no puede superar el 100%")
    private Double porcentajeIva = 21.0;

    @NotNull(message = "El IRPF es obligatorio")
    @Min(value = 0, message = "El IRPF no puede ser negativo")
    @Max(value = 100, message = "El IRPF no puede superar el 100%")
    private Double porcentajeIrpf = 15.0;

    // --- CALCULADOS ---
    private Double importeTotal;
    private LocalDate fechaEmision = LocalDate.now();

    // --- ESTADO ---
    private boolean pagada = false;

    // --- FICHERO ---
    private String nombreLogo;
}