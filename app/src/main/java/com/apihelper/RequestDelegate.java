package com.apihelper;

import java.util.Map;

import okhttp3.RequestBody;

/**
 * Created by korotenko on 29.07.14.
 */
public abstract class RequestDelegate<T> extends Request<T> {
    protected Request<T> mInnerRequest;

    public RequestDelegate(String method, String url, Map<String, String> headers, RequestBody body,
                           final Request<T> innerRequest) {
        super(method, url, headers, body, innerRequest.listener, innerRequest.errorListener);
        mInnerRequest = innerRequest;
    }

    public T onDelegateFailed() {
        //stab
        return null;
    }
}
