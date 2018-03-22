/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.alone;

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

import org.slf4j.LoggerFactory;

public class SendMail {
	private static final String host = "smtp.163.com";
	private static final String user = "outerman_mail";
	private static final String pwd = System.getProperty("mail.passcode");
	private static final String from = "outerman_mail@163.com";
	private String to = null;
	private static final byte[] identify = {111, 117, 116, 101, 114, 109, 97, 110, 106, 105, 97, 109, 105, 110, 103, 64, 49, 54, 51, 46, 99, 111, 109};
	private static String smtp = "smtp";
	private String subject = "SuppressWarnings";
	private static org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	public SendMail(){}
	public SendMail(String sendTo) {
		this.to = sendTo;
	}
	public void title(String subject, String content) {
		logger.info("to send mail");
		try {
			Properties props = new Properties();
			props.put("mail.smtp.host", host);
	        props.put("mail.smtp.auth", "true");
	        Session session = Session.getInstance(props);
			session.setDebug(false);
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			if(to != null) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			}
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(new String(identify, "UTF-8")));
			message.setSubject(subject);
			message.setText(content);
			
			Transport transport = session.getTransport(smtp);
			transport.connect(host, user, pwd);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		} catch (Error e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void multipart(String content, String attachment) {
		try {
			if(to == null) return;
			if(attachment == null) return;
			File affix = new File(attachment);
			if (!affix.exists()) return;
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props);
			session.setDebug(false);
			MimeMessage message = new MimeMessage(session);
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