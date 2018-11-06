package com.apihelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by denis on 17.02.16.
 */
public abstract class CountDownListener<T> implements Request.Listener<T>, Request.ErrorListener {
    private AtomicInteger counter;

    public CountDownListener(int responsesLimit) {
        this.counter = new AtomicInteger(responsesLimit);
    }

    @Override
    public void onResponse(T result) {
        if (counter.decrementAndGet() == 0) {
            onLimitIsReached();
        }
    }

    @Override
    public void onError(Error error) {
        if (counter.decrementAndGet() == 0) {
            onLimitIsReached();
        }
    }

    public abstract void onLimitIsReached();
}
