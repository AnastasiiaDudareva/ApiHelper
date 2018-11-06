package com.apihelper.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.apihelper.Session;

public class AuthActivityPhantom {
    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an Udinic account";

    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an Udinic account";

    private AccountManager mAccountManager;
	private String mAuthTokenType;
	private String accountName;
	private final Context mContext;
	private final AuthActivityListener authActivityListener;

	public AccountManager getAccountManager() {
		return mAccountManager;
	}

	public String getAuthTokenType() {
		return mAuthTokenType;
	}

	public String getAccountName() {
		return accountName;
	}

	public AuthActivityPhantom(Context context,
			AuthActivityListener authActivityListener) {
		mContext = context;
		this.authActivityListener = authActivityListener;
	}

	/**
	 * Called when the activity is first created.
	 */
	public void onCreate(Bundle savedInstanceState) {
		mAccountManager = AccountManager.get(mContext);

		accountName = authActivityListener.getIntent().getStringExtra(
                Session.ARG_ACCOUNT_NAME);
		mAuthTokenType = authActivityListener.getIntent().getStringExtra(
                Session.ARG_AUTH_TYPE);
		if (mAuthTokenType == null) {
			mAuthTokenType = AUTHTOKEN_TYPE_FULL_ACCESS;
		}

	}

	/**
	 * Called when user finish login
	 * 
	 * @param session
	 */
	public void finishLogin(Session session) {
		Intent intent = new Intent();
		if (!Session.isSessionValid(session)) {
			return;
		}
		String accountType = mContext.getPackageName();

		Bundle extras = session.getLoginBundle();
		extras.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
		intent.putExtras(extras);

		final Account account = new Account(session.getLogin(),
				accountType);
		if (authActivityListener.getIntent().getBooleanExtra(
                Session.ARG_IS_ADDING_NEW_ACCOUNT, false)) {

			// Creating the account on the device and setting the auth token we
			// got
			// (Not setting the auth token will cause another call to the server
			// to authenticate the user)
			mAccountManager.addAccountExplicitly(account, session.getRefreshToken(), extras.getBundle(AccountManager.KEY_USERDATA));
		} else {
			mAccountManager.setPassword(account, session.getRefreshToken());
            Bundle userData = extras.getBundle(AccountManager.KEY_USERDATA);
            if (userData != null) {
                for (String key : userData.keySet()) {
                    mAccountManager.setUserData(account, key, userData.getString(key));
                }
            }
		}
        mAccountManager.setAuthToken(account, mAuthTokenType, session.getAccessToken());

		authActivityListener.setAccountAuthenticatorResult(intent.getExtras());
		authActivityListener.setResult(Activity.RESULT_OK, intent);
	}

    //TODO
	public interface AuthActivityListener {
		Intent getIntent();

		void setAccountAuthenticatorResult(Bundle bundle);

		void setResult(int result, Intent intent);
	}
}