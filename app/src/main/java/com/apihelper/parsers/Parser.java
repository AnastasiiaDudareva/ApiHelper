package com.apihelper.parsers;

import com.apihelper.Error;
import com.apihelper.utils.L;

import java.io.IOException;

public abstract class Parser<T> {

    public abstract T parse(byte[] data) throws IOException;

    public Error error(byte[] data) throws IOException {
        String bytes = new String(data);
        L.logLong("Parser.error()", bytes);
        return new Error(bytes);
    }
}
