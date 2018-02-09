package com.suppresswarnings.osgi.alone;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

public class ReadMail {
	private static String host = "pop3.163.com";
	private static String user = "outerman_mail";
	private static String pwd = System.getProperty("mail.passcode");
	private static String dir = "INBOX";
	private static String pop3 = "pop3";
	private static byte[] identify = {111, 117, 116, 101, 114, 109, 97, 110, 106, 105, 97, 109, 105, 110, 103, 64, 49, 54, 51, 46, 99, 111, 109};
	public static List<String> read(int count) {
		List<String> result = new ArrayList<String>();
		Session session = Session.getDefaultInstance(new Properties());
		session.setDebug(false);
		try {
			Store store = session.getStore(pop3);
			store.connect(host, user, pwd);
			Folder folder = store.getFolder(dir);
			folder.open(Folder.READ_ONLY);
			int end = folder.getMessageCount();
			int start = end - count;
			Message[] msgs = folder.getMessages(start, end);
			for(Message msg : msgs) {
				Address[] recipients = msg.getRecipients(RecipientType.TO);
				if(recipients.length < 2) {
					continue;
				}
				InternetAddress[] address = (InternetAddress[]) recipients;
				for(InternetAddress addr : address) {
					String mail = addr.getAddress();
					if(mail.equals(new String(identify, "UTF-8"))) {
						System.out.println("msgid: " + msg.getMessageNumber() + ", ");
						result.add(msg.getSubject());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public static void main(String[] args) {
		List<String> titles = ReadMail.read(2);
		System.out.println(titles);
	}
}
