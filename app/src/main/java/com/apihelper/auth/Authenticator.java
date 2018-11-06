package com.apihelper.auth;

import com.apihelper.Session;

/**
 * @author korotenko. Interface to implement the logic to work with the user
 *         account.
 * 
 */
public interface Authenticator {

	/**
	 * @return true if the user is authorized, false otherwise.
	 */
	boolean hasAccountData();

	/**
	 * @return true if the item was successfully added, false otherwise.
	 */
	boolean addAccount(Session session);

	/**
	 * @return AccountData if the item was successfully removed.
	 */
    Session removeAccount();

	/**
	 * @return AccountData if the user is authorized.
	 */
    Session getAccountData();
}
