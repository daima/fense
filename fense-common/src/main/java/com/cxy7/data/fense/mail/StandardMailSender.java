package com.cxy7.data.fense.mail;

import org.apache.commons.lang3.StringUtils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;


/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
public class StandardMailSender {
	/**
	 * 以文本格式发送邮件
	 *
	 * @param mailInfo
	 *            待发送的邮件的信息
	 */
	public boolean sendTextMail(MailSenderInfo mailInfo) {
		Session sendMailSession = createSession(mailInfo);
		try {
			// 根据session创建一个邮件消息
			Message mailMessage = new MimeMessage(sendMailSession);
			// 创建邮件发送者地址
			Address from = new InternetAddress(mailInfo.getFromAddress());
			// 设置邮件消息的发送者
			mailMessage.setFrom(from);
			// 创建邮件的接收者地址，并设置到邮件消息中
			Address to = new InternetAddress(mailInfo.getToAddress());
			mailMessage.setRecipient(Message.RecipientType.TO, to);
			// 设置邮件消息的主题
			mailMessage.setSubject(mailInfo.getSubject());
			// 设置邮件消息发送的时间
			mailMessage.setSentDate(new Date());
			// 设置邮件消息的主要内容
			String mailContent = mailInfo.getContent();
			mailMessage.setText(mailContent);
			// 发送邮件
			Transport.send(mailMessage);
			return true;
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	/**
	 * 以HTML格式发送邮件
	 *
	 * @param mailInfo
	 *            待发送的邮件信息
	 * @throws Exception
	 */
	public static boolean sendHtmlMail(MailSenderInfo mailInfo) throws Exception {
		Session sendMailSession = createSession(mailInfo);
		try {
			// 根据session创建一个邮件消息
			Message mailMessage = new MimeMessage(sendMailSession);
			// 创建邮件发送者地址
			String nike = null;
			try {
				nike = MimeUtility.encodeText(mailInfo.getFromNickName());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			Address fromAddress = new InternetAddress(nike + " <" + mailInfo.getFromAddress() + ">");
			// 设置邮件消息的发送者
			mailMessage.setFrom(fromAddress);
			// 创建邮件的接收者地址，并设置到邮件消息中
			String[] tos = mailInfo.getToAddress().split(";|,");
			InternetAddress[] address = new InternetAddress[tos.length];
			for (int i = 0; i < tos.length; i++) {
				address[i] = new InternetAddress(tos[i]);
			}
			// Message.RecipientType.TO属性表示接收者的类型为TO
			mailMessage.addRecipients(Message.RecipientType.TO, address);
			if (StringUtils.isNotBlank(mailInfo.getCcAddress())) {
				String[] ccs = mailInfo.getCcAddress().split(";|,");
				InternetAddress[] ccAddress = new InternetAddress[ccs.length];
				for (int i = 0; i < ccs.length; i++) {
					ccAddress[i] = new InternetAddress(ccs[i]);
				}
				mailMessage.addRecipients(Message.RecipientType.CC, ccAddress);
			}
			// 设置邮件消息的主题
			mailMessage.setSubject(mailInfo.getSubject());
			// 设置邮件消息发送的时间
			mailMessage.setSentDate(new Date());

			MimeMultipart allPart = new MimeMultipart("mixed");

			// 创建邮件的各个 MimeBodyPart 部分
			String[] files = mailInfo.getAttachFileNames();
			if (files != null) {
				for (String fileName : files) {
					MimeBodyPart attachment = createAttachment(fileName);
					allPart.addBodyPart(attachment);
				}
			}
			MimeBodyPart content = createContent(mailInfo.getContent(), mailInfo.getBodyImage());
			// 将邮件中各个部分组合到一个"mixed"型的 MimeMultipart 对象
			allPart.addBodyPart(content);

			// 将上面混合型的 MimeMultipart 对象作为邮件内容并保存
			mailMessage.setContent(allPart);
			mailMessage.saveChanges();
			// 发送邮件
			Transport.send(mailMessage);
			return true;
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	/**
	 * 根据传入的邮件正文body和文件路径创建图文并茂的正文部分
	 */
	public static MimeBodyPart createContent(String body, String fileName) throws Exception {
		// 用于保存最终正文部分
		MimeBodyPart contentBody = new MimeBodyPart();
		// 用于组合文本和图片，"related"型的MimeMultipart对象
		MimeMultipart contentMulti = new MimeMultipart("related");

		// 正文的文本部分
		MimeBodyPart textBody = new MimeBodyPart();
		textBody.setContent(body, "text/html;charset=gbk");
		contentMulti.addBodyPart(textBody);

		// 正文的图片部分
		if (StringUtils.isNotBlank(fileName)) {
			MimeBodyPart jpgBody = new MimeBodyPart();
			FileDataSource fds = new FileDataSource(fileName);
			jpgBody.setDataHandler(new DataHandler(fds));
			jpgBody.setContentID("logo_jpg");
			contentMulti.addBodyPart(jpgBody);
		}

		// 将上面"related"型的 MimeMultipart 对象作为邮件的正文
		contentBody.setContent(contentMulti);
		return contentBody;
	}

	/**
	 * 根据传入的文件路径创建附件并返回
	 */
	public static MimeBodyPart createAttachment(String fileName) throws Exception {
		MimeBodyPart attachmentPart = new MimeBodyPart();
		FileDataSource fds = new FileDataSource(fileName);
		attachmentPart.setDataHandler(new DataHandler(fds));
		String filename = MimeUtility.encodeText(fds.getName());
		filename = filename.replaceAll("\r", "").replaceAll("\n", "");
		attachmentPart.setFileName(filename);
		return attachmentPart;
	}

	private static Session createSession(MailSenderInfo mailInfo) {
		// 判断是否需要身份认证
		MyAuthenticator authenticator = null;
		Properties pro = mailInfo.getProperties();
		// 如果需要身份认证，则创建一个密码验证器
		if (mailInfo.isAuth()) {
			authenticator = new MyAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
		}
		// 根据邮件会话属性和密码验证器构造一个发送邮件的session
		Session sendMailSession = Session.getDefaultInstance(pro, authenticator);
		return sendMailSession;
	}

}
