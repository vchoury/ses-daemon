package fr.vcy.coredaemon.camel;

import fr.vcy.coredaemon.services.MailService;

import javax.mail.MessagingException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vchoury
 */
public class ExceptionService {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(ExceptionService.class.getName());
    
    public void process(Exchange exchange, Message message) {
        try {
            String filename = message.getHeader(Exchange.FILE_NAME_ONLY, "<CamelFileNameOnly inconnu!>", String.class);
            String subject = "Erreur fichier : " + filename;
            String details = "FilePath = " + message.getHeader(Exchange.FILE_PATH, "<CamelFilePath inconnu!>", String.class)+ "\n";
            details += "FileLength = " + message.getHeader(Exchange.FILE_LENGTH, "<CamelFileLength inconnu!>", String.class)+ "\n";
            details += "MessageId = " + message.getMessageId()+ "\n";
            Exception ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
            if (ex == null) {
                ex = exchange.getException();
            }
            LOGGER.error(subject + "\n" + details, ex);
            MailService.sendMailToAdmin(subject, details, ex);
        } catch (MessagingException ex1) {
            LOGGER.error("Erreur lors de la prise en compte de l'exception", ex1);
        }
    }
    
}
