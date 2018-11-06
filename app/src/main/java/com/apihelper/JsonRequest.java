package com.apihelper;

import com.apihelper.parsers.JsonParser;
import com.apihelper.utils.L;

import java.io.IOException;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.Response;

public class JsonRequest<T> extends Request<T> {
    private final JsonParser<T> parser;

    public JsonParser<T> getParser() {
        return parser;
    }

    public JsonRequest(String url, JsonParser<T> parser) {
        this(url, null, parser, null, null);
    }

    public JsonRequest(String url, JsonParser<T> parser, Listener<T> listener, ErrorListener errorListener) {
        this(url, null, parser, listener, errorListener);
    }

    public JsonRequest(String url, Map<String, String> headers, JsonParser<T> parser,
                       Listener<T> listener, ErrorListener errorListener) {
        this("GET", url, headers, null, parser, listener, errorListener);
    }

    public JsonRequest(String method, String url, Map<String, String> headers, RequestBody body,
                       JsonParser<T> parser, Listener<T> listener, ErrorListener errorListener) {
        super(method, url, headers, body, listener, errorListener);
        this.parser = parser;
    }

    @Override
    public T parseNetworkResponse(Response response) throws IOException {
        byte[] bytes = response.body().bytes();
//        L.logLong("parseNetworkResponse", new String(bytes));
        return parser == null ? null : parser.parse(bytes);
    }

    @Override
    public Error parseNetworkError(Response response) throws IOException {
        if (parser == null) {
            return super.parseNetworkError(response);
        } else {
            Error error = parser.error(response.body().bytes());
            error.code = response.code();
            return error;
        }
    }
}