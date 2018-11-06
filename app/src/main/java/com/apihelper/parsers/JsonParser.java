package com.apihelper.parsers;

import com.apihelper.utils.L;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by denis on 20.01.16.
 */
public abstract class JsonParser<T> extends Parser<T> {

    @Override
    public T parse(byte[] data) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(data);
            L.log("jsonNode", jsonNode.toString());
            return parse(jsonNode);
        }catch (Exception e){
            e.printStackTrace();
            return (T) error(data);
        }
    }

    public T parse(JsonNode jsonNode) {
        return (T) jsonNode;
    }
}
