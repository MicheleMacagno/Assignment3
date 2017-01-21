package it.polito.dp2.NFFG.sol3.service;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

//Looger libraries
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import it.polito.dp2.NFFG.sol3.bindings.*;

//TODO: verify the set schema, is it correct? - it always throws exceptions

public class NffgPolicyService {
	private static ConcurrentHashMap<String,XNffg> mapXNffg = new ConcurrentHashMap<String,XNffg>();
	private static ConcurrentHashMap<String,XPolicy> mapXPolicy = new ConcurrentHashMap<String,XPolicy>();
	private static ConcurrentHashMap<String,String> mapNameNffgMasterNodesNeo  = new ConcurrentHashMap<String,String>();
	
	//Map Indexed Via NffgName -> Retrieves a Map of NodesName/ Neo4J id
	//		retrieved map.get(NodeName) -> Neo4J id
	// Useful because different Nffgs can have the same NodeName
	private static ConcurrentHashMap<String,ConcurrentHashMap<String,String>> mapNffgNodesNffg = new ConcurrentHashMap<String,ConcurrentHashMap<String,String>>();
	
	private String baseServiceUrlneo;
	private Client client=null;
	private Logger logger;
	
//TODO: change to 8080	
	public NffgPolicyService(){
		logger = Logger.getLogger(NffgPolicyService.class.getName());;
		
		baseServiceUrlneo=System.getProperty("it.polito.dp2.NFFG.lab3.NEO4JURL");
		if(baseServiceUrlneo==null){
			logger.log(Level.INFO, "URL of neo4j - system property is not set");
			baseServiceUrlneo="http://localhost:8081/Neo4JXML/rest/";
		}
		
		//make the last symbol of the path a slash
		if(baseServiceUrlneo.lastIndexOf("/") != baseServiceUrlneo.length()-1){
			baseServiceUrlneo = baseServiceUrlneo + "/";
		}
		
		//create a client for each different connection to the system
		logger.log(Level.INFO,baseServiceUrlneo);
		try{
			client = ClientBuilder.newClient();
		}catch(Exception e){
			logger.log(Level.SEVERE,e.getMessage(),e);
			 
		}
	}
	
	public XNffg getXNffgByName(String name) throws NotFoundException{
		synchronized(mapXNffg){
			if(mapXNffg.containsKey(name)){
				return mapXNffg.get(name);
			}
			else{
				logger.log(Level.WARNING,"Nffg not found in the database");
				throw new NotFoundException("Nffg not found in the database",
						Response.status(404).entity("Nffg not found in the database").build());
			}
		}
	}
	
	
	public XNffgs getXNffgs(){
		XNffgs returnXNffgs= new XNffgs();
		
		synchronized(mapXNffg){
			mapXNffg.values().stream().forEach(n->{
				returnXNffgs.getNffg().add(n);
			});
		}
		return returnXNffgs;
	}
	
	public XNffgs addXNffgs(XNffgs xnffgs,UriInfo uriInfo) throws ForbiddenException,InternalServerErrorException{
		
		XNffgs returnedXNffgs = new XNffgs();
		
		synchronized(mapXNffg){
			
			//This control is necessary to avoid the insertion of one or more nffgs in case 
			//at least one nffg is already existing
			
			//Garanties Atomicity of the operation
			xnffgs.getNffg().forEach(n->{
				if(mapXNffg.containsKey(n.getName())){
					logger.log(Level.WARNING,"At least one of the Nffg in the set is already existing");
					throw new ForbiddenException("Error 403 - At least one of the Nffg in the set is already existing",
							Response.status(403).entity("Error 403 - At least one of the Nffg in the set is already existing").build());
				}
			});
			
			xnffgs.getNffg().forEach(n->{
				//call the method addXNffg adding a policy at a time
				returnedXNffgs.getNffg().add(addXNffg(n,uriInfo));
			});
		}
		
		return returnedXNffgs;
	}

	
	/**
	 * Method receiving an XNffg and storing it in the Database.
	 * Manages the insert into neo4j
	 * @param Xnffg nffg : the element to be inserted in the database
	 * @return
	 * XNffg element: with up to date information (i.e. Last Update Time)
	 * Throws Exceptions on failures
	 */
	public XNffg addXNffg(XNffg nffg,UriInfo uriInfo) throws ForbiddenException,InternalServerErrorException{
		//at the end of the procedure it is inserted in the big map, containing as Key the name of Nfg, and as content the set of Nodes (with
		//relative Neo4J Id of Nodes
		ConcurrentHashMap<String,String> tmpMapNameNodesNeo  = new ConcurrentHashMap<String,String>();
		String nffgNodeId; //contains the id received from neo4j after creating the NffgNode - NffgNode / Neo4J id
		
		
		synchronized(mapXNffg){
				if(!mapXNffg.containsKey(nffg.getName())){	
					//able to insert the element in the map
					
					//prepare modification time
					Calendar c = Calendar.getInstance();
					c.setTimeZone(TimeZone.getDefault());
					nffg.setLastUpdate(convertCalendar(c));
					
					//neo4j
					List<Node> listNode = new LinkedList<Node>();
					
		//NODES			
					//nodes of nffg to insert into neo4j
					for(XNode n : nffg.getNodes().getNode()){
						logger.log(Level.INFO,"Node " + n.getName() + " TO ADD");
						
						//prepare Node to add to Neo4J service
						Node node = new Node();
						Property nodeProperty = new Property();
						
						nodeProperty.setName("name");
						nodeProperty.setValue(n.getName());
						node.getProperty().add(nodeProperty);
						
						
						Node response=null;
						
						try{
								//POST add node in neo4j service
								String resourceName = baseServiceUrlneo + "resource/node/";
								response = client.target(resourceName)
										.request(MediaType.APPLICATION_XML)
										.accept(MediaType.APPLICATION_XML)
										.post(Entity.xml(node),Node.class);
								logger.log(Level.INFO,"Response of server: \t" + response.getId() + response.getProperty().get(0).getValue()
										+"\n Node " + n.getName() + " added");
								//Insert in the map Node Name, Id of Neo4J
								tmpMapNameNodesNeo.put(response.getProperty().get(0).getValue(),response.id); 
								listNode.add(response);//to create link with nffg node
								
								
						}catch(Exception e){
							logger.log(Level.SEVERE,"Something was wrong while contacting neo4j to insert a node. End");
							logger.log(Level.SEVERE, e.getMessage(),e);
							throw new InternalServerErrorException("Something was wrong while contacting neo4j to insert a node",
									Response.status(500).entity("Something was wrong while contacting neo4j to insert a node").build());
						}
					}
					
	//--NFFG NODE
					
					//ADDED assigment 3
					
					//Neo4J - manegement of NFFG master node
					Node nodeNffg = new Node();
					Property nodeNffgProperty = new Property();
					nodeNffgProperty.setName("name");
					nodeNffgProperty.setValue(nffg.getName());
					nodeNffg.getProperty().add(nodeNffgProperty);
					
					
					Node response = null;
					try{
						String resourceName = baseServiceUrlneo + "resource/node/";
						response = client.target(resourceName)
								.request(MediaType.APPLICATION_XML)
								.accept(MediaType.APPLICATION_XML)
								.post(Entity.xml(nodeNffg),Node.class);
						logger.log(Level.INFO,"Created node of nffg - Response of server: \t" + response.getId() + "\t" + response.getProperty().get(0).getValue());
						
						//Id assigned by Neo4J to The Nffg Node
						nffgNodeId=response.getId();
	//LABELS
						//Create labels for Nffg Node - for Neo4j 
						Labels lbl = new Labels();
						lbl.value= new LinkedList<String>();
						lbl.value.add(new String("NFFG"));
						resourceName = baseServiceUrlneo + "resource/node/"+response.id+"/label";
						
						//POST the label of Nffg Node
						client.target(resourceName)
								.request(MediaType.APPLICATION_XML)
								.post(Entity.xml(lbl));
				
					}catch(Exception e){
						logger.log(Level.SEVERE,"Something was wrong while contacting neo4j to create the nffg node");
						logger.log(Level.SEVERE,e.getMessage(),e);
						 
						throw new InternalServerErrorException("Something was wrong while contacting neo4j to create the nffg node",
								Response.status(500).entity("Something was wrong while contacting neo4j to create the nffg node").build());
					}
	
					
	//Neo4j RELATIONSHIPS - LINKS
					
					//for each Link in the received data I create a Relationship in Neo4J
					for(XLink l : nffg.getLinks().getLink()){
						String srcId = tmpMapNameNodesNeo.get(l.getSrc());
						String dstId = tmpMapNameNodesNeo.get(l.getDst());
					
						//prepare relationship for POST
						Relationship relationship = new Relationship();
						relationship.setDstNode(dstId);
						relationship.setSrcNode(srcId);
						
						//This name is set by Assignment2.pdf
						relationship.setType("Link");
						
						String requestString = baseServiceUrlneo + "resource/node/" + srcId +"/relationship";
						try{
							//POST - Create a relationship in Neo4J
							Relationship returnedRelationship =
									client.target(requestString)
									.request(MediaType.APPLICATION_XML)
									.accept(MediaType.APPLICATION_XML)
									.post(Entity.xml(relationship),Relationship.class);
							
							logger.log(Level.INFO,"Returned Relationship: " + returnedRelationship.getId() + " " + returnedRelationship.getSrcNode() + " " + returnedRelationship.getDstNode() + " " + returnedRelationship.getType());
						}catch(Exception e){
							logger.log(Level.SEVERE,"Error in creating the relationship\nSomething was wrong while contacting neo4j to create relationship");
							logger.log(Level.SEVERE,e.getMessage(),e);
							throw new InternalServerErrorException("Something was wrong while contacting neo4j  to create relationship",
									Response.status(500).entity("Something was wrong while contacting neo4j  to create relationship").build());
						}
						
					}
				
					
	//Neo4J - RELATIONSHIPS of NFFG node to define connection to Nffg Node
					for(Node n : listNode){
						String srcId = nffgNodeId;
						logger.log(Level.INFO,"NDFFG ID: " + srcId + nffg.getName() );
						String dstId = n.getId();
						logger.log(Level.INFO,"Node ID: " + dstId + n.getProperty().get(0).getValue() );
						
					
						//prepare RELATIONSHIP for POST
						Relationship relationship = new Relationship();
						relationship.setDstNode(dstId);
						relationship.setSrcNode(srcId);
						
						//This name is set by Assignment2.pdf
						relationship.setType("belongs");
						
						String requestString = baseServiceUrlneo + "resource/node/" + srcId +"/relationship";
						try{
							//POST - create a Relationship for Nffg Node
							Relationship returnedRelationship =
									client.target(requestString)
									.request(MediaType.APPLICATION_XML)
									.accept(MediaType.APPLICATION_XML)
									.post(Entity.xml(relationship),Relationship.class);
							
							logger.log(Level.INFO,"Created link of nffg - Returned Relationship: " + returnedRelationship.getId() + " " + returnedRelationship.getSrcNode() + " " + returnedRelationship.getDstNode() + " " + returnedRelationship.getType());
						}catch(Exception e){
							logger.log(Level.SEVERE,"Error in creating the relationship\nSomething was wrong while contacting neo4j to create relationship for the nffg");
							logger.log(Level.SEVERE,e.getMessage(),e);
							throw new InternalServerErrorException("Something was wrong while contacting neo4j  to create relationship for the nffg",
									Response.status(500).entity("Something was wrong while contacting neo4j  to create relationship for the nffg").build());
						}
					}
					
				
				//Modification exploited in order to be more robust with respect to errors occurred while contacting Neo4j
				//No incoherence of data if some errors occurs.
				//Only the effectively registered nodes have been inserted in the maps
				
				//CONSISTENCY of Data: only in case of success data is stored
				URI uri = uriInfo.getBaseUriBuilder().path("nffg").path(nffg.getName()).build();
				nffg.setHref(uri.toString());	
				mapXNffg.put(nffg.getName(), nffg);
				mapNffgNodesNffg.put(nffg.getName(), tmpMapNameNodesNeo);
				mapNameNffgMasterNodesNeo.put(nffg.getName(), nffgNodeId);
				
				logger.log(Level.INFO,"Nffg corretly inserted in the map");
				return nffg;
			}
			else{
				logger.log(Level.WARNING,"Error - Nffg name already existing - Please submit a Nffg with a different name ");
				throw new ForbiddenException("Error - Nffg name already existing - Please submit a Nffg with a different name ",
						Response.status(403).entity("Error - Nffg name already existing").build());
			}
		}
	}
	
	/**
	 * 
	 * @param name
	 * @return the XPolicy object in case of success
	 * Throws a NotFoundException if the policy does not exist.
	 */
	public XPolicy getXPolicyByName(String name) throws NotFoundException{
		
		synchronized(mapXPolicy){
			if(mapXPolicy.containsKey(name)){
				return mapXPolicy.get(name);
			}
			else{
				throw new NotFoundException("The requested Policy is not existing", 
						Response.status(404).entity("The requested Policy is not existing").build());
			}
		}
	}
	
	public XPolicies getXPolicies(){
		XPolicies rxpolicies = new XPolicies();
		
		
		synchronized(mapXPolicy){
			
			mapXPolicy.values().stream().forEach(p->{
				rxpolicies.getPolicy().add(p);
			});
		}
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
	public XPolicy addXPolicyVerifyXNffg(XPolicy policy,UriInfo uriInfo) throws NotFoundException{
		
		//these two synchronized allow us to exploit a bit more parallelism of operations
		//operations are exploited both on policy and in nffg
		synchronized(mapXNffg){
			synchronized(mapXPolicy){
				
				
				if(!mapXNffg.containsKey(policy.getNffg())){
					logger.log(Level.WARNING,"Error - Nffg not existing");
					throw new NotFoundException("Error - Nffg name not existing",
							Response.status(404).entity("Error - Nffg name not existing").build());
				}
				else{
					
					//TODO:verify the existence of nodes
					XNffg xnffg = mapXNffg.get(policy.getNffg());
					Boolean found = false;
					
					//verify the existence of source node
					for(XNode n : xnffg.getNodes().getNode()){
						if(n.getName().equals(policy.getSrc())){
							found=true;
							break;
						}
					}
					if(!found){
						logger.log(Level.WARNING,"Error - Src Node of nffg is not existing");
						throw new ForbiddenException("Error - Src Node of nffg is not existing",
								Response.status(403).entity("Error - Src Node of nffg is not existing").build());
					}
					
					//verify the existence of dstNode
					found=false;
					for(XNode n : xnffg.getNodes().getNode()){
						if(n.getName().equals(policy.getDst())){
							found=true;
							break;
						}
					}
					if(!found){
						logger.log(Level.WARNING,"Error - Dst Node of nffg is not existing");
						throw new ForbiddenException("Error - Dst Node of nffg is not existing",
								Response.status(403).entity("Error - Dst Node of nffg is not existing").build());
					}
					
					
					//create officially the policy
					
					URI uri = uriInfo.getBaseUriBuilder().path("policy").path(policy.getName()).build();
					policy.setHref(uri.toString());
					//allowed to overwrite existing policies
					mapXPolicy.put(policy.getName(), policy);
					
					return policy;
				}
			}
		}
	}
	
	
	//TODO: manage policy not existing or nffg not existing
	public XPolicies addXPolicies(XPolicies xpolicies,UriInfo uriInfo) throws NotFoundException,InternalServerErrorException,ForbiddenException {
		synchronized(mapXNffg){
			synchronized(mapXPolicy){
				
					XPolicies returnedXPolicies = new XPolicies();
					
					//verify if the Nffg related to the policies are really existing
					xpolicies.getPolicy().forEach(p->{
						if(!mapXNffg.containsKey(p.getNffg())){
							logger.log(Level.WARNING,"The nffg corresponding to the policy is not existing");
							throw new NotFoundException("The nffg corresponding to the policy is not existing",
									Response.status(404).entity("The nffg corresponding to the policy is not existing").build());
						}
					});		
					
				try{
					//prepare a list of all created policies. To give back to the service 
					List<XPolicy> list = returnedXPolicies.getPolicy(); //initialize
					xpolicies.getPolicy().forEach(p->{
						//sometimes the verification can be totally empty
						if(p.getVerification()==null){
							p.setVerification(null);
						}
						//if it is not specified the verification time it means the policy was not verified.
						else if(p.getVerification().getVerificationTime()==null){
							p.setVerification(null);
						}
						list.add(addXPolicyVerifyXNffg(p,uriInfo));
					});
					
					return returnedXPolicies;
				}catch(ForbiddenException e){
					logger.log(Level.WARNING, "A node is not existing", e);
					throw e;
				}catch(Exception e){
					logger.log(Level.SEVERE,e.getMessage(),e);
					 
					throw new InternalServerErrorException("Unexpected Error in the system",
							Response.status(500).entity("Unexpected Error in the system").build());
				}
				
				
			}
		}
		
		
	}
	
	public void deleteAllPolicies(){
		synchronized(mapXPolicy){
			mapXPolicy.clear();
		}
	}
	
	public XPolicy deletePolicyByName(String name) throws NotFoundException{
		
		synchronized(mapXPolicy){
			
			if(mapXPolicy.containsKey(name)){
				XPolicy xpolicy = mapXPolicy.get(name);
				mapXPolicy.remove(name);
				return xpolicy;
			}
			else{
				throw new NotFoundException("The requested policy does not exists! Impossible to remove it",
						Response.status(404).entity("The requested policy does not exists! Impossible to remove it").build());
			}
		}
	}
	
	public XPolicy updatePolicyByName(XPolicy xpolicy) throws NotFoundException,ForbiddenException{
		
		synchronized(mapXNffg){
			synchronized(mapXPolicy){
				
				//Policy not existing
				if(!mapXPolicy.containsKey(xpolicy.getName())){
					throw new NotFoundException("The policy is not existing, impossible to update it",
							Response.status(404).entity("The policy is not existing, impossible to update it").build());
				}
				
				//Nffg of updated policy not existing
				if(!mapXNffg.containsKey(xpolicy.getNffg())){
					throw new ForbiddenException("Error - Nffg name not existing",
							Response.status(403).entity("Error - Nffg name not existing").build());
				}
				
				//Updated the policy and return it
				mapXPolicy.put(xpolicy.getName(),xpolicy);
				return xpolicy;
				
			}
		}
		
		
	}
	
//TODO: test the verification
//TODO modify everything
// policy and node id must come from the policy
	public XPolicy verifyPolicy(String name){
		XPolicy xpolicy;
		

		synchronized(mapXPolicy){
			xpolicy = mapXPolicy.get(name);

			//debug-------------------------------------
				mapNffgNodesNffg.keySet().stream().forEach(m->{
					logger.log(Level.INFO,"NFFG: " + m + "\t--------------------------" );
					mapNffgNodesNffg.get(m).keySet().stream().forEach(e->{
						logger.log(Level.INFO,"\tNode: " + e + "\t" +mapNffgNodesNffg.get(m).get(e));
					});
				});
			
			//--------------------------------------------end of debug
			if(xpolicy==null){
				
				logger.log(Level.WARNING,"--Error impossible to find the policy in the database");
				throw new NotFoundException("Unable to find the policy to validate",
						Response.status(404).entity("Unable to find the policy to validate").build());
			}
			
			try{
				
				String resourceName = baseServiceUrlneo + "resource/node/"
				+mapNffgNodesNffg.get(xpolicy.getNffg()).get(xpolicy.getSrc()) 	//id of src node of nffg relative to the policy
				+"/paths?dst="
				+mapNffgNodesNffg.get(xpolicy.getNffg()).get(xpolicy.getDst()); //id of dst node of nffg relative to the policy
				
				logger.log(Level.INFO,resourceName);
				
				Paths paths = client.target(resourceName)
						.request(MediaType.APPLICATION_XML)
						.accept(MediaType.APPLICATION_XML)
						.get(Paths.class);
			
				logger.log(Level.INFO,"Found n paths: " + paths.getPath().size());
				
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
				xv.setVerificationTime(convertCalendar(c));
				
				xpolicy.setVerification(xv);
			}catch(Exception e){
				 
				logger.log(Level.SEVERE,"Error in retrieving the paths" + e.getMessage());
				logger.log(Level.SEVERE,e.getMessage(),e);
				throw new InternalServerErrorException("Internal Server Error - Error in retrieving the paths",
						Response.status(500).entity("Internal Server Error - Error in retrieving the paths!\nProblems while contacting neo4J").build());
			}
		}

		return xpolicy;
	}
	
	
	
	private XMLGregorianCalendar convertCalendar (Calendar cal){
		Date calendarDate = cal.getTime();
		
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(calendarDate);
		c.setTimeZone(cal.getTimeZone());
		XMLGregorianCalendar date2 = null;
		try {
			date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			logger.log(Level.SEVERE,"Error in converting data type");
			logger.log(Level.SEVERE,e.getMessage(),e);
			 
		}
		return date2;
	}
	
}
