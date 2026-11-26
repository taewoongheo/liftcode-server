package com.liftcode.liftcode_server.sync.service;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/**
 * Converts the values of a pushed change into a typed record.
 * Rejects unknown keys and values of the wrong type with an {@link IllegalArgumentException}.
 */
@Component
public class SyncValuesReader {

    private final JsonMapper mapper;

    public SyncValuesReader(JsonMapper jsonMapper) {
        this.mapper = jsonMapper.rebuild()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    public <T> T read(Map<String, Object> values, Class<T> type) {
        try {
            return mapper.convertValue(values, type);
        } catch (JacksonException e) {
            throw new IllegalArgumentException(e.getOriginalMessage(), e);
        }
    }
}
