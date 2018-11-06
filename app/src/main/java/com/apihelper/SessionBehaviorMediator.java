package com.apihelper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.apihelper.auth.AppAuthenticator;
import com.apihelper.utils.ApiUtils;
import com.apihelper.utils.L;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by korotenko on 29.07.14.
 */
public class SessionBehaviorMediator extends BehaviorMediator {
    private static final String TAG = SessionBehaviorMediator.class.getName();

    private static final Object AUTH_LOCK = new Object();

    public static final String LOGIN_URL_KEY = "login_url_key";
    public static final String REFRESH_TOKEN_URL_KEY = "refresh_token_url_key";
    public static final String LOGOUT_URL_KEY = "logout_url_key";
    public static final String DATA_KEY = "data";
    public static final String METHOD_LOGOUT_KEY = "method_logout_key";

    public static final String A_AUTHORIZATION_KEY = "User-Token";
    public static final String A_ACCESS_TOKEN_KEY = "token";
    public static final String A_REFRESH_TOKEN_KEY = "refresh";
    public static final String A_TOKEN_TYPE_KEY = "token_type";
    public static final String A_EXPIRES_KEY = "expires";
    public static final String A_REFRESH_STRING_START = "a_refresh_string_start";
    public static final String A_REFRESH_HEADER = "a_refresh_header";

    private Context mContext;
    private Session mSession;
    private AppAuthenticator authenticator;
    protected final Map<String, String> urlEndpoints = new HashMap<String, String>();
    protected final Map<String, String> authKeys = new HashMap<String, String>();

    {
        authKeys.put(A_AUTHORIZATION_KEY, A_AUTHORIZATION_KEY);
        authKeys.put(A_ACCESS_TOKEN_KEY, A_ACCESS_TOKEN_KEY);
        authKeys.put(A_REFRESH_TOKEN_KEY, A_REFRESH_TOKEN_KEY);
        authKeys.put(A_TOKEN_TYPE_KEY, A_TOKEN_TYPE_KEY);
        authKeys.put(A_EXPIRES_KEY, A_EXPIRES_KEY);
        authKeys.put(METHOD_LOGOUT_KEY, "POST");
    }

    public Session getSession() {
        return mSession;
    }

    public SessionBehaviorMediator(Context context) {
        L.log("SessionBehaviorMediator", "constructor");
        mContext = context.getApplicationContext() == null ? context : context.getApplicationContext();
        authenticator = new AppAuthenticator(mContext, null);
    }

    public SessionBehaviorMediator addUrlEndpoint(String key, String urlEndpoint) {
        urlEndpoints.put(key, urlEndpoint);
        return this;
    }

    public SessionBehaviorMediator addAuthKeys(String key, String authKey) {
        if (authKeys.containsKey(key)) {
            authKeys.remove(key);
        }
        authKeys.put(key, authKey);
        return this;
    }

    public SessionBehaviorMediator setExpiresDelayMillis(long millis) {
        Session.expiresDelay = millis;
        return this;
    }

    public void restore(final Request.Listener<Session> listener, final Request.ErrorListener errorListener) {
        synchronized (AUTH_LOCK) {
            restoreSession();
            if (isSessionValid()) {
                if (isSessionAlive()) {
                    if (listener != null) {
                        listener.onResponse(mSession);
                    }
                } else {
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... params) {
                            refreshTokenIfNeeded();
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            if (isSessionAlive()) {
                                if (listener != null) {
                                    listener.onResponse(mSession);
                                }
                            } else {
                                if (errorListener != null) {
                                    errorListener.onError(new Error("Authorization failed!"));
                                }
                            }
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            } else {
                if (errorListener != null) {
                    errorListener.onError(new Error("Authorization failed!"));
                }
            }
        }
    }

    public Request login(final Map<String, String> params, final Request.Listener<Session> listener,
                         final Request.ErrorListener errorListener) {
        return login(url(urlEndpoints.get(LOGIN_URL_KEY)), params, listener, errorListener);
    }

    public Request login(String url, final Map<String, String> params, final Request.Listener<Session> listener,
                         final Request.ErrorListener errorListener) {
        LoginRequest request = new LoginRequest(url, params, new Request.Listener<Session>() {
            @Override
            public void onResponse(Session session) {
                if (isSessionValid()) {
                    if (listener != null) {
                        listener.onResponse(session);
                    }
                } else {
                    if (errorListener != null) {
                        errorListener.onError(new Error("Session is not valid!"));
                    }
                }
            }
        }, new Request.ErrorListener() {
            @Override
            public void onError(Error error) {
                if (errorListener != null) {
                    errorListener.onError(error);
                }
            }
        });

        ApiHelper.request(request);
        return request;
    }

    public Request connectSocial(final Map<String, String> params, final Request.Listener<Session> listener,
                                 final Request.ErrorListener errorListener) {
        LoginRequest request = new LoginRequest(url(urlEndpoints.get(LOGIN_URL_KEY)), params, new Request.Listener<Session>() {
            @Override
            public void onResponse(Session session) {
                if (isSessionValid()) {
                    if (listener != null) {
                        listener.onResponse(session);
                    }
                } else {
                    if (errorListener != null) {
                        errorListener.onError(new Error("Session is not valid!"));
                    }
                }
            }
        }, new Request.ErrorListener() {
            @Override
            public void onError(Error error) {
                if (errorListener != null) {
                    errorListener.onError(error);
                }
            }
        });

        ApiHelper.request(request);
        return request;
    }


    public void logout() {
        logout(null, null);
    }

    public void logout(final Request.Listener<Session> listener, final Request.ErrorListener errorListener) {
        L.log("logout", !isSessionValid());
        if (!isSessionValid()) {
            destroySession();
            if (listener != null) {
                listener.onResponse(null);
            }
            return;
        }
        ApiHelper.cancelAll(this);

        if (authKeys.containsKey(A_REFRESH_STRING_START)) {
            headers.put(authKeys.get(A_AUTHORIZATION_KEY),
                    authKeys.get(A_REFRESH_STRING_START) + " " + mSession.getAccessToken());
        }
        if (authKeys.containsKey(A_REFRESH_HEADER)) {
            headers.put(authKeys.get(A_REFRESH_HEADER), mSession.getRefreshToken());
        }
        LogoutRequest request = new LogoutRequest(new Request.Listener<Session>() {
            @Override
            public void onResponse(Session session) {
                destroySession();
                if (listener != null) {
                    listener.onResponse(session);
                }
            }
        }, new Request.ErrorListener() {
            @Override
            public void onError(Error error) {
                destroySession();
                if (errorListener != null) {
                    errorListener.onError(new Error(error.getMessage()));
                }
            }
        });
        ApiHelper.request(request);
    }


    @Override
    public Request request(final Request request) {
        request.requestBuilder.tag(this);
        if (isSessionAlive()) {
            ApiHelper.request(new SessionController(request));
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    refreshTokenIfNeeded();

                    ApiHelper.HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            ApiHelper.request(new SessionController(request));
                        }
                    });
                }
            }).start();
        }
        return request;
    }

    @Override
    public <T> T requestExecute(Request<T> request) {
        request.requestBuilder.tag(this);
        refreshTokenIfNeeded();
        request = new SessionController(request);
        return ApiHelper.requestExecute(request);
    }


    private void restoreSession() {
        if (!isSessionValid()) {
            mSession = authenticator.getAccountData();
        }
        if (isSessionAlive()) {
            if (authKeys.containsKey(A_REFRESH_STRING_START)) {
                headers.put(authKeys.get(A_AUTHORIZATION_KEY),
                        authKeys.get(A_REFRESH_STRING_START) + " " + mSession.getAccessToken());
            } else {
                headers.put(authKeys.get(A_AUTHORIZATION_KEY), mSession.getAccessToken());
            }
        } else {
            if (authKeys.containsKey(A_REFRESH_STRING_START) && mSession != null) {
                headers.put(authKeys.get(A_AUTHORIZATION_KEY),
                        authKeys.get(A_REFRESH_STRING_START) + " " + mSession.getRefreshToken());
            }
        }
    }

    public void refreshTokenIfNeeded() {
        synchronized (AUTH_LOCK) {
            if (authKeys.get(A_REFRESH_TOKEN_KEY) != null && !isSessionAlive()) {
                restoreSession();
                if (Session.isSessionValid(mSession) && !mSession.isAlive()) {
                    ApiHelper.requestExecute(new RefreshRequest());
                }
            }
        }
    }

    private void updateAccount() {
        AccountManager accountManager = AccountManager.get(mContext);
        String accountType = mContext.getPackageName();

        final Account account = new Account(mSession.getLogin(),
                accountType);
        accountManager.setPassword(account, mSession.getRefreshToken());
        accountManager.setUserData(account, Session.ACCESS_TOKEN_KEY, mSession.token);
        accountManager.setUserData(account, Session.UPDATE_TIME_KEY, String.valueOf(mSession.updateTime));
    }

    private void destroySession() {
        if (mSession != null) {
            mSession = null;
        }
        new AppAuthenticator(mContext, null).removeAccount();
    }

    public boolean isSessionAlive() {
        return Session.isSessionAlive(mSession);
    }

    public boolean isSessionValid() {
        return Session.isSessionValid(mSession);
    }

    protected class LoginRequest extends Request<Session> {

        public LoginRequest(String url, Map<String, String> params, Listener<Session> listener, ErrorListener errorListener) {
            super("POST", url, headers,
                    RequestBody.create(JSON, ApiUtils.mapToJsonObject(params).toString()),
                    listener, errorListener);
        }

        @Override
        public Session parseNetworkResponse(Response response) throws IOException {
            parseTokens(response);
            return mSession;
        }
    }

    private class RefreshRequest extends Request<Session> {

        public RefreshRequest() {
            super("POST", url(urlEndpoints.get(REFRESH_TOKEN_URL_KEY)), headers,
                    RequestBody.create(JSON, ApiUtils.toJsonObject(authKeys.get(A_REFRESH_TOKEN_KEY),
                            mSession.refreshToken).toString()),
                    null, null);
        }

        @Override
        public Session parseNetworkResponse(Response response) throws IOException {
            parseTokens(response);
            return mSession;
        }
    }

    private class LogoutRequest extends Request<Session> {

        public LogoutRequest(Listener listener, ErrorListener errorListener) {
            super(authKeys.get(METHOD_LOGOUT_KEY), url(urlEndpoints.get(LOGOUT_URL_KEY)), headers,
                    authKeys.get(METHOD_LOGOUT_KEY).equals("POST") ? RequestBody.create(JSON, "") : null,
                    listener, errorListener);
        }

        @Override
        public Session parseNetworkResponse(Response response) throws IOException {
            return mSession;
        }
    }

    private class SessionController extends Request {

        private final Request request;

        public SessionController(Request request) {
            super(request);
            this.request = request;
            if (isSessionAlive()) {
                if (authKeys.containsKey(A_REFRESH_STRING_START)) {
                    request.requestBuilder.header(authKeys.get(A_AUTHORIZATION_KEY),
                            authKeys.get(A_REFRESH_STRING_START) + " " + mSession.getAccessToken());
                } else {
                    request.requestBuilder.header(authKeys.get(A_AUTHORIZATION_KEY), mSession.getAccessToken());
                }
            }
        }

        @Override
        public Object parseNetworkResponse(Response response) throws IOException {
            if (isSessionValid() && !isSessionAlive() && response.headers().get(A_ACCESS_TOKEN_KEY) != null) {
                String accessToken = response.headers().get(A_ACCESS_TOKEN_KEY);
                String tokenType = "";
                String[] tokenSegments = accessToken.split(" ");
                if (tokenSegments.length > 1) {
                    tokenType = tokenSegments[0];
                }
                mSession.refresh(accessToken, tokenType);
                if (authKeys.containsKey(A_REFRESH_STRING_START)) {
                    headers.put(authKeys.get(A_AUTHORIZATION_KEY),
                            authKeys.get(A_REFRESH_STRING_START) + " " + accessToken);
                } else {
                    headers.put(authKeys.get(A_AUTHORIZATION_KEY), accessToken);
                }
                updateAccount();
            }
            return request.parseNetworkResponse(response);
        }
    }

    protected void parseTokens(Response response) throws IOException {
        try {
            for (int i = 0; i < response.headers().size(); i++) {
                L.log(response.headers().name(i), response.headers().value(i));
            }
            String accessToken = "";
            String refreshToken = "";
            String rawData = "";
            String tokenType = "";
            String login = "unknown";

            if (response.headers().get(authKeys.get(A_ACCESS_TOKEN_KEY)) == null) {
                String body = new String(response.body().bytes());
                JSONObject result = new JSONObject(body);
                JSONObject jsonObject;
                L.log("result", result);
                if (result.has(DATA_KEY)) {
                    jsonObject = result.getJSONObject("data");
                } else {
                    jsonObject = result;
                }
                if (jsonObject.has(Session.LOGIN_KEY) && !jsonObject.isNull(Session.LOGIN_KEY)) {
                    jsonObject.getString(Session.LOGIN_KEY);
                }
                accessToken = jsonObject.get(authKeys.get(A_ACCESS_TOKEN_KEY)).toString();
                if (jsonObject.has(authKeys.get(A_REFRESH_TOKEN_KEY))) {
                    refreshToken = jsonObject.get(authKeys.get(A_REFRESH_TOKEN_KEY)).toString();
                } else {
                    refreshToken = mSession.getRefreshToken();
                }
                tokenType = jsonObject.optString(authKeys.get(A_TOKEN_TYPE_KEY));

                if (mSession == null) {
                    mSession = new Session(login, refreshToken);
                }
                rawData = body;
            } else {
                accessToken = response.headers().get(authKeys.get(A_ACCESS_TOKEN_KEY));
                refreshToken = authKeys.get(A_REFRESH_TOKEN_KEY) == null ?
                        accessToken : response.headers().get(authKeys.get(A_REFRESH_TOKEN_KEY));
                String[] tokenSegments = accessToken.split(" ");
                if (tokenSegments.length > 1) {
                    tokenType = tokenSegments[0];
                }
                rawData = "";
            }

            if (mSession == null) {
                mSession = new Session(login, refreshToken);
            }
            mSession.rawData = rawData;
            mSession.refresh(accessToken, tokenType);
            if (authKeys.containsKey(A_REFRESH_STRING_START)) {
                headers.put(authKeys.get(A_AUTHORIZATION_KEY),
                        authKeys.get(A_REFRESH_STRING_START) + " " + accessToken);
            } else {
                headers.put(authKeys.get(A_AUTHORIZATION_KEY), accessToken);
            }
            updateAccount();
        } catch (JSONException exc) {
            Log.e(TAG, exc.getMessage(), exc);
        }
    }


}
