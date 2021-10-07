package com.cxy7.data.fense.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;


/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
public class MyAuthenticator extends Authenticator {
	String userName = null;
	String password = null;

	public MyAuthenticator() {
	}

	public MyAuthenticator(String username, String password) {
		this.userName = username;
		this.password = password;
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(userName, password);
	}
}
