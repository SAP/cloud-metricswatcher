package com.sap.hana.cloud.samples.mail;

import java.util.Properties;
import org.apache.log4j.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mailsender {
	private static final Logger LOGGER = Logger.getLogger(Mailsender.class);
	private static final String FROM = "my_email_account@email.com";
	private static final String userName = "my_email_account";
	private static final String password = "my_email_password";

	public static void sendEmail(String to, String subject, String body) throws AddressException, MessagingException {
		// Set up the mail server.
		Properties properties = new Properties();
		properties.setProperty("mail.transport.protocol", "smtp");
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.starttls.enable", "true");
		properties.setProperty("mail.smtp.port", "587");
		properties.setProperty("mail.smtp.host", "smtp.email.com");
		properties.setProperty("mail.smtp.host", "mail.email.com");

		Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		});

		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(FROM));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

		message.setSubject(subject);
		message.setText(body);

		// Send message
		Transport.send(message);
		LOGGER.info("Email sent successfully....");
	}
}