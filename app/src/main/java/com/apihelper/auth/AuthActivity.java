package com.apihelper.auth;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import com.apihelper.Session;

public abstract class AuthActivity extends AppCompatAccountAuthenticatorActivity {
    private AuthActivityPhantom activityPhantom = new AuthActivityPhantom(this,
            new AuthActivityPhantom.AuthActivityListener() {

                @Override
                public void setResult(int result, Intent intent) {
                    AuthActivity.this.setResult(result, intent);

                }

                @Override
                public void setAccountAuthenticatorResult(Bundle bundle) {
                    AuthActivity.this.setAccountAuthenticatorResult(bundle);

                }

                @Override
                public Intent getIntent() {
                    return AuthActivity.this.getIntent();
                }
            });

    public AccountManager getAccountManager() {
        return activityPhantom.getAccountManager();
    }

    public String getmAuthTokenType() {
        return activityPhantom.getAuthTokenType();
    }

    public String getAccountName() {
        return activityPhantom.getAccountName();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPhantom.onCreate(savedInstanceState);
    }

    /**
     * Called when user finish login
     *
     * @param session
     */
    public void finishLogin(Session session) {
        activityPhantom.finishLogin(session);
    }

}