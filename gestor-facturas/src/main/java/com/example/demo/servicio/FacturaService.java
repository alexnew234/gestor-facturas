package com.example.demo.servicio; // <--- CAMBIADO

import com.example.demo.modelo.Factura; // <--- CAMBIADO IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FacturaService {

    private List<Factura> repositorio = new ArrayList<>();
    private Long nextId = 1L;

    @Autowired
    private JavaMailSender mailSender;

    public List<Factura> findAll() {
        return repositorio;
    }

    public Factura findById(Long id) {
        return repositorio.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void guardar(Factura f) {
        // Cálculos
        double iva = f.getBaseImponible() * (f.getPorcentajeIva() / 100);
        double irpf = f.getBaseImponible() * (f.getPorcentajeIrpf() / 100);
        double total = f.getBaseImponible() + iva - irpf;
        f.setImporteTotal(Math.round(total * 100.0) / 100.0);

        // Guardar o Actualizar
        if (f.getId() == null) {
            f.setId(nextId++); // Asignamos ID nuevo
            repositorio.add(f);
        } else {
            // Actualizar existente
            repositorio.removeIf(e -> e.getId().equals(f.getId()));
            repositorio.add(f);
        }
    }

    public void borrar(Long id) {
        repositorio.removeIf(f -> f.getId().equals(id));
    }

    public void enviarFacturaPorEmail(Factura f) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(f.getClienteEmail());

            // Asunto
            message.setSubject("Nueva Factura Disponible - " + f.getConcepto());

            // Cuerpo del mensaje (AQUÍ AÑADIMOS EL CONCEPTO)
            message.setText("Estimado/a " + f.getClienteNombre() + ",\n\n" +
                    "Adjuntamos el resumen de su nueva factura:\n\n" +
                    "----------------------------------------------------\n" +
                    "CONCEPTO: " + f.getConcepto() + "\n" +  // <--- NUEVA LÍNEA
                    "----------------------------------------------------\n" +
                    "Base Imponible: " + f.getBaseImponible() + " €\n" +
                    "Total a Pagar:  " + f.getImporteTotal() + " €\n\n" +
                    "Gracias por confiar en nosotros.");

            mailSender.send(message);
            System.out.println("Email enviado correctamente a " + f.getClienteEmail());
        } catch (Exception e) {
            System.err.println("Error al enviar email: " + e.getMessage());
        }
    }

    // Método de Búsqueda (Buscador)
    public List<Factura> buscar(String consulta) {
        // Convertimos a minúsculas para que la búsqueda no distinga mayúsculas
        String texto = consulta.toLowerCase();

        return repositorio.stream()
                .filter(f -> f.getClienteNombre().toLowerCase().contains(texto) ||
                        f.getConcepto().toLowerCase().contains(texto))
                .toList(); // En versiones antiguas de Java usar .collect(Collectors.toList())
    }
}