package com.apihelper.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ModelParser<T> extends JsonParser<T> {
    private final Class<T> tClass;
    private final ObjectMapper mapper;

    public ModelParser(Class<T> valueType) {
        this(valueType, new ObjectMapper());
    }

    public ModelParser(Class<T> valueType, ObjectMapper mapper) {
        super();
        this.tClass = valueType;
        this.mapper = mapper;
    }

    public T parse(byte[] data) {
        try {
            return mapper.readValue(data, tClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
