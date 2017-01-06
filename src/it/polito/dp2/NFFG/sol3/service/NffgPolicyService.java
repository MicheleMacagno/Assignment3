package it.polito.dp2.NFFG.sol3.service;

import java.io.File;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAllowedException;
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

import com.sun.jersey.api.client.Client;

import it.polito.dp2.NFFG.LinkReader;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.NodeReader;
import it.polito.dp2.NFFG.lab2.ServiceException;
import it.polito.dp2.NFFG.sol3.bindings.*;

//TODO: verify the set schema, is it correct? - it always throws exceptions

public class NffgPolicyService {
	static ConcurrentHashMap<String,XNffg> mapXNffg = NffgDB.getMapXNffg();
	static ConcurrentHashMap<String,XPolicy> mapXPolicy = NffgDB.getMapXPolicy();
	private static ConcurrentHashMap<String,String> mapNameNodesNeo  = new ConcurrentHashMap<String,String>();
	private static ConcurrentHashMap<String,String> mapNameNodesNeoNffg  = new ConcurrentHashMap<String,String>();
	
	private String baseServiceUrlneo;
	private Client client=null;
	
	public NffgPolicyService(){
		baseServiceUrlneo=System.getProperty("it.polito.dp2.NFFG.lab3.NEO4JURL");
		if(baseServiceUrlneo==null){
			System.out.println("URL of neo4j - system property is not set");;
			baseServiceUrlneo="http://localhost:8081/Neo4JXML/rest";
		}
		//create a client for each different connection to the system
		client = Client.create();
	}
	
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
	

	
	/**
	 * Method receiving an XNffg and storing it in the Database.
	 * Manages the insert into neo4j
	 * @param nffg
	 * @return
	 * true:  successful creation
	 * false: creation failed
	 */
	public static synchronized XNffg addXNffg(XNffg nffg){
			
			NffgPolicyService nps = new NffgPolicyService();
			
//TODO: modify it in order to avoid the deletion
//			if(mapXNffg.putIfAbsent(nffg.getName(), nffg) == null){
			if(!mapXNffg.containsKey(nffg.getName())){	
				//able to insert the element in the map
				
				Calendar c = Calendar.getInstance();
				c.setTimeZone(TimeZone.getDefault());
				nffg.setLastUpdate(NffgPolicyService.convertCalendar(c));
				
				//neo4j
				
				List<Node> listNode = new LinkedList<Node>();
				
				for(XNode n : nffg.getNodes().getNode()){
					System.out.println("Node " + n.getName() + " TO ADD");
					Node node = new Node();
					Property nodeProperty = new Property();
					//
					nodeProperty.setName("name");
					nodeProperty.setValue(n.getName());
					node.getProperty().add(nodeProperty);
					
					
					Node response=null;
					//add node in neo4j service
					try{
							String resourceName = nps.baseServiceUrlneo + "/resource/node/";
							response = nps.client.resource(resourceName)
									.type("application/xml")
									.accept("application/xml")
									.post(Node.class,node);
							System.out.println("Response of server: \t" + response.getId() + response.getProperty().get(0).getValue());
							//create a mapping between name and id of nodes stored in the neo4j service
							System.out.println("Node " + n.getName() + " added");
							mapNameNodesNeo.put(response.getProperty().get(0).getValue(),response.id); 
							listNode.add(response);//to create link with nffg node
							
							
					}catch(Exception e){
						System.out.println("Something was wrong while contacting neo4j to insert a node");
						e.printStackTrace();
						//TODO verify it
						
						//TODO: manage deletion of the node from the map
						throw new NotFoundException("Something was wrong while contacting neo4j to insert a node",
								Response.status(404).entity("Something was wrong while contacting neo4j to insert a node").build());
					}
				}
				
				//--NFFG NODE
				
				//ADDED assigment 3
				//manegement of nffg master node
				Node nodeNffg = new Node();
				Property nodeNffgProperty = new Property();
				nodeNffgProperty.setName("name");
				nodeNffgProperty.setValue(nffg.getName());
				nodeNffg.getProperty().add(nodeNffgProperty);
				
				it.polito.dp2.NFFG.sol3.service.Labels lbl = new it.polito.dp2.NFFG.sol3.service.Labels();
				lbl.value= new LinkedList<String>();
				lbl.value.add(new String("NFFG"));
				nodeNffg.setLabels(lbl);
				
//				nodeNffg.getLabels().getValue().add(new String("NFFG"));
				
				Node response = null;
				try{
					String resourceName = nps.baseServiceUrlneo + "/resource/node/";
					response = nps.client.resource(resourceName)
							.type("application/xml")
							.accept("application/xml")
							.post(Node.class,nodeNffg);
					System.out.println("Created node of nffg - Response of server: \n" + response.getId() + response.getProperty().get(0).getValue());
					//create a mapping between name and id of nodes stored in the neo4j service - special map for nffg
					mapNameNodesNeoNffg.put(response.getProperty().get(0).getValue(),response.id); 
					
			}catch(Exception e){
				System.out.println("Something was wrong while contacting neo4j to create the nffg node");
				e.printStackTrace();
				//TODO verify it
				
				//TODO: manage deletion of the node from the map
				throw new NotFoundException("Something was wrong while contacting neo4j to create the nffg node",
						Response.status(404).entity("Something was wrong while contacting neo4j to create the nffg node").build());
			}

				
		//RELATIONSHIPS - LINKS
					for(XLink l : nffg.getLinks().getLink()){
						String srcId = mapNameNodesNeo.get(l.getSrc());
						String dstId = mapNameNodesNeo.get(l.getDst());
					
						//prepare relationship for POST
						Relationship relationship = new Relationship();
						relationship.setDstNode(dstId);
						relationship.setSrcNode(srcId);
						
						//This name is set by Assignment2.pdf
						relationship.setType("Link");
						
						String requestString = nps.baseServiceUrlneo + "/resource/node/" + srcId +"/relationship";
						try{
							Relationship returnedRelationship =
									nps.client.resource(requestString)
									.accept("application/xml")
									.type("application/xml")
									.post(Relationship.class,relationship);
							
							System.out.println("Returned Relationship: " + returnedRelationship.getId() + " " + returnedRelationship.getSrcNode() + " " + returnedRelationship.getDstNode() + " " + returnedRelationship.getType());
						}catch(Exception e){
							System.out.println("Error in creating the relationship");
							e.printStackTrace();
							System.out.println("Something was wrong while contacting neo4j to create relationship");
							e.printStackTrace();
							//TODO verify it
							
							//TODO: manage deletion of the node from the map
							throw new NotFoundException("Something was wrong while contacting neo4j  to create relationship",
									Response.status(404).entity("Something was wrong while contacting neo4j  to create relationship").build());
						}
						
					}
				
					
		//RELATIONSHIPS of NFFG node to define connection to Nffg Node
					for(Node n : listNode){
						String srcId = mapNameNodesNeoNffg.get(nffg.getName());
						System.out.println("NDFFG ID: " + srcId + nffg.getName() );
						String dstId = n.getId();
						System.out.println("Node ID: " + dstId + n.getProperty().get(0).getValue() );
						
					
						//prepare relationship for POST
						Relationship relationship = new Relationship();
						relationship.setDstNode(dstId);
						relationship.setSrcNode(srcId);
						
						//This name is set by Assignment2.pdf
						relationship.setType("belongs");
						
						String requestString = nps.baseServiceUrlneo + "/resource/node/" + srcId +"/relationship";
						try{
							Relationship returnedRelationship =
									nps.client.resource(requestString)
									.accept("application/xml")
									.type("application/xml")
									.post(Relationship.class,relationship);
							
							System.out.println("Created link of nffg - Returned Relationship: " + returnedRelationship.getId() + " " + returnedRelationship.getSrcNode() + " " + returnedRelationship.getDstNode() + " " + returnedRelationship.getType());
						}catch(Exception e){
							System.out.println("Error in creating the relationship");
							e.printStackTrace();
							System.out.println("Something was wrong while contacting neo4j to create relationship for the nffg");
							e.printStackTrace();
							//TODO verify it
							
							//TODO: manage deletion of the node from the map
							throw new NotFoundException("Something was wrong while contacting neo4j  to create relationship for the nffg",
									Response.status(404).entity("Something was wrong while contacting neo4j  to create relationship for the nffg").build());
						}
					}
					
				mapXNffg.put(nffg.getName(), nffg);
				System.out.println("Nffg corretly inserted in the map");
				return nffg;
			}
			else{
				System.out.println("Error - Nffg name already existing");
				throw new ForbiddenException("Error - Nffg name already existing",Response.status(403).entity("Error - Nffg name already existing").build());
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
	public static synchronized XPolicy addXPolicyVerifyXNffg(XPolicy policy,Boolean overwrite){
		
			if(!mapXNffg.containsKey(policy.getNffg())){
				System.out.println("Error - Nffg not existing");
				throw new NotFoundException("Error - Nffg name not existing",Response.status(403).entity("Error - Nffg name not existing").build());
			}
			else{
				if(!overwrite){
					//not allowed to overwrite existing policies
					if(mapXPolicy.putIfAbsent(policy.getName(), policy) == null){
						//able to insert the element in the map
						return policy;
					}
					else{
						System.out.println("Error - Policy already existing");
						throw new ForbiddenException("Error - Policy already existing",Response.status(403).entity("Error - Policy already existing").build());
						
					}
				}
				else{
					//allowed to overwrite existing policies
					mapXPolicy.put(policy.getName(), policy);
					return policy;
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
		
		xnffgs.getNffg().forEach(n->{
			returnedXNffgs.getNffg().add(addXNffg(n));
		});
		
		return returnedXNffgs;
	}
	
	//TODO: manage policy not existing or nffg not existing
	public static synchronized XPolicies addXPolicies(XPolicies xpolicies,Boolean overwrite) {
		try{
			XPolicies returnedXPolicies = new XPolicies();
			
			//verify if the Nffg related to the policies are really existing
			xpolicies.getPolicy().forEach(p->{
				if(!mapXNffg.containsKey(p.getNffg())){
					System.out.println("The nffg corresponding to the policy is not existing");
					throw new NotFoundException("The nffg corresponding to the policy is not existing",Response.status(404).entity("The nffg corresponding to the policy is not existing").build());
				}
			});		
			
			//if you can't overwrite the policy, verify no policies are alreaddy existing
			if(!overwrite){
				System.out.println("Not Overwriting Policies");
				xpolicies.getPolicy().forEach(p->{
					if(mapXPolicy.containsKey(p.getName())){
						System.out.println("At least one policy in the set is already existing");
						throw new ForbiddenException("At least one policy in the set is already existing",Response.status(403).entity("At least one policy in the set is already existing").build());
					}
				});
			}
			else{
				System.out.println("Overwriting Policies");

			}
			List<XPolicy> list = returnedXPolicies.getPolicy();
			xpolicies.getPolicy().forEach(p->{
				if(p.getVerification()==null){
					p.setVerification(null);
				}
				
				if(p.getVerification().getVerificationTime()==null){
					p.setVerification(null);
				}
				list.add(addXPolicyVerifyXNffg(p,overwrite));
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
	
//TODO: test the verification
	public static synchronized XPolicy verifyPolicy(String name){
		NffgPolicyService nps = new NffgPolicyService();
		XPolicy xpolicy = mapXPolicy.get(name);

		if(xpolicy==null){
			
			System.out.println("--Error impossible to find the policy in the database");
			throw new NotFoundException("Unable to find the policy to validate",
					Response.status(404).entity("Unable to find the policy to validate").build());
		}
		
		try{
			
			String resourceName = nps.baseServiceUrlneo + "/resource/node/"+
					mapNameNodesNeo.get(xpolicy.getSrc()) + 
					"/paths?dst="+mapNameNodesNeo.get(xpolicy.getDst());
			
			System.out.println("+++++++++++++++++RESUORCE URL+++++++++++++++++++++");
			System.out.println(resourceName);
			
			Paths paths = nps.client.resource(resourceName)
					.type("application/xml")
					.accept("application/xml")
					.get(Paths.class);
		
			System.out.println("Found n paths: " + paths.getPath().size());
			
			XVerification xv = new XVerification();
			if(paths.getPath().size()==0){
				//the policy is not satisfied
				xv.setMessage("The reachability policy is not satisfied");
				xv.setResult(false);
				
			}
			else{
				//at least one path found - policy satisfied
				xv.setMessage("The reachability policy is satisfied!!");
				xv.setResult(true);
			}
			Calendar c = Calendar.getInstance();
			c.setTimeZone(TimeZone.getDefault());
			xv.setVerificationTime(NffgPolicyService.convertCalendar(c));
			
			xpolicy.setVerification(xv);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Error in retrieving the paths" + e.getMessage());
			throw new NotFoundException("Unable to find a resource for validation",
					Response.status(404).entity("Unable to find a resource for validation").build());
		}
		
		return xpolicy;
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



//public static synchronized void deleteAllXNffgs(){
//mapXNffg.clear();
////when removing an nffg also the relative policies are removed
//deleteAllPolicies();
//}

//public static synchronized XNffg deleteNffgByName(String name,String delete_policy) throws NotFoundException,ForbiddenException{
//
//NffgPolicyService nps = new NffgPolicyService();
////delete from neo4j
//
//if(!mapXNffg.containsKey(name)){
//	throw new NotFoundException(Response.status(404).entity("The requested Nffg does not exists! Impossible to remove it").build());
//}
//
//XNffg xnffg = mapXNffg.get(name);
//
//
//if(delete_policy.equals("y")){
//		
////		deleteFromNeo4J(xnffg,nps);
//	
//		mapXNffg.remove(name);
//		
//		
//		//delete related Policy of the given nffg
//		mapXPolicy.values().stream().filter(p->{
//			if(p.getNffg().equals(xnffg.getName())){
//				return true;
//			}
//			else{
//				return false;
//			}
//		}).forEach(p->{
//			mapXPolicy.remove(p.getName());
//		});
//		
//		
//		return xnffg;
//	
//}else if(delete_policy.equals("n")){
//	int size = 
//		mapXPolicy.values().stream().filter(p->{
//			if(p.getNffg().equals(xnffg.getName())){
//				return true;
//			}
//			else{
//				return false;
//			}
//		}).collect(Collectors.toList()).size();
//	
//	//no policies, I can remove the nffg
//	if(size==0){
//		
////		deleteFromNeo4J(xnffg,nps);
//		
//		mapXNffg.remove(name);
//		return xnffg;
//	}
//	else{
//		throw new ForbiddenException("Impossible to delete the nffg - at least one policy referring to it exists",Response.status(403).entity("Impossible to delete the nffg - at least one policy referring to it exists").build());
//	}
//}
//else{
//	throw new ForbiddenException("You must specify either y/n for delpolicy parameter",
//			Response.status(403).entity("You must specify either y/n for delpolicy parameter").build());
//}
//}

//private static synchronized void deleteFromNeo4J(XNffg xnffg,NffgPolicyService nps) throws NotFoundException{
//
//for(XNode x : xnffg.getNodes().getNode()){
//	try{
//		String resourceName = nps.baseServiceUrlneo + "/resource/node/" + mapNameNodesNeo.get(x.getName());
//		Response response = nps.client.resource(resourceName)
//				.type("text/plain")
//				.accept("application/xml")
//				.delete(Response.class);
//		
//		if(response.getStatus()>=400){
//			throw new Exception("Error in deleting the node");
//		}
//		System.out.println("Response of server: \t");
//		//create a mapping between name and id of nodes stored in the neo4j service
//		System.out.println("Node deleted: " + response.getEntity().toString());
//		mapNameNodesNeo.remove(x.getName()); 
//	}catch(Exception e){
//		System.out.println("Something was wrong while deleting a noe from neo4j");
//		e.printStackTrace();
//		//TODO verify it
//		
//		//TODO: manage deletion of the node from the map
//		throw new NotFoundException("Something was wrong while contacting neo4j to insert a node",
//				Response.status(404).entity("Something was wrong while contacting neo4j to insert a node").build());
//	}
//}
////TODO - remove this unuseful redundancy		
////delete the node of the nffg
//try{
//	String resourceName = nps.baseServiceUrlneo + "/resource/node/" + mapNameNodesNeoNffg.get(xnffg.getName());
//	Response response = nps.client.resource(resourceName)
//			.type("text/plain")
//			.accept("application/xml")
//			.delete(Response.class);
//	
//	if(response.getStatus()>=400){
//		throw new Exception("Error in deleting the node");
//	}
//	System.out.println("Response of server: \t");
//	//create a mapping between name and id of nodes stored in the neo4j service
//	System.out.println("Node deleted: " + response.getEntity().toString());
//	mapNameNodesNeoNffg.remove(xnffg.getName()); 
//}catch(Exception e){
//	System.out.println("Something was wrong while deleting a noe from neo4j");
//	e.printStackTrace();
//	//TODO verify it
//	
//	//TODO: manage deletion of the node from the map
//	throw new NotFoundException("Something was wrong while contacting neo4j to insert a node",
//			Response.status(404).entity("Something was wrong while contacting neo4j to insert a node").build());
//}
//
//}