package com.apihelper;

import android.text.TextUtils;

import okhttp3.RequestBody;

/**
 * Created by denis on 19.01.16.
 */
public class RestDecorator {
    private final BehaviorMediator mMediator;

    public RestDecorator(BehaviorMediator mediator) {
        mMediator = mediator;
    }

    public class RequestProxy<T> {
        private Request<T> request;

        private RequestProxy(Request<T> request) {
            this.request = request;
        }

        public RequestProxy<T> redirect(Redirect redirect) {
            request = redirect.request(request);
            return this;
        }

        public Request request() {
            return mMediator.request(request);
        }

        public T requestExecute() {
            T response = mMediator.requestExecute(request);
            if (response == null && request instanceof RequestDelegate) {
                return ((RequestDelegate<T>) request).onDelegateFailed();
            }
            return response;
        }
    }

    public <T> RequestProxy<T> create(Request<T> request) {
        return new RequestProxy(request);
    }

    public <T> RequestProxy<T> create(String route, com.apihelper.parsers.JsonParser<T> parser, Request.Listener<T> listener,
                                      Request.ErrorListener errorListener) {
        return create("GET", route, null, parser, listener, errorListener);
    }

    public <T> RequestProxy<T> create(String method, String route, String body, com.apihelper.parsers.JsonParser<T> parser,
                                      Request.Listener<T> listener, Request.ErrorListener errorListener) {
        RequestBody requestBody = null;
        if (!TextUtils.isEmpty(body)) {
            requestBody = RequestBody.create(BehaviorMediator.JSON, body);
        }
        return new RequestProxy(new JsonRequest<T>(method, mMediator.url(route), mMediator.getHeaders(),
                requestBody, parser, listener, errorListener));
    }
}
