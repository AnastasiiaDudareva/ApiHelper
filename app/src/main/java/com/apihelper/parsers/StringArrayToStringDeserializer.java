package com.apihelper.parsers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class StringArrayToStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        StringBuilder builder = new StringBuilder();
        try {
            String value;
            while ((value = jp.nextTextValue()) != null) {
                builder.append(value).append(", ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.length() == 0 ? "" : builder.substring(0, builder.length() - 2).toString();
    }
}