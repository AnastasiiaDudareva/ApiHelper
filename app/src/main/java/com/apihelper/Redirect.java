package com.apihelper;

public interface Redirect {

    <T> Request<T> request(Request<T> request);
}