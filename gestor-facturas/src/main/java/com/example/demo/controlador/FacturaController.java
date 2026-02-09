package com.example.demo.controlador;

import com.example.demo.modelo.Factura;
import com.example.demo.servicio.FacturaService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FacturaController {

    private final FacturaService service;
    private final Path UPLOAD_DIR = Paths.get("upload");

    public FacturaController(FacturaService service) throws IOException {
        this.service = service;
        Files.createDirectories(UPLOAD_DIR);
    }

    // --- TEMA 12: COOKIES (MODO OSCURO) ---
    // Esto hace que la variable 'tema' esté disponible en TODOS los HTML
    @ModelAttribute("tema")
    public String getTema(@CookieValue(name = "tema", defaultValue = "light") String tema) {
        return tema;
    }

    // Endpoint para cambiar el tema y guardar la cookie
    @GetMapping("/cambiar-tema")
    public String cambiarTema(HttpServletResponse response,
                              @CookieValue(name = "tema", defaultValue = "light") String temaActual,
                              @RequestHeader(value = "Referer", defaultValue = "/") String referer) {

        String nuevoTema = "light".equals(temaActual) ? "dark" : "light";

        // Creamos la cookie
        Cookie cookie = new Cookie("tema", nuevoTema);
        cookie.setPath("/"); // Visible en toda la app
        cookie.setMaxAge(7 * 24 * 60 * 60); // Dura 1 semana

        response.addCookie(cookie);

        return "redirect:" + referer; // Vuelve a la página donde estabas
    }
    // --------------------------------------

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth, @RequestParam(required = false) String query) {
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<Factura> facturas = (query != null && !query.isEmpty()) ? service.buscar(query) : service.findAll();

        if (!isAdmin) {
            facturas = facturas.stream()
                    .filter(f -> f.getClienteNombre().equalsIgnoreCase(username))
                    .collect(Collectors.toList());
        }

        model.addAttribute("facturas", facturas);
        model.addAttribute("query", query);
        return "lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("factura", new Factura());
        return "form_factura";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("factura") Factura factura, BindingResult result, @RequestParam("file") MultipartFile file) {
        if (result.hasErrors()) return "form_factura";

        if (!file.isEmpty()) {
            try {
                String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), UPLOAD_DIR.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                factura.setNombreLogo(filename);
            } catch (IOException e) { e.printStackTrace(); }
        } else if (factura.getId() != null) {
            Factura antigua = service.findById(factura.getId());
            if (antigua != null) factura.setNombreLogo(antigua.getNombreLogo());
        }
        service.guardar(factura);
        return "redirect:/";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Factura f = service.findById(id);
        if (f == null) return "error/404";
        model.addAttribute("factura", f);
        return "form_factura";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, Authentication auth) {
        Factura f = service.findById(id);
        if (f == null) return "error/404";
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !f.getClienteNombre().equalsIgnoreCase(auth.getName())) return "redirect:/";
        model.addAttribute("factura", f);
        return "detalle";
    }

    @GetMapping("/borrar/{id}")
    public String borrar(@PathVariable Long id) { service.borrar(id); return "redirect:/"; }

    @GetMapping("/enviar/{id}")
    public String enviar(@PathVariable Long id) {
        Factura f = service.findById(id);
        if (f != null) service.enviarFacturaPorEmail(f);
        return "redirect:/";
    }

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws MalformedURLException {
        Resource file = new UrlResource(UPLOAD_DIR.resolve(filename).toUri());
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"").body(file);
    }
}