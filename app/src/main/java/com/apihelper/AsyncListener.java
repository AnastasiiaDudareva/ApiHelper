package com.apihelper;

import android.os.AsyncTask;

/**
 * Created by denis on 12/27/15.
 */
public abstract class AsyncListener<T> extends AsyncTask<T, T, T> implements Request.Listener<T> {

    @Override
    public void onResponse(T result) {
        executeOnExecutor(THREAD_POOL_EXECUTOR, result);
    }
}
