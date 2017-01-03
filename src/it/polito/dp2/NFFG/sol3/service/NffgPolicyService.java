package it.polito.dp2.NFFG.sol3.service;

import java.io.File;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.validation.SchemaFactory;
import javax.xml.ws.WebServiceException;

import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.sol3.bindings.*;

//TODO: verify the set schema, is it correct? - it always throws exceptions

public class NffgPolicyService {
	static ConcurrentHashMap<String,XNffg> mapXNffg = NffgDB.getMapXNffg();
	static ConcurrentHashMap<String,XPolicy> mapXPolicy = NffgDB.getMapXPolicy();
	
	public static synchronized XNffg getXNffgByName(String name){
		if(mapXNffg.containsKey(name)){
			return mapXNffg.get(name);
		}
		else{
			System.out.println("Nffg not found in the database");
			throw new NotFoundException("msg Nffg not found in the database",Response.status(404).entity("Nffg not found in the database").build());
//			return null;
		}
	}
	
//	/**
//	 * Method receiving an XNffg and storing it in the Database
//	 * @param nffg
//	 * @return
//	 * true:  successful creation
//	 * false: creation failed
//	 */
//	public static synchronized Integer addXNffg(XNffg nffg){
//		try{
//			if(mapXNffg.putIfAbsent(nffg.getName(), nffg) == null){
//				//able to insert the element in the map
//				System.out.println("Nffg corretly inserted in the map");
//				return 0;
//			}
//			else{
//				System.out.println("Error - Nffg name already existing");
//				return -1;
//			}
//		}catch(Exception e){
//			//the put can throw many exceptions
//			System.out.println("Error - exception " +e.getStackTrace());
//			return -2;
//		}
//	}
	
	/**
	 * Method receiving an XNffg and storing it in the Database
	 * @param nffg
	 * @return
	 * true:  successful creation
	 * false: creation failed
	 */
	public static synchronized XNffg addXNffg(XNffg nffg){
			if(mapXNffg.putIfAbsent(nffg.getName(), nffg) == null){
				//able to insert the element in the map
				System.out.println("Nffg corretly inserted in the map");
				Calendar c = Calendar.getInstance();
				c.setTimeZone(TimeZone.getDefault());
				nffg.setLastUpdate(NffgPolicyService.convertCalendar(c));
				return nffg;
			}
			else{
				System.out.println("Error - Nffg name already existing");
				throw new ForbiddenException("Error - Nffg name already existing",Response.status(403).entity("Error - Nffg name already existing").build());
//				return null;
			}
	}
	
	/**
	 * 
	 * @param name
	 * @return the XPolicy object in case of success
	 */
	public static synchronized XPolicy getXPolicyByName(String name){
		if(mapXPolicy.containsKey(name)){
			return mapXPolicy.get(name);
		}
		else{
			throw new NotFoundException("The requested Policy is not existing", Response.status(404).entity("The requested Policy is not existing").build());
		}
	}
	
	
//	/**
//	 * Verify the Nffg is existing and then add the policy in the database
//	 * (in case the policy is not already existing)
//	 * @param policy
//	 * @return
//	 * -1 the Nffg is not existing
//	 * -2 the Policy is already existing
//	 * -3 other errors
//	 */
//	public static synchronized Integer addXPolicyVerifyXNffg(XPolicy policy){
//		
//		try{
//			//verify the Nffg relative to the policy is existing
//			if(mapXNffg.containsKey(policy.getNffg())){
//				System.out.println("Error - Policy already existing");
//				return -1;
//			}
//			else{
//				if(mapXPolicy.putIfAbsent(policy.getName(), policy) == null){
//					//able to insert the element in the map
//					return 0;
//				}
//				else{
//					return -2;
//				}
//			}
//			
//		}catch(Exception e){
//			//the put can throw many exceptions
//			return -3;
//		}
//	}
	
	/**
	 * Verify the Nffg is existing and then add the policy in the database
	 * (in case the policy is not already existing)
	 * @param policy
	 * @return
	 * The policy in case of success
	 * throws an exception in case of failure
	 * 
	 */
	//TODO: add conformity check to xml 
	public static synchronized XPolicy addXPolicyVerifyXNffg(XPolicy policy){
		
			if(!mapXNffg.containsKey(policy.getNffg())){
				System.out.println("Error - Nffg not existing");
				throw new ForbiddenException("Error - Policy name Already existing",Response.status(403).entity("Error - Policy name Already existing").build());
			}
			else{
				if(mapXPolicy.putIfAbsent(policy.getName(), policy) == null){
					//able to insert the element in the map
					return policy;
				}
				else{
					System.out.println("Error - Policy already existing");
					throw new ForbiddenException("Error - Nffg name not existing",Response.status(403).entity("Error - Nffg name not existing").build());
					
				}
			}
	}
	
	public static synchronized XNffgs addXNffgs(XNffgs xnffgs) throws ForbiddenException{
		
		XNffgs returnedXNffgs = new XNffgs();
		xnffgs.getNffg().forEach(n->{
			if(mapXNffg.containsKey(n.getName())){
				System.out.println("At least one of the Nffg in the set is already existing");
				throw new ForbiddenException("At least one of the Nffg in the set is already existing",Response.status(403).entity("At least one of the Nffg in the set is already existing").build());
			}
		});
		
		List<XNffg> list = returnedXNffgs.getNffg();
		xnffgs.getNffg().forEach(n->{
			list.add(addXNffg(n));
//			returned.getNffg().add(addXNffg(n));
		});
		
		return returnedXNffgs;
		
	}
	
	
//	//TODO: decide the error value
//	public Response unmarshalNffg(String xml){
//		JAXBContext jc;
//		Unmarshaller u;
//		XNffg objectXNffg;
//		
//		try {
//			jc = JAXBContext.newInstance("it.polito.dp2.NFFG.sol3.bindings");
//			SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
//			u = jc.createUnmarshaller();
//			try{
////				u.setSchema(sf.newSchema(new File("xsd/nffgInfo.xsd")));
//			}catch(Exception e){
//				e.printStackTrace();
//				return Response.status(404).entity(new String("Error Occurred: -1\n" + e.getMessage() +"\nVerify the Nffg data are correctly written" )).build(); 
//			}
//			StringReader sr = new StringReader(xml);
//			// Unmarshal Shipping Address
//	        JAXBElement<XNffg> je = (JAXBElement<XNffg>) u.unmarshal(sr);
//	        objectXNffg = je.getValue();
//			if(objectXNffg==null){
//				//TODO: verify if this null is really necessary
//				return Response.status(404).entity(new String("Error Occurred: -2\n" +"\nVerify the Nffg data are correctly written")).build(); 
//			}
//			
//			//TODO: add verification about existence
//			if(NffgPolicyService.addXNffg(objectXNffg)!=null){
//				return Response.status(200).entity("Nffg correctly created!!").build() ;
//			}
//			else{
//				return Response.status(404).entity(new String("Error Occurred: -5\n" +"\nNffg already existing, unable to add it")).build(); 
//			}
//		} catch (JAXBException e) {
//			e.printStackTrace();
//			return Response.status(404).entity(new String("Error Occurred: -3\n" + e.getStackTrace()+"\nVerify the Nffg data are correctly written")).build(); 
//		}catch (Exception e){
//			e.printStackTrace();
//			return Response.status(404).entity(new String("Error Occurred: -4\n" + e.getStackTrace()+"\nVerify the Nffg data are correctly written")).build(); 
//		}
//	}
//	
//	public Response unmarshalNffgs(String xml) {
//		JAXBContext jc;
//		Unmarshaller u;
//		XNffgs objectXNffgs;
//		
//		try {
//			jc = JAXBContext.newInstance("it.polito.dp2.NFFG.sol3.bindings");
//			SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
//			u = jc.createUnmarshaller();
//			try{
////				u.setSchema(sf.newSchema(new File("xsd/nffgInfo.xsd")));
//			}catch(Exception e){
//				e.printStackTrace();
//				return Response.status(404).entity(new String("Error Occurred: -1\n" + e.getMessage() +"\nVerify the set of Nffgs is correctly written and respect the schema" )).build(); 
//			}
//			StringReader sr = new StringReader(xml);
//			// Unmarshal Shipping Address
//	        JAXBElement<XNffgs> je = (JAXBElement<XNffgs>) u.unmarshal(sr);
//	        objectXNffgs = je.getValue();
//			if(objectXNffgs==null){
//				//TODO: verify if this null is really necessary
//				return Response.status(404).entity(new String("Error Occurred: -2\n" +"\nVerify the set of Nffgs is correctly written")).build(); 
//			}
//			
//			//TODO: add verification about existence
//			//successful creation
//			return Response.status(200).entity("Nffgs correctly created!!").build() ;
//		} catch (JAXBException e) {
//			e.printStackTrace();
//			return Response.status(404).entity(new String("Error Occurred: -3\n" + e.getStackTrace()+"\nVerify the set of Nffgs is correctly written")).build(); 
//		}catch (Exception e){
//			e.printStackTrace();
//			return Response.status(404).entity(new String("Error Occurred: -4\n" + e.getStackTrace()+"\nVerify the set of Nffgs is correctly written")).build(); 
//		}
//	}
//
//
//	public Response unmarshalPolicy(String xml){
//		
//		JAXBContext jc;
//		Unmarshaller u;
//		XPolicy objectXPolicy;
//		
//		try {
//			jc = JAXBContext.newInstance("it.polito.dp2.NFFG.sol3.bindings");
//			SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
//			u = jc.createUnmarshaller();
//			try{
////				u.setSchema(sf.newSchema(new File("/xsd/nffgInfo.xsd")));
//			}catch(Exception e){
//				e.printStackTrace();
//				return Response.status(404).entity(new String("Error Occurred: -1\n" + e.getMessage() +"\nSchema error" )).build(); 
//			}
//			StringReader sr = new StringReader(xml);
//			// Unmarshal Shipping Address
//	        JAXBElement<XPolicy> je = (JAXBElement<XPolicy>) u.unmarshal(sr);
//	        objectXPolicy = je.getValue();
//			if(objectXPolicy==null){
//				//TODO: verify if this null is really necessary
//				return Response.status(404).entity(new String("Error Occurred: -2\n" +"\nVerify the set of policies are correctly written")).build(); 
//			}
//			
//			switch(NffgPolicyService.addXPolicyVerifyXNffg(objectXPolicy)){
//			case 0:
//				//successful cretion
//				return Response.status(200).entity("Policies correctly created!!").build() ;
//			case -1:
//				return Response.status(404).entity(new String("Error Occurred: -5\n" +"\nThe Nffg relative to the policy does not exist.")).build(); 
//			case -2:	
//				return Response.status(404).entity(new String("Error Occurred: -6\n" +"\nPolicy with the same name already existing.")).build(); 
//			case -3:
//			default:
//				return Response.status(404).entity(new String("Error Occurred: -7\n" +"\nUnexpected error occurred while inserting data")).build(); 
//					
//			}
//		} catch (JAXBException e) {
//			e.printStackTrace();
//			return Response.status(404).entity(new String("Error Occurred: -3\n" + e.getStackTrace()+"\nVerify the set of policies are correctly written")).build(); 
//		}catch (Exception e){
//			e.printStackTrace();
//			return Response.status(404).entity(new String("Error Occurred: -4\n" + e.getStackTrace()+"\nVerify the set of policies are correctly written")).build(); 
//		}
//	}
//		
//	//TODO: complete verrification
//	public Response unmarshalPolicies(String xml) {
//		JAXBContext jc;
//		Unmarshaller u;
//		XPolicies objectPolicies;
//		
//		try {
//			jc = JAXBContext.newInstance("it.polito.dp2.NFFG.sol3.bindings");
//			SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
//			u = jc.createUnmarshaller();
//			try{
////				u.setSchema(sf.newSchema(new File("xsd/nffgInfo.xsd")));
//			}catch(Exception e){
//				e.printStackTrace();
//				return Response.status(404).entity(new String("Error Occurred: -1\n" + e.getMessage() +"\nVerify the set of policies are correctly written" )).build(); 
//			}
//			StringReader sr = new StringReader(xml);
//			// Unmarshal Shipping Address
//	        JAXBElement<XPolicies> je = (JAXBElement<XPolicies>) u.unmarshal(sr);
//	        objectPolicies = je.getValue();
//			if(objectPolicies==null){
//				//TODO: verify if this null is really necessary
//				return Response.status(404).entity(new String("Error Occurred: -2\n" +"\nVerify the set of policies are correctly written")).build(); 
//			}
//			
//			//TODO: add verification about existence
//			//successful creation
//			return Response.status(200).entity("Policies correctly created!!").build() ;
//		} catch (JAXBException e) {
//			e.printStackTrace();
//			return Response.status(404).entity(new String("Error Occurred: -3\n" + e.getStackTrace()+"\nVerify the set of policies are correctly written")).build(); 
//		}catch (Exception e){
//			e.printStackTrace();
//			return Response.status(404).entity(new String("Error Occurred: -4\n" + e.getStackTrace()+"\nVerify the set of policies are correctly written")).build(); 
//		}
//	}
	
	private static XMLGregorianCalendar convertCalendar (Calendar cal){
		Date calendarDate = cal.getTime();
		
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(calendarDate);
//TODO ENABLE
		c.setTimeZone(cal.getTimeZone());
		XMLGregorianCalendar date2 = null;
		try {
			date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			System.out.println("Error in converting data type");
			e.printStackTrace();
		}
		return date2;
	}
	
	
}