package com.cxy7.data.fense.mail;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Component
public class SimpleMailSender {
	private static Logger logger = LoggerFactory.getLogger(SimpleMailSender.class);
	@Value("${mail.smtp.auth}")
	private boolean auth;
	@Value("${mail.smtp.host}")
	private String host;
	@Value("${mail.smtp.port}")
	private String port;
	@Value("${mail.smtp.from.nikename}")
	private String nikename;
	@Value("${mail.smtp.starttls.enable}")
	private boolean tls_enable;
	@Value("${mail.smtp.socketFactory.port}")
	private int socket_port;
	@Value("${mail.smtp.username}")
	private String username;
	@Value("${mail.smtp.password}")
	private String password;
	@Value("${mail.smtp.from}")
	private String from;

	/**
	 *	使用默认配置发送邮件
	 * @param to	收件人
	 * @param cc	抄送
	 * @param subject	主题
	 * @param content	正文
	 * @param files	附件
	 */
	public void send(String to, String cc, String subject, String content, String files) {
		if (StringUtils.isAnyBlank(to, subject, content)) {
			logger.error("参数错误！to:{}, subject:{}, content:{}", to, subject, content);
			return;
		}
		MailSenderInfo mi = new MailSenderInfo();

		mi.setAuth(auth);
		mi.setHost(host);
		mi.setPort(port);
		mi.setFromNikeName(nikename);
		mi.setTlsEnable(tls_enable);
		if (tls_enable)
			mi.setSocketFactoryPort(socket_port);

		mi.setUserName(username);
		mi.setPassword(password);
		mi.setFromAddress(from);
		mi.setToAddress(to);
		mi.setCcAddress(cc);
		mi.setSubject(subject);
		mi.setContent(content);
		if (StringUtils.isNotBlank(files)) {
			String[] fs = files.split(";|,");
			mi.setAttachFileNames(fs);
		}
		try {
			StandardMailSender.sendHtmlMail(mi);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
