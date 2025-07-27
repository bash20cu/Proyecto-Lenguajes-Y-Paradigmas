/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author migue
 */
@Controller
public class HelloController {

    private OpenAIClient openAIClient = new OpenAIClient();

    /*
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Consulta IA Generativa");
        model.addAttribute("content", "home :: content");
        return "layout";
    }

     */
    @GetMapping({"/", "/formulario"})
    public String mostrarHome(Model model) {
        model.addAttribute("title", "Consulta IA Generativa");
        model.addAttribute("mostrarFormulario", true);
        model.addAttribute("title", "Consulta IA Generativa");
        return "layout";
    }

    @PostMapping("/procesar")
    public String procesarFormulario(@RequestParam String color,
            @RequestParam String zona,
            @RequestParam String tamano,
            @RequestParam("imagen") MultipartFile imagen,
            Model model) throws IOException, InterruptedException {

        String colorDetectado = "rgb(255,255,255)";


        if (!imagen.isEmpty()) {
            BufferedImage img = ImageIO.read(imagen.getInputStream());
            colorDetectado = detectarColorPrincipal(img);
        }
        if (colorDetectado == null || colorDetectado.isBlank()) {
            colorDetectado = "rgb(255,255,255)"; // valor por defecto válido para CSS
        }


        // Llama a Prolog con los datos
        String resultadoProlog = ejecutarProlog(color, zona, tamano);

        // Agrega atributos al modelo
        model.addAttribute("color", color);
        model.addAttribute("zona", zona);
        model.addAttribute("tamano", tamano);
        model.addAttribute("colorDetectado", colorDetectado.toLowerCase());
        model.addAttribute("resultadoProlog", resultadoProlog);

        // Control de visibilidad
        model.addAttribute("title", "Resultado de Clasificación");
        model.addAttribute("mostrarFormulario", true);
        model.addAttribute("mostrarResultado", true);

        return "layout";
    }

    private String detectarColorPrincipal(BufferedImage img) {
        Map<Integer, Integer> frecuencia = new HashMap<>();
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                frecuencia.put(rgb, frecuencia.getOrDefault(rgb, 0) + 1);
            }
        }

        // RGB más frecuente
        int colorDominante = Collections.max(frecuencia.entrySet(), Map.Entry.comparingByValue()).getKey();
        Color c = new Color(colorDominante);
        return "RGB(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")";
    }

private String ejecutarProlog(String color, String zona, String tamano) throws IOException, InterruptedException {
    // Línea corregida
String consulta = String.format(
    ":- clasificar('%s', '%s', '%s', Resultado), writeln(Resultado).",
    color, zona, tamano
);



    Path recursosPath = Paths.get("src", "main", "resources");
    Path consultaPath = recursosPath.resolve("consulta.pl").toAbsolutePath();

    Path basePlPath = recursosPath.resolve("base.pl").toAbsolutePath();
    String rutaProlog = basePlPath.toString().replace("\\", "/");

    System.out.println("Creando consulta.pl en: " + consultaPath);
    System.out.println("Usando base.pl desde: " + rutaProlog);

    // Asegúrate que la carpeta resources existe:
    Files.createDirectories(recursosPath);

    // Escribe consulta.pl en src/main/resources
    Files.write(consultaPath, Arrays.asList(
        ":- consult('" + rutaProlog + "').",
        consulta
    ));

    ProcessBuilder pb = new ProcessBuilder("swipl", "-q", "-f", consultaPath.toString(), "-t", "halt");
    pb.redirectErrorStream(true);
    Process process = pb.start();

    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    StringBuilder salida = new StringBuilder();
    String linea;
    while ((linea = reader.readLine()) != null) {
        salida.append(linea).append("\n");
    }

    process.waitFor();
    return salida.toString().trim();
}


    @PostMapping("/query")
    public String query(@RequestParam String query, Model model) {
        String response;
        try {
            response = openAIClient.getChatCompletion(query);
        } catch (Exception e) {
            response = "Error: " + e.getMessage();
        }

        model.addAttribute("title", "Respuesta IA");
        model.addAttribute("query", query);
        model.addAttribute("response", response);
        model.addAttribute("mostrarConsultaIA", true);
        model.addAttribute("mostrarRespuestaIA", true);
        model.addAttribute("mostrarFormulario", true);

        return "layout";
    }

    private String llamarModeloIA(String prompt) {
        // Por ahora, ejemplo fijo:
        // Aquí iría la llamada real a la API de IA (OpenAI, Google Gemini, etc)
        // Puedes usar HTTP clients para llamar la API y devolver la respuesta
        return "Esta es una respuesta simulada para la consulta: " + prompt;
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
