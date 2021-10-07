package com.cxy7.data.fense.mail;

import java.util.Properties;


/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
public class MailSenderInfo {
//	public static final String SOCKET_FACTORY_PORT = "mail.smtp.socketFactory.port";
//	public static final String TLS_ENABLE = "mail.smtp.starttls.enable";
//	public static final String AUTH = "mail.smtp.auth";
//	public static final String PORT = "mail.smtp.port";
//	public static final String HOST = "mail.smtp.host";
	// 发送邮件的服务器的IP和端口
	private String host;

	private String port = "25";
	// 邮件发送者的地址
	private String fromAddress;
	private String fromNickName;
	// 邮件接收者的地址
	private String toAddress;
	// 抄送者
	private String ccAddress;
	// 登陆邮件发送服务器的用户名和密码
	private String userName;
	private String password;
	// 是否需要身份验证
	private boolean auth = false;
	private boolean tlsEnable = false;
	private int socketFactoryPort = 465;
	// 邮件主题
	private String subject;
	// 邮件的文本内容
	private String content;
	// 邮件附件的文件名
	private String[] attachFileNames;
	private String bodyImage;

	/**
	 * 获得邮件会话属性
	 */
	public Properties getProperties() {
		Properties props = new Properties();
		props.put("mail.smtp.host", getHost());
		props.put("mail.smtp.auth", isAuth());

		if (isTlsEnable()) {
			props.put("mail.smtp.starttls.enable", true);
			props.put("mail.smtp.socketFactory.port", getSocketFactoryPort());
		} else
			props.put("mail.smtp.port", this.port);
		return props;
	}

	public String getBodyImage() {
		return bodyImage;
	}

	public void setBodyImage(String bodyImage) {
		this.bodyImage = bodyImage;
	}

	public String getFromNickName() {
		return fromNickName;
	}

	public void setFromNikeName(String fromNickName) {
		this.fromNickName = fromNickName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public boolean isAuth() {
		return auth;
	}

	public void setAuth(boolean auth) {
		this.auth = auth;
	}

	public int getSocketFactoryPort() {
		return socketFactoryPort;
	}

	public void setSocketFactoryPort(int socketFactoryPort) {
		this.socketFactoryPort = socketFactoryPort;
	}

	public boolean isTlsEnable() {
		return tlsEnable;
	}

	public void setTlsEnable(boolean tlsEnable) {
		this.tlsEnable = tlsEnable;
	}

	public String[] getAttachFileNames() {
		return attachFileNames;
	}

	public void setAttachFileNames(String[] fileNames) {
		this.attachFileNames = fileNames;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getCcAddress() {
		return ccAddress;
	}

	public void setCcAddress(String ccAddress) {
		this.ccAddress = ccAddress;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String textContent) {
		this.content = textContent;
	}
}
