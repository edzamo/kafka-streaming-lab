package com.example.banking.services.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JsonSerializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> T jsonStringToObject(String jsonString, Class<T> desiredClass) {

        if (jsonString == null || jsonString.equals(""))
            return null;

        T jsonObject;
        try {
            jsonObject = objectMapper.readValue(jsonString, desiredClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting the json from string to Java object");
        }
        return jsonObject;
    }

    public String jsonObjectToString(Object object) {
        String result;
        try {
            result = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting the Java object to json");
        }
        return result;
    }
}
