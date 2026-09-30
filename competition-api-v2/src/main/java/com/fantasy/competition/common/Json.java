package com.fantasy.competition.common;

import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/** Thin wrapper over the auto-configured Jackson 3 mapper. JSON columns are stored as strings and converted here. */
@Component
public class Json {

    private final JsonMapper mapper;

    public Json(JsonMapper mapper) {
        this.mapper = mapper;
    }

    public JsonMapper mapper() { return mapper; }

    public String write(Object value) {
        return value == null ? null : mapper.writeValueAsString(value);
    }

    public <T> T read(String json, Class<T> type) {
        return json == null || json.isBlank() ? null : mapper.readValue(json, type);
    }

    public JsonNode tree(String json) {
        return json == null || json.isBlank() ? null : mapper.readTree(json);
    }

    public <T> T convert(Object value, Class<T> type) {
        return value == null ? null : mapper.convertValue(value, type);
    }

    public Map<String, Object> toMap(String json) {
        return json == null || json.isBlank() ? null : mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    /** JsonNode (or null) to its compact JSON text for storage. */
    public static String text(JsonNode node) {
        return node == null || node.isNull() ? null : node.toString();
    }
}
