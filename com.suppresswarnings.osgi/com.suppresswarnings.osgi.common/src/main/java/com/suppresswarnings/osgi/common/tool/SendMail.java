package com.suppresswarnings.osgi.common.tool;

import java.io.File;
import java.util.Properties;
import java.util.Random;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class SendMail {
	private static String host = "smtp.163.com";
	private static String user = "outerman_mail";
	private static String pwd = System.getProperty("mail.passcode");
	private static String from = "outerman_mail@163.com";
	private static String to = "609558729@qq.com";
	private static byte[] identify = {111, 117, 116, 101, 114, 109, 97, 110, 106, 105, 97, 109, 105, 110, 103, 64, 49, 54, 51, 46, 99, 111, 109};
	private static String smtp = "smtp";
	private String subject = "SuppressWarnings";
	public void title(String subject, String content) {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props);
		session.setDebug(true);
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(new String(identify, "UTF-8")));
			message.setSubject(subject);
			message.setText(content);
			
			Transport transport = session.getTransport(smtp);
			transport.connect(host, user, pwd);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void multipart(String content, String attachment) {
		if(attachment == null) return;
		File affix = new File(attachment);
		if (!affix.exists()) return;
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props);
		session.setDebug(false);
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			Multipart multipart = new MimeMultipart();
			BodyPart contentPart = new MimeBodyPart();
			contentPart.setText(content);
			multipart.addBodyPart(contentPart);
			
			BodyPart messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(affix);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(MimeUtility.encodeText(affix.getName()));
			
			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);
			message.saveChanges();

			Transport transport = session.getTransport(smtp);
			transport.connect(host, user, pwd);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SendMail cn = new SendMail();
		Random rand = new Random();
		for(int i : new int[]{1,2,3}) {
			cn.title(cn.subject, "content:" + Long.toHexString(i + rand.nextLong()));
		}
	}
}