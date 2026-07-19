package com.minimarket.security.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.boot.jackson.JsonComponent;
import java.io.IOException;

// Esta anotación le dice a Spring que registre este sanitizador automáticamente
@JsonComponent
public class XssStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }
        
        // Aquí llamamos a tu clase de sanitización estática usando JSoup
        return XssSanitizer.sanitize(value);
    }
}