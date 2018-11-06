package com.apihelper.auth;

import android.accounts.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.apihelper.Session;
import com.apihelper.utils.L;

public class AppAuthenticator extends AbstractAccountAuthenticator implements
        Authenticator {

    private final Context mContext;
    private final Class<?> mAuthActivityClass;

    /**
     * TODO
     *
     * @return
     */
    public String getAccountType() {
        return mContext.getPackageName();
    }

    public AppAuthenticator(Context context, Class<?> authActivityClass) {
        super(context);
        if (context == null) {
            throw new IllegalArgumentException(
                    "You must specify arguments. Context can not be null.");
        }
        mContext = context;
        mAuthActivityClass = authActivityClass;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
                             String accountType, String authTokenType,
                             String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        final Bundle bundle = new Bundle();
        if (mAuthActivityClass != null) {
            final Intent intent = new Intent(mContext, mAuthActivityClass);
            intent.putExtra(Session.ARG_ACCOUNT_TYPE, accountType);
            intent.putExtra(Session.ARG_AUTH_TYPE, authTokenType);
            intent.putExtra(Session.ARG_IS_ADDING_NEW_ACCOUNT, true);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                    response);

            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        }
        return bundle;
    }

    /**
     * TODO
     *
     * @return
     */
    @Override
    public boolean hasAccountData() {
        AccountManager accountManager = AccountManager.get(mContext);

        Account userAccount = getAvailableAccount(accountManager);
        if (userAccount != null) {
            String userLogin = userAccount.name;
            String refreshTocken = accountManager.getPassword(userAccount);

            if (!TextUtils.isEmpty(userLogin)
                    && !TextUtils.isEmpty(refreshTocken)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addAccount(Session session) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Session getAccountData() {
        Session session = null;
        AccountManager accountManager = AccountManager.get(mContext);

        Account userAccount = getAvailableAccount(accountManager);
        if (userAccount != null) {
            String userLogin = userAccount.name;
            String refreshToken = accountManager.getPassword(userAccount);
            String accessToken = accountManager.getUserData(userAccount, Session.ACCESS_TOKEN_KEY);
            String tokenType = accountManager.getUserData(userAccount, Session.TOKEN_TYPE_KEY);
            String updateTimeString = accountManager.getUserData(userAccount, Session.UPDATE_TIME_KEY);
            long updateTime = 0;
            if (!TextUtils.isEmpty(updateTimeString)) {
                updateTime = Long.parseLong(updateTimeString);
            }
            session = new Session(userLogin, refreshToken);
            session.refresh(accessToken, tokenType, updateTime);
        }
        return session;
    }

    @Override
    public Session removeAccount() {
        Session session = null;
        AccountManager accountManager = AccountManager.get(mContext);

        Account userAccount = getAvailableAccount(accountManager);
        if (userAccount != null) {
            session = new Session(userAccount.name, accountManager.getPassword(userAccount));
            accountManager.removeAccount(userAccount, null, null);
            return session;
        }
        return session;
    }

    private Account getAvailableAccount(AccountManager accountManager) {
        Account userAccount = null;
        final Account[] availableAccounts = accountManager
                .getAccountsByType(getAccountType());
        if (availableAccounts.length > 0) {
            userAccount = availableAccounts[availableAccounts.length - 1];
        }
        return userAccount;
    }

    //TODO
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
                               Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {

        // If the caller requested an authToken type we don't support, then
        // return an error
        if (!authTokenType.equals(AuthActivityPhantom.AUTHTOKEN_TYPE_READ_ONLY)
                && !authTokenType
                .equals(AuthActivityPhantom.AUTHTOKEN_TYPE_FULL_ACCESS)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE,
                    "invalid authTokenType");
            return result;
        }

//		ApiHelper.refreshToken();
//
//		// If we get an authToken - we return it
//		if (BehaviorMediator.isSessionValid()) {
//			return ApiHelper.getSession().getAccountBundle(account);
//		}

        final Bundle bundle = new Bundle();
        if (mAuthActivityClass != null) {
            // If we get here, then we couldn't access the user's password - so
            // we
            // need to re-prompt them for their credentials. We do that by
            // creating
            // an intent to display our AuthenticatorActivity.
            final Intent intent = new Intent(mContext, mAuthActivityClass);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                    response);
            intent.putExtra(Session.ARG_ACCOUNT_TYPE, account.type);
            intent.putExtra(Session.ARG_AUTH_TYPE, authTokenType);

            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        }
        return bundle;
    }

    //TODO
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (AuthActivityPhantom.AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
            return AuthActivityPhantom.AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
        else if (AuthActivityPhantom.AUTHTOKEN_TYPE_READ_ONLY.equals(authTokenType))
            return AuthActivityPhantom.AUTHTOKEN_TYPE_READ_ONLY_LABEL;
        else
            return authTokenType + " (Label)";
    }

    //TODO
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
                              Account account, String[] features) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
                                 String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
                                     Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
                                    Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        return null;
    }
}
