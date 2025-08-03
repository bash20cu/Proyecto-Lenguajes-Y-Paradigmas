package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import io.github.cdimascio.dotenv.Dotenv;

public class GeminiClient {

    private static final Dotenv dotenv = Dotenv.load(); // Cargar .env una sola vez
    private static final String API_KEY = dotenv.get("GEMINI_API_KEY"); // Cargar API_KEY
    private static final String MODELS_URL = "https://generativelanguage.googleapis.com/v1/models?key=" + API_KEY;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<String> listAvailableModels() throws IOException {
        Request request = new Request.Builder()
                .url(MODELS_URL)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            //System.out.println("ListModels HTTP " + response.code() + " - " + responseBody);

            if (!response.isSuccessful()) {
                throw new IOException("Error al listar modelos: " + response.code() + " - " + responseBody);
            }

            Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> models = (List<Map<String, Object>>) responseMap.get("models");

            List<String> filteredModels = new ArrayList<>();
            if (models != null) {
                for (Map<String, Object> model : models) {
                    String name = (String) model.get("name");
                    List<String> supportedMethods = (List<String>) model.get("supportedGenerationMethods");
                    // Filtrar modelos que no sean "pro" y soporten "generateContent"
                    if (name != null && supportedMethods != null
                            && supportedMethods.contains("generateContent")
                            && !name.toLowerCase().contains("pro")) {
                        filteredModels.add(name);
                    }
                }
            }
            return filteredModels;
        }
    }

    public String getCompletion(String prompt) throws IOException {
        List<String> models = listAvailableModels();
        if (models.isEmpty()) {
            return "No hay modelos disponibles que cumplan los criterios.";
        }

        String modeloUsado = models.get(0); // Primer modelo no pro que soporte generateContent
        System.out.println("Usando modelo: " + modeloUsado);

        String apiUrl = "https://generativelanguage.googleapis.com/v1/" + modeloUsado + ":generateContent?key=" + API_KEY;

        Map<String, Object> message = Map.of(
                "parts", List.of(Map.of("text", prompt)),
                "role", "user"
        );
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(message)
        );

        String json = mapper.writeValueAsString(requestBody);
        System.out.println("JSON Enviado: " + json);
        System.out.println("API URL: " + apiUrl);

        RequestBody body = RequestBody.create(
                json, MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println("HTTP " + response.code() + " - " + responseBody);

            if (!response.isSuccessful()) {
                return "Error HTTP: " + response.code() + " - " + responseBody;
            }

            Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                return parts.get(0).get("text").toString();
            }

            return "No se encontró una respuesta válida.";
        }
    }
}
