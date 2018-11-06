package com.apihelper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.apihelper.utils.L;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * Created by korotenko on 29.07.14.
 */
public class ApiHelper {
    public static final String MAIN_DOMAIN_RESOURCES = "http://84.22.110.135:81/static/content/";

    private static final String TAG = ApiHelper.class.getName();
    private static final Object REQUEST_QUEUE_LOCK = new Object();
    private static final Object CANCEL_QUEUE_LOCK = new Object();
    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static OkHttpClient.Builder okHttpClientBuilder;

    protected ApiHelper() {
    }

    static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static volatile OkHttpClient queue;

    private static OkHttpClient getRequestQueue() {
        if (okHttpClientBuilder == null) {
            throw new IllegalArgumentException("ApiHelper wasn't initialized. Call first ApiHelper.init(Context)");
        }
        synchronized (REQUEST_QUEUE_LOCK) {
            if (queue == null) {
                queue = okHttpClientBuilder.build();
            }
            return queue;
        }
    }

    public static OkHttpClient.Builder init(Context context) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(getCacheDirectory(context), cacheSize);
        okHttpClientBuilder = new OkHttpClient.Builder()
                .readTimeout(30000, TimeUnit.MILLISECONDS)
                .writeTimeout(30000, TimeUnit.MILLISECONDS)
                .cache(cache);
        boolean isDebuggable = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        L.setIsDebugMode(isDebuggable);
        return okHttpClientBuilder;
    }

    public static Call request(final Request request) {
        synchronized (CANCEL_QUEUE_LOCK) {
            L.log("request "+request.getMethod(), request.url);
            Call call = getRequestQueue().newCall(request.requestBuilder.build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    L.logI("onFailure", e.toString());
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            if (request.errorListener != null) {
                                request.errorListener.onError(new Error(e.toString()));
                            }
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    L.log("response.code()", response.code()+" "+request.url);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (response.code() == 200 || response.code() == 201
                                        || response.code() == 204) {
                                    if (request.listener != null) {
                                        final Object data = request.parseNetworkResponse(response);
                                        if (data instanceof Error) {
                                            if (request.errorListener != null) {
                                                HANDLER.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        request.errorListener.onError((Error) data);
                                                    }
                                                });
                                            }
                                        } else {
                                            HANDLER.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    request.listener.onResponse(data);
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    if (request.errorListener != null) {
                                        final Error error = request.parseNetworkError(response);
                                        HANDLER.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                request.errorListener.onError(error);
                                            }
                                        });
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            });
            return call;
        }
    }

    public static <T> T requestExecute(Request<T> request) {
        synchronized (CANCEL_QUEUE_LOCK) {
            L.log("request "+request.getMethod(), request.url);
            try {
                Response response = getRequestQueue().newCall(request.requestBuilder.build()).execute();
                L.log("response.code()", response.code());
                if (response.code() == 200 || response.code() == 201
                        || response.code() == 204) {
                    T result = request.parseNetworkResponse(response);
                    if (request.listener != null) {
                        request.listener.onResponse(result);
                    }
                    return result;
                } else {
                    if (request.errorListener != null) {
                        request.errorListener.onError(new Error(response.code(), response.body().string()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (request.errorListener != null) {
                    request.errorListener.onError(new Error(e.toString()));
                }
            }
            return null;
        }
    }

    public static void cancelAll() {
        synchronized (CANCEL_QUEUE_LOCK) {
            getRequestQueue().dispatcher().cancelAll();
        }
    }

    public static void cancelAll(Object tag) {
        synchronized (CANCEL_QUEUE_LOCK) {
            List<Call> queuedCalls = getRequestQueue().dispatcher().queuedCalls();
            for (Call call : queuedCalls) {
                if (call.request().tag() == tag) {
                    call.cancel();
                }
            }
            List<Call> runningCalls = getRequestQueue().dispatcher().runningCalls();
            for (Call call : runningCalls) {
                if (call.request().tag() == tag) {
                    call.cancel();
                }
            }
        }
    }

    private static File getCacheDirectory(Context context) {
        File appCacheDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            L.logV("CacheDirectory", "Can't define system cache directory! The app should be re-installed.");
        }
        return appCacheDir;
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                L.logV("ExternalCacheDir", "Unable to create external cache directory");
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                L.logI("ExternalCacheDir", "Can't create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }
}
