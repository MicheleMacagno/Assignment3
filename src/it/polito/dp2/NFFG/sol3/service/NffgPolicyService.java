package it.polito.dp2.NFFG.sol3.service;

import java.io.File;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
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
	
	
	public static synchronized XNffgs getXNffgs(){
		XNffgs returnXNffgs= new XNffgs();
		mapXNffg.values().stream().forEach(n->{
			returnXNffgs.getNffg().add(n);
		});
		return returnXNffgs;
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
	 * Throws a NotFoundException if the policy does not exist.
	 */
	public static synchronized XPolicy getXPolicyByName(String name){
		if(mapXPolicy.containsKey(name)){
			return mapXPolicy.get(name);
		}
		else{
			throw new NotFoundException("The requested Policy is not existing", Response.status(404).entity("The requested Policy is not existing").build());
		}
	}
	
	public static synchronized XPolicies getXPolicies(){
		XPolicies rxpolicies = new XPolicies();
		mapXPolicy.values().stream().forEach(p->{
			rxpolicies.getPolicy().add(p);
		});
		return rxpolicies;
	}
	
	

	
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
		});
		
		return returnedXNffgs;
	}
	
	public static synchronized void deleteAllXNffgs(){
		mapXNffg.clear();
		//when removing an nffg also the relative policies are removed
		deleteAllPolicies();
	}
	
	public static synchronized XNffg deleteNffgByName(String name,String delete_policy) throws NotFoundException,ForbiddenException{
		
		if(!mapXNffg.containsKey(name)){
			throw new NotFoundException(Response.status(404).entity("The requested Nffg does not exists! Impossible to remove it").build());
		}
		
		XNffg xnffg = mapXNffg.get(name);
		if(delete_policy.equals("y")){
				mapXNffg.remove(name);
				//delete related Policy of the given nffg
				mapXPolicy.values().stream().filter(p->{
					if(p.getNffg().equals(xnffg.getName())){
						return true;
					}
					else{
						return false;
					}
				}).forEach(p->{
					mapXPolicy.remove(p.getName());
				});
				return xnffg;
			
		}else if(delete_policy.equals("n")){
			int size = 
				mapXPolicy.values().stream().filter(p->{
					if(p.getNffg().equals(xnffg.getName())){
						return true;
					}
					else{
						return false;
					}
				}).collect(Collectors.toList()).size();
			if(size==0){
				mapXNffg.remove(name);
				return xnffg;
			}
			else{
				throw new ForbiddenException("Impossible to delete the nffg - at least one policy referring to it exists",Response.status(403).entity("Impossible to delete the nffg - at least one policy referring to it exists").build());
			}
		}
		else{
			throw new ForbiddenException("You must specify either y/n for delpolicy parameter",
					Response.status(403).entity("You must specify either y/n for delpolicy parameter").build());
		}
	}
	
	//TODO: manage policy not existing or nffg not existing
	public static synchronized XPolicies addXPolicies(XPolicies xpolicies) {
		try{
			XPolicies returnedXPolicies = new XPolicies();
			xpolicies.getPolicy().forEach(p->{
				if(mapXPolicy.containsKey(p.getName())){
					System.out.println("At least one policy in the set is already existing");
					throw new ForbiddenException("At least one policy in the set is already existing",Response.status(403).entity("At least one policy in the set is already existing").build());
				}
				if(!mapXNffg.containsKey(p.getNffg())){
					System.out.println("The nffg corresponding to the policy is not existing");
					throw new ForbiddenException("The nffg corresponding to the policy is not existing",Response.status(403).entity("The nffg corresponding to the policy is not existing").build());
				}
				
			});
			
			List<XPolicy> list = returnedXPolicies.getPolicy();
			xpolicies.getPolicy().forEach(p->{
				list.add(addXPolicyVerifyXNffg(p));
			});
			
			return returnedXPolicies;
		}catch(NullPointerException e){
			e.printStackTrace();
			throw new NotFoundException("NullPointerException",Response.status(404).entity("Error- null found").build());
		}
	}
	
	public static synchronized void deleteAllPolicies(){
		mapXPolicy.clear();
	}
	
	public static synchronized XPolicy deletePolicyByName(String name) throws NotFoundException{
		if(mapXPolicy.containsKey(name)){
			XPolicy xpolicy = mapXPolicy.get(name);
			mapXPolicy.remove(name);
			return xpolicy;
		}
		else{
			throw new NotFoundException(Response.status(404).entity("The requested policy does not exists! Impossible to remove it").build());
		}
	}
	
	public static synchronized XPolicy updatePolicyByName(String name,XPolicy xpolicy) throws NotFoundException,ForbiddenException{
		xpolicy.setName(name);
		if(!mapXNffg.containsKey(xpolicy.getNffg())){
			throw new ForbiddenException("Error - Nffg name not existing",Response.status(403).entity("Error - Nffg name not existing").build());
		}
		if(mapXPolicy.containsKey(name)){
			mapXPolicy.put(name,xpolicy);
			return xpolicy;
		}else{
			throw new NotFoundException("The policy is not existing, impossible to update it",Response.status(404).entity("The policy is not existing, impossible to update it").build());
		}
	}
	
	public static synchronized XPolicies verifyPolicies(XVerificationRequest xvr){
		XPolicies xpolicies = new XPolicies();
		xvr.getPolicyname().forEach(p->{
			if(!mapXPolicy.containsKey(p)){
				throw new NotFoundException("Error - At least one policy for which you are requiring the verification is not existing.",
						Response.status(404).entity("Error - At least one policy for which you are requiring the verification is not existing.").build());
			}
			//TODO: check about TRAVERSAL POLICY/ only reachability must be verified
		});
		
		//TODO: verify using neo4j
		return null;
	}
	
	
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