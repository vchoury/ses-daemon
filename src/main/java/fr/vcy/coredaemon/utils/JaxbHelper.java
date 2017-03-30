package fr.vcy.coredaemon.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 *
 * @author vchoury
 */
public class JaxbHelper implements ValidationEventHandler {

    private TraitementException validationException;

    public boolean handleEvent(ValidationEvent event) {
        switch (event.getSeverity()) {
            case ValidationEvent.WARNING:
                return true; // continue after warnings
            default:
                validationException = new TraitementException("Echec de la validation XSD : " + event.getMessage(), event.getLinkedException());
                if (event.getLocator() != null) {
                    if (event.getLocator().getLineNumber() > 1) {
                        validationException.setLigne(event.getLocator().getLineNumber());
                    }
                    if (event.getLocator().getColumnNumber() > 1) {
                        validationException.setColonne(event.getLocator().getColumnNumber());
                    }
                    if (event.getLocator().getNode() != null) {
                        validationException.setBaliseEnErreur(event.getLocator().getNode().getNodeName());
                        validationException.setValeurEnErreur(event.getLocator().getNode().getNodeValue());
                    }
                }
                return false;
        }

    }

    public <T> void marshall(OutputStream output, JAXBElement<T> obj, String pkg, String schemaPath) throws TraitementException {
        try {
            JAXBContext jc = JAXBContext.newInstance(pkg);
            Marshaller m = jc.createMarshaller();
            if (schemaPath != null) {
                SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
                m.setSchema(sf.newSchema(new File(schemaPath)));
                m.setEventHandler(this);
            }
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(obj, output);
        } catch (SAXException ex) {
            throw new TraitementException("Schema inconnu : " + schemaPath, ex);
        } catch (JAXBException ex) {
            if (validationException != null) {
                throw validationException;
            } else {
                throw new TraitementException("Marshalling exception", ex);
            }
        }
    }
    
    public <T> JAXBElement<T> unmarshall(InputStream input, String pkg, String schemaPath) throws TraitementException {
        try {
            JAXBContext jc = JAXBContext.newInstance(pkg);
            Unmarshaller m = jc.createUnmarshaller();
            if (schemaPath != null) {
                SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
                m.setSchema(sf.newSchema(new File(schemaPath)));
                m.setEventHandler(this);
            }
            return (JAXBElement<T>) m.unmarshal(input);
        } catch (SAXException ex) {
            throw new TraitementException("Schema inconnu : " + schemaPath, ex);
        } catch (JAXBException ex) {
            if (validationException != null) {
                throw validationException;
            } else {
                throw new TraitementException("Marshalling exception", ex);
            }
        }
    }
}
