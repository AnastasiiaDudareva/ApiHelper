package com.apihelper;

import android.accounts.AccountManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.apihelper.utils.L;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class Session {
    private static final String TAG = Session.class.getName();
    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";
    public final static String ARG_IS_NEW_REGISTRATION = "IS_NEW_REGISTRATION";

    public static final String LOGIN_KEY = "login";
    public static final String ACCESS_TOKEN_KEY = "token";
    public static final String REFRESH_TOKEN_KEY = "refresh";
    public static final String TOKEN_TYPE_KEY = "tokenType";
    public static final String RAW_DATA_KEY = "rawData";
    public static final String UPDATE_TIME_KEY = "updateTime";
    static final long EXPIRES_DELAY_1_DAY = TimeUnit.HOURS.toMillis(24);

    public static long expiresDelay = EXPIRES_DELAY_1_DAY;

    final String login;
    final String refreshToken;
    String token;
    String tokenType;
    String rawData;
    long updateTime;

    public String getLogin() {
        return login;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAccessToken() {
        return token;
    }

    public String getRawData() {
        return rawData;
    }

    public Session(String login, String refreshToken) {
        this.login = login;
        this.refreshToken = refreshToken;
    }

    public void refresh(String token, String tokenType) {
        refresh(token, tokenType, System.currentTimeMillis());
    }

    public void refresh(String token, String tokenType, long updateTime) {
        this.token = token;
        this.tokenType = tokenType;
        this.updateTime = updateTime;

    }

    public boolean isAlive() {
        return isValid() && !TextUtils.isEmpty(token)
                && (System.currentTimeMillis() - updateTime) < expiresDelay;
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(refreshToken);
    }

    public Bundle getLoginBundle() {
        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, login);
        data.putString(AccountManager.KEY_PASSWORD, refreshToken);
        Bundle extraData = new Bundle();
        extraData.putString(ACCESS_TOKEN_KEY, token);
        extraData.putString(TOKEN_TYPE_KEY, tokenType);
        extraData.putString(RAW_DATA_KEY, rawData);
        extraData.putString(UPDATE_TIME_KEY, String.valueOf(updateTime));
        data.putBundle(AccountManager.KEY_USERDATA, extraData);
        return data;
    }

//    public Bundle getAccountBundle(Account account) {
//        Bundle data = new Bundle();
//        data.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
//        data.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
//        data.putString(AccountManager.KEY_PASSWORD, refreshToken);
//        data.putString(AccountManager.KEY_AUTHTOKEN, token);
//        return data;
//    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ACCESS_TOKEN_KEY, token);
            jsonObject.put(REFRESH_TOKEN_KEY, refreshToken);
            jsonObject.put(TOKEN_TYPE_KEY, tokenType);
            jsonObject.put(RAW_DATA_KEY, rawData);
        } catch (JSONException exc) {
            Log.e(TAG, exc.getMessage(), exc);
        }
        return jsonObject.toString();
    }

    public static boolean isSessionValid(Session session) {
        return session != null && session.isValid();
    }

    public static boolean isSessionAlive(Session session) {
        return session != null && session.isAlive();
    }
}