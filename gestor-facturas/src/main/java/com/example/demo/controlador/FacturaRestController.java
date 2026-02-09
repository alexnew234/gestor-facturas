package com.example.demo.controlador;

import com.example.demo.modelo.Factura;
import com.example.demo.servicio.FacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController; // Anotación clave Tema 13

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
public class FacturaRestController {

    @Autowired
    private FacturaService facturaService;

    // Devuelve JSON automáticamente
    @GetMapping
    public List<Factura> obtenerTodas() {
        return facturaService.findAll();
    }
}