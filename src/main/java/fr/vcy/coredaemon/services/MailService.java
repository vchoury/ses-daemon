package fr.vcy.coredaemon.services;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pcuisinaud
 */
public class MailService {

    public static final Logger LOGGER = LoggerFactory.getLogger(MailService.class.getName());
    
    public static final String ENV = "vcy.env";
    public static final String APP = "vcy.base.name";
    public static final String MAIL_FROM = "vcy.mail.from";
    public static final String MAIL_TO = "vcy.mail.to";
    
    private MailService() { }
    
    public static void sendMailToAdminQuietly(String subject, String body, Throwable t) {
        try {
            sendMailToAdmin(subject, body, t);
        } catch (MessagingException ex) {
            LOGGER.error("Mail non envoyé. Non bloquant.", ex);
        }
    }
    
    public static boolean sendMailToAdmin(String subject, String body, Throwable t) throws MessagingException {
        String fullSubject = "[" + System.getProperty(APP) + "] {" + System.getProperty(ENV) + "} ";
        if (subject == null && t != null) {
            fullSubject += ExceptionUtils.getMessage(t);
        } else {
            fullSubject += subject;
        }
        StringBuilder fullBody = new StringBuilder();
        if (body != null) {
            fullBody.append(body).append(" \n");
        }
        if (t != null) {
            fullBody.append(" \n").append(ExceptionUtils.getMessage(t));
            fullBody.append(" \n").append(ExceptionUtils.getStackTrace(t));
        }
        fullBody.append(" \n");
        return send(System.getProperty(MAIL_FROM), new String[]{System.getProperty(MAIL_TO)}, 
                null, fullSubject, fullBody.toString(), "text/plain; charset=utf-8", null, null);
    }

    public static boolean sendMail(String[] to, String subject, String text) throws MessagingException {
        return send(System.getProperty(MAIL_FROM), to, null, subject, text, "text/plain; charset=utf-8", null, null);
    }

    public static boolean send(String from, String[] to, String[] cc, String subject, String text, String mimeType, String filename, DataSource datasource) throws MessagingException {
        LOGGER.debug("Sending mail " + subject);
        Session session = Session.getDefaultInstance(System.getProperties());
        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);
        // Set From: header field of the header.
        message.setFrom(new InternetAddress(from));
        // Set To: header field of the header.
        for (String addr : to) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(addr));
        }
        if (cc != null) {
            for (String addr : cc) {
                message.addRecipient(Message.RecipientType.CC, new InternetAddress(addr));
            }
        }
        // Set Subject: header field
        message.setSubject(subject, "UTF-8");
        // if attachment
        if (filename != null && datasource != null) {
            // Create the message part 
            BodyPart messageBodyPart = new MimeBodyPart();
            // Fill the message
            messageBodyPart.setContent(text, mimeType);
            // Create a multipar message
            Multipart multipart = new MimeMultipart();
            // Set text message part
            multipart.addBodyPart(messageBodyPart);
            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);
            // Send the complete message parts
            message.setContent(multipart);
        } else {
            // Just set the actual message
            message.setContent(text, mimeType);
        }
        // Send message
        Transport.send(message);
        return true;
    }
}
