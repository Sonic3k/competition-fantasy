package com.fantasy.competition.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class GrokService {

    @Value("${grok.api-key:}") private String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    public boolean isConfigured() { return apiKey != null && !apiKey.isBlank(); }

    public List<String> generate(String prompt, int n) throws Exception {
        if (!isConfigured()) throw new IllegalStateException("Grok API key not configured");

        String body = mapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
            put("model", "grok-2-image");
            put("prompt", prompt);
            put("n", Math.min(n, 4));
        }});

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("https://api.x.ai/v1/images/generations"))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new RuntimeException("Grok API error " + resp.statusCode() + ": " + resp.body());

        JsonNode root = mapper.readTree(resp.body());
        List<String> urls = new ArrayList<>();
        for (JsonNode item : root.get("data")) {
            if (item.has("url")) urls.add(item.get("url").asText());
            else if (item.has("b64_json")) urls.add("data:image/png;base64," + item.get("b64_json").asText());
        }
        return urls;
    }
}
