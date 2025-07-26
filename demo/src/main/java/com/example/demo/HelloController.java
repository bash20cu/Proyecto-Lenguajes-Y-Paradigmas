/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo;

import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;


/**
 *
 * @author migue
 */

@Controller
public class HelloController {
    
    private OpenAIClient openAIClient = new OpenAIClient();
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Consulta IA Generativa");
        model.addAttribute("content", "home :: content");
        return "layout";
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
        model.addAttribute("content", "result :: content");
        model.addAttribute("query", query);
        model.addAttribute("response", response);

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