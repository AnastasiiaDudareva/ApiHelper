package com.apihelper;

import com.apihelper.utils.L;

import java.io.IOException;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by korotenko on 29.07.14.
 */
public abstract class Request<T> {
    final okhttp3.Request.Builder requestBuilder;
    protected Listener<T> listener;
    protected ErrorListener errorListener;

    final String method;
    final String url;
    final RequestBody body;
    final Object tag;

    public okhttp3.Request.Builder getRequestBuilder() {
        return requestBuilder;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public RequestBody getBody() {
        return body;
    }

    public Object getTag() {
        return tag;
    }

    public Request(String url, Listener<T> listener, ErrorListener errorListener) {
        this(url, null, listener, errorListener);
    }

    public Request(String url, Map<String, String> headers, Listener<T> listener, ErrorListener errorListener) {
        this("GET", url, headers, null, listener, errorListener);
    }

    public Request(String method, String url, Map<String, String> headers, RequestBody body, Listener<T> listener, ErrorListener errorListener) {
        this(method, url, headers, body, listener, errorListener, null);
    }

    public Request(String method, String url, Map<String, String> headers, RequestBody body, Listener<T> listener, ErrorListener errorListener, Object tag) {
        requestBuilder = new okhttp3.Request.Builder()
                .url(url)
                .method(method, body)
                .tag(tag);
        if (headers != null) requestBuilder.headers(Headers.of(headers));
        this.listener = listener;
        this.errorListener = errorListener;
        this.tag = tag;
        this.method = method;
        this.url = url;
        this.body = body;
    }

    public Request(Request request) {
        requestBuilder = request.requestBuilder;
        this.listener = request.listener;
        this.errorListener = request.errorListener;
        this.tag = request.tag;
        this.method = request.method;
        this.url = request.url;
        this.body = request.body;
    }

    public abstract T parseNetworkResponse(Response response) throws IOException;

    public Error parseNetworkError(Response response) throws IOException {
        byte[] bytes = response.body().bytes();
        L.logLong("parseNetworkError", new String(bytes));
        return new Error(response.code(), new String(bytes));
    }

    public interface Listener<T> {
        void onResponse(T result);
    }

    public interface ErrorListener {
        void onError(Error error);
    }
}
