package com.myapp.studentbackend.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class OllamaService {

    // Locked to Phi-3 for high-speed student assistance
    private static final String MODEL = "phi3";

    public String askOllama(String question) {
        try {
            URL url = new URL("http://localhost:11434/api/generate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String body = """
                    {
                      "model": "%s",
                      "prompt": "%s",
                      "stream": false
                    }
                    """.formatted(MODEL, question.replace("\"", "\\\""));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Extract answer text from Ollama JSON
            String json = response.toString();
            int start = json.indexOf("\"response\":\"") + 12;
            int end = json.lastIndexOf("\"");

            return json.substring(start, end).replace("\\n", "\n");

        } catch (Exception e) {
            return "Error contacting AI: " + e.getMessage();
        }
    }
}
