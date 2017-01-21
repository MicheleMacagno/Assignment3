package it.polito.dp2.NFFG.sol3.service;
//This validator performs JAXB unmarshalling with validation
//against the schema
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

@Provider
@Consumes(MediaType.APPLICATION_XML)
public class NffgPolicyValidationProvider implements MessageBodyReader<JAXBElement<?>> {
	final String jaxbPackage = "it.polito.dp2.NFFG.sol3.bindings"; //the package I use for validation
	Unmarshaller unmarshaller;
	Logger logger;

	public NffgPolicyValidationProvider() {
		logger = Logger.getLogger(NffgPolicyValidationProvider.class.getName());

		try {				
			InputStream schemaStream = NffgPolicyValidationProvider.class.getResourceAsStream("/xsd/nffgVerifier.xsd");
			if (schemaStream == null) {
				logger.log(Level.SEVERE, "xml schema file Not found.");
				throw new IOException();
			}
         JAXBContext jc = JAXBContext.newInstance( jaxbPackage );
         unmarshaller = jc.createUnmarshaller();
         SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
         Schema schema = sf.newSchema(new StreamSource(schemaStream));
         unmarshaller.setSchema(schema);
         logger.log(Level.INFO, "NffgPolicyProvider initialized successfully");
		} catch (SAXException | JAXBException | IOException se) {
			logger.log(Level.SEVERE, "Error parsing xml directory file. Service will not work properly.", se);
		}
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		logger.log(Level.INFO,"IsReadable called");
		return JAXBElement.class.equals(type) || jaxbPackage.equals(type.getPackage().getName());
	}

	@Override
	public JAXBElement<?> readFrom(Class<JAXBElement<?>> type, Type genericType, Annotation[] annotations, MediaType mediaType,

			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		logger.log(Level.INFO,"ReadFrom called");
		try {
			return (JAXBElement<?>) unmarshaller.unmarshal(entityStream);
		} catch (JAXBException ex) {
			logger.log(Level.WARNING, "Request body validation error.", ex);
			Throwable linked = ex.getLinkedException();
			String validationErrorMessage = "Request body validation error";
			if (linked != null && linked instanceof SAXParseException){
				validationErrorMessage += ": " + linked.getMessage();
			}
			
			//in case of failure in validation, throw an exception
			//Error 400 returned
			throw new BadRequestException("Request body validation error", 
					Response.status(400).entity("Error 400 - Error while validating against the schema\n" + validationErrorMessage).build());
		}
		catch(Exception e){
			//in case of failure in validation, throw an exception
			String validationErrorMessage = "Request body validation error";
			throw new BadRequestException("Request body validation error", 
					Response.status(400).entity("Error 400 - Error while validating against the schema\n" + validationErrorMessage).build());
		}
	}

}
