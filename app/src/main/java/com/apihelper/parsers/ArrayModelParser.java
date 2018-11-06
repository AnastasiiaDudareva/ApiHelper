package com.apihelper.parsers;

import com.apihelper.utils.L;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArrayModelParser<T> extends com.apihelper.parsers.JsonParser<List<T>> {
    private final Class<T> tClass;
    private final ObjectMapper mapper;

    public ArrayModelParser(Class<T> valueType) {
        this(valueType, new ObjectMapper());
    }

    public ArrayModelParser(Class<T> valueType, ObjectMapper mapper) {
        super();
        this.tClass = valueType;
        this.mapper = mapper;
    }

    public List<T> parse(byte[] data) {
        List<T> list = new ArrayList<>();

        JsonFactory f = new JsonFactory();
        try {
            JsonParser jp = f.createJsonParser(data);
            // advance stream to START_ARRAY first:
            jp.nextToken();
            // and then each time, advance to opening START_OBJECT
            while (jp.nextToken() == JsonToken.START_OBJECT) {
                T item = mapper.readValue(jp, tClass);
                if (item != null) list.add(item);
                // process
                // after binding, stream points to closing END_OBJECT
            }
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
