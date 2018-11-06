package com.apihelper;

import android.text.TextUtils;
import android.util.Base64;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;

/**
 * Created by korotenko on 29.07.14.
 */
public class BehaviorMediator {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private String scheme = "http://";

    private String domain;

    protected final Map<String, String> headers = new HashMap<String, String>();

    {
        headers.put("Content-Type", "application/json");
    }

    public Map<String, String> getHeaders() {
        HashMap<String, String> map = new HashMap<String, String>(headers);
        return map;
    }

    public Map<String, String> getHeadersWithoutContentType() {
        HashMap<String, String> mapWithoutContentType = new HashMap<String, String>(headers);
        mapWithoutContentType.remove("Content-Type");
        return mapWithoutContentType;
    }

    public Map<String, String> getHeadersWithJsonContentType() {
        HashMap<String, String> mapWithoutContentType = new HashMap<String, String>(getHeadersWithoutContentType());
        mapWithoutContentType.put("Content-Type", "application/json");
        return mapWithoutContentType;
    }

    public void changeDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain(){
        return domain;
    }

    public String url(String route) {
        if (TextUtils.isEmpty(route)) {
            return "";
        }
        return scheme + domain + route;
    }

    public Request request(Request request) {
        ApiHelper.request(request);
        return request;
    }

    public <T> T requestExecute(Request<T> request) {
        return ApiHelper.requestExecute(request);
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void basicAuthentication(String username, String password) {
        String creds = String.format("%s:%s", username, password);
        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
        this.headers.put("Authorization", auth);
    }
}
