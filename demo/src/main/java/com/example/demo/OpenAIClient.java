/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author migue
 */
public class OpenAIClient {
    private static final String OPENAI_API_KEY = "XXXXXX";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // Parámetros de reintento
    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_BACKOFF_MS = 1000; // 1 segundo

    public String getChatCompletion(String prompt) throws IOException, InterruptedException {
        // Crear mensaje para la API
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo"); // Verificar que sea el modelo correcto
        body.put("messages", messages);
        body.put("max_tokens", 70);

        String jsonBody = mapper.writeValueAsString(body);

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        int attempt = 0;
        long backoff = INITIAL_BACKOFF_MS;

        while (attempt < MAX_RETRIES) {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String respBody = response.body().string();
                    Map<?, ?> jsonResponse = mapper.readValue(respBody, Map.class);
                    var choices = (List<?>) jsonResponse.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                        Map<?, ?> messageResp = (Map<?, ?>) firstChoice.get("message");
                        return (String) messageResp.get("content");
                    }
                    return "No se obtuvo respuesta del modelo";
                } else if (response.code() == 429) {
                    attempt++;
                    System.out.println("Recibido 429, reintentando en " + backoff + "ms (intento " + attempt + ")");
                    Thread.sleep(backoff);
                    backoff *= 2; // Backoff exponencial
                } else {
                    return "Error: " + response.code() + " - " + response.message();
                }
            }
        }
        return "Error 429: Se excedió el número máximo de reintentos por demasiadas solicitudes.";
    }
}