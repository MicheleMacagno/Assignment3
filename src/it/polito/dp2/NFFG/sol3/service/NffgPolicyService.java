package it.polito.dp2.NFFG.sol3.service;

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
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


import it.polito.dp2.NFFG.sol3.bindings.*;

//TODO: verify the set schema, is it correct? - it always throws exceptions

public class NffgPolicyService {
	private static ConcurrentHashMap<String,XNffg> mapXNffg = new ConcurrentHashMap<String,XNffg>();
	private static ConcurrentHashMap<String,XPolicy> mapXPolicy = new ConcurrentHashMap<String,XPolicy>();
//	private static ConcurrentHashMap<String,String> mapNameNodesNeo  = new ConcurrentHashMap<String,String>();
//	private static ConcurrentHashMap<String,String> mapNameNodesNeoNffg  = new ConcurrentHashMap<String,String>();
	private static ConcurrentHashMap<String,ConcurrentHashMap<String,String>> mapNffgNodesNffg = new ConcurrentHashMap<String,ConcurrentHashMap<String,String>>();
	
	private String baseServiceUrlneo;
	private Client client=null;
	
//TODO: change to 8080	
	public NffgPolicyService(){
		baseServiceUrlneo=System.getProperty("it.polito.dp2.NFFG.lab3.NEO4JURL");
		if(baseServiceUrlneo==null){
			System.out.println("URL of neo4j - system property is not set");;
			baseServiceUrlneo="http://localhost:8081/Neo4JXML/rest/";
		}
		
		//make the last symbol of the path a slash
		if(baseServiceUrlneo.lastIndexOf("/") != baseServiceUrlneo.length()-1){
			baseServiceUrlneo = baseServiceUrlneo + "/";
		}
		
		//create a client for each different connection to the system
		System.out.println(baseServiceUrlneo);
		try{
			client = ClientBuilder.newClient();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public XNffg getXNffgByName(String name){
		synchronized(mapXNffg){
			if(mapXNffg.containsKey(name)){
				return mapXNffg.get(name);
			}
			else{
				System.out.println("Nffg not found in the database");
				throw new NotFoundException("Nffg not found in the database",Response.status(404).entity("Nffg not found in the database").build());
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
	
	public XNffgs addXNffgs(XNffgs xnffgs) throws ForbiddenException{
		
		XNffgs returnedXNffgs = new XNffgs();
		
		synchronized(mapXNffg){
			
			//This control is necessary to avoid the insertion of one or more nffgs in case 
			//at least one nffg is already existing
			xnffgs.getNffg().forEach(n->{
				if(mapXNffg.containsKey(n.getName())){
					System.out.println("At least one of the Nffg in the set is already existing");
					throw new ForbiddenException("At least one of the Nffg in the set is already existing",Response.status(403).entity("At least one of the Nffg in the set is already existing").build());
				}
			});
			
			xnffgs.getNffg().forEach(n->{
				//call the method addXNffg adding a policy at a time
				returnedXNffgs.getNffg().add(addXNffg(n));
			});
		}
		
		return returnedXNffgs;
	}

	
	/**
	 * Method receiving an XNffg and storing it in the Database.
	 * Manages the insert into neo4j
	 * @param nffg
	 * @return
	 * true:  successful creation
	 * false: creation failed
	 */
	public XNffg addXNffg(XNffg nffg){
		//at the end of the procedure it is inserted in the big map, containing as Key the name of Nfg, and as content the set of Nodes (with
		//relative Neo4J Id of Nodes
		ConcurrentHashMap<String,String> tmpMapNameNodesNeo  = new ConcurrentHashMap<String,String>();
//TODO: verify still useful
		ConcurrentHashMap<String,String> tmpMapNameNodesNeoNffg  = new ConcurrentHashMap<String,String>();
		
		synchronized(mapXNffg){
				if(!mapXNffg.containsKey(nffg.getName())){	
					//able to insert the element in the map
					
					//prepare modification time
					Calendar c = Calendar.getInstance();
					c.setTimeZone(TimeZone.getDefault());
					nffg.setLastUpdate(convertCalendar(c));
					
					//neo4j
					List<Node> listNode = new LinkedList<Node>();
					
					
					//nodes of nffg to insert into neo4j
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
								String resourceName = baseServiceUrlneo + "resource/node/";
								response = client.target(resourceName)
										.request(MediaType.APPLICATION_XML)
										.accept(MediaType.APPLICATION_XML)
										.post(Entity.xml(node),Node.class);
								System.out.println("Response of server: \t" + response.getId() + response.getProperty().get(0).getValue());
								//create a mapping between name and id of nodes stored in the neo4j service
								System.out.println("Node " + n.getName() + " added");
								tmpMapNameNodesNeo.put(response.getProperty().get(0).getValue(),response.id); 
								listNode.add(response);//to create link with nffg node
								
								
						}catch(Exception e){
							System.out.println("Something was wrong while contacting neo4j to insert a node");
							e.printStackTrace();
							//TODO verify it
	//TODO: ROLLBACK OF ALREADY INSERTED NODES IN NEO4J						
							//TODO: manage deletion of the node from the map
							throw new InternalServerErrorException("Something was wrong while contacting neo4j to insert a node",
									Response.status(500).entity("Something was wrong while contacting neo4j to insert a node").build());
						}
					}
					
	//--NFFG NODE
					
					//ADDED assigment 3
					//manegement of NFFG master node
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
						System.out.println("Created node of nffg - Response of server: \n" + response.getId() + response.getProperty().get(0).getValue());
						
	//LABELS
						//Create labels for Neo4j
						Labels lbl = new Labels();
						lbl.value= new LinkedList<String>();
						lbl.value.add(new String("NFFG"));
						resourceName = baseServiceUrlneo + "resource/node/"+response.id+"/label";
						client.target(resourceName)
								.request(MediaType.APPLICATION_XML)
								.post(Entity.xml(lbl));
						
						//create a mapping between name and id of nodes stored in the neo4j service - special map for nffg
						tmpMapNameNodesNeoNffg.put(response.getProperty().get(0).getValue(),response.id); 
				
					}catch(Exception e){
						System.out.println("Something was wrong while contacting neo4j to create the nffg node");
						e.printStackTrace();
						//TODO verify it
						
						//TODO: manage deletion of the node from the map
	//TODO: delete all the nodes from the map, including labels and master node
						throw new InternalServerErrorException("Something was wrong while contacting neo4j to create the nffg node",
								Response.status(500).entity("Something was wrong while contacting neo4j to create the nffg node").build());
					}
	
					
	//RELATIONSHIPS - LINKS
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
							Relationship returnedRelationship =
									client.target(requestString)
									.request(MediaType.APPLICATION_XML)
									.accept(MediaType.APPLICATION_XML)
									.post(Entity.xml(relationship),Relationship.class);
							
							System.out.println("Returned Relationship: " + returnedRelationship.getId() + " " + returnedRelationship.getSrcNode() + " " + returnedRelationship.getDstNode() + " " + returnedRelationship.getType());
						}catch(Exception e){
							System.out.println("Error in creating the relationship");
							System.out.println("Something was wrong while contacting neo4j to create relationship");
							e.printStackTrace();
							
							//TODO: manage deletion of the node from the map
	
							throw new InternalServerErrorException("Something was wrong while contacting neo4j  to create relationship",
									Response.status(500).entity("Something was wrong while contacting neo4j  to create relationship").build());
						}
						
					}
				
					
					//RELATIONSHIPS of NFFG node to define connection to Nffg Node
					for(Node n : listNode){
						String srcId = tmpMapNameNodesNeoNffg.get(nffg.getName());
						System.out.println("NDFFG ID: " + srcId + nffg.getName() );
						String dstId = n.getId();
						System.out.println("Node ID: " + dstId + n.getProperty().get(0).getValue() );
						
					
						//prepare relationship for POST
						Relationship relationship = new Relationship();
						relationship.setDstNode(dstId);
						relationship.setSrcNode(srcId);
						
						//This name is set by Assignment2.pdf
						relationship.setType("belongs");
						
						String requestString = baseServiceUrlneo + "resource/node/" + srcId +"/relationship";
						try{
							Relationship returnedRelationship =
									client.target(requestString)
									.request(MediaType.APPLICATION_XML)
									.accept(MediaType.APPLICATION_XML)
									.post(Entity.xml(relationship),Relationship.class);
							
							System.out.println("Created link of nffg - Returned Relationship: " + returnedRelationship.getId() + " " + returnedRelationship.getSrcNode() + " " + returnedRelationship.getDstNode() + " " + returnedRelationship.getType());
						}catch(Exception e){
							System.out.println("Error in creating the relationship");
							System.out.println("Something was wrong while contacting neo4j to create relationship for the nffg");
							e.printStackTrace();
							throw new InternalServerErrorException("Something was wrong while contacting neo4j  to create relationship for the nffg",
									Response.status(500).entity("Something was wrong while contacting neo4j  to create relationship for the nffg").build());
						}
					}
					
				mapXNffg.put(nffg.getName(), nffg);
				
				//Modification exploited in order to be more robust with respect to errors occurred while contacting Neo4j
				//No incoherence of data if some errors occurs.
				//Only the effectivly registered nodes have been inserted in the maps

//TODO: fixing this activity
				mapNffgNodesNffg.put(nffg.getName(), tmpMapNameNodesNeo);
//				tmpMapNameNodesNeo.entrySet().stream().forEach(n->{
//					mapNameNodesNeo.put(n.getKey(),n.getValue());
//				});
				
//				tmpMapNameNodesNeoNffg.entrySet().stream().forEach(n->{
//					mapNameNodesNeoNffg.put(n.getKey(), n.getValue());
//				});
				
				System.out.println("Nffg corretly inserted in the map");
				return nffg;
			}
			else{
				System.out.println("Error - Nffg name already existing - Please submit a Nffg with a different name ");
				throw new ForbiddenException("Error - Nffg name already existing - Please submit a Nffg with a different name ",Response.status(403).entity("Error - Nffg name already existing").build());
			}
		}
	}
	
	/**
	 * 
	 * @param name
	 * @return the XPolicy object in case of success
	 * Throws a NotFoundException if the policy does not exist.
	 */
	public XPolicy getXPolicyByName(String name){
		
		synchronized(mapXPolicy){
			if(mapXPolicy.containsKey(name)){
				return mapXPolicy.get(name);
			}
			else{
				throw new NotFoundException("The requested Policy is not existing", Response.status(404).entity("The requested Policy is not existing").build());
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
	public XPolicy addXPolicyVerifyXNffg(XPolicy policy,Boolean overwrite){
		
		//these two synchronized allow us to exploit a bit more parallelism of operations
		//operations are exploited both on policy and in nffg
		synchronized(mapXNffg){
			synchronized(mapXPolicy){
				
				
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
							throw new ForbiddenException("Error - Policy already existing. Cannot overwrite it",Response.status(403).entity("Error - Policy already existing. Cannot overwrite it").build());
							
						}
					}
					else{
						//allowed to overwrite existing policies
						mapXPolicy.put(policy.getName(), policy);
						return policy;
					}
				}
				
				
				
				
				
			}
		}
			
	}
	
	
	//TODO: manage policy not existing or nffg not existing
	public XPolicies addXPolicies(XPolicies xpolicies,Boolean overwrite) {
		synchronized(mapXNffg){
			synchronized(mapXPolicy){
				
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
						//sometimes the verification can be totally empty
						if(p.getVerification()==null){
							p.setVerification(null);
						}
						//if it is not specified the verification time it means the policy was not verified.
						else if(p.getVerification().getVerificationTime()==null){
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
				throw new NotFoundException(Response.status(404).entity("The requested policy does not exists! Impossible to remove it").build());
			}
		}
	}
	
	public XPolicy updatePolicyByName(String name,XPolicy xpolicy) throws NotFoundException,ForbiddenException{
		
		synchronized(mapXNffg){
			synchronized(mapXPolicy){
				
				
				xpolicy.setName(name);
				if(!mapXNffg.containsKey(xpolicy.getNffg())){
					throw new ForbiddenException("Error - Nffg name not existing",
							Response.status(403).entity("Error - Nffg name not existing").build());
				}
				if(mapXPolicy.containsKey(name)){
					mapXPolicy.put(name,xpolicy);
					return xpolicy;
				}else{
					throw new NotFoundException("The policy is not existing, impossible to update it",
							Response.status(404).entity("The policy is not existing, impossible to update it").build());
				}
			}
		}
		
		
	}
	
//TODO: test the verification
//TODO modify everything
// policy and node id must come from the policy
	public XPolicy verifyPolicy(String name){
		XPolicy xpolicy = mapXPolicy.get(name);
		
		

		synchronized(mapXPolicy){
			//debug-------------------------------------
				mapNffgNodesNffg.keySet().stream().forEach(m->{
					System.out.println("NFFG: " + m + "\t--------------------------" );
					mapNffgNodesNffg.get(m).keySet().stream().forEach(e->{
						System.out.println("\tNode: " + e + "\t" +mapNffgNodesNffg.get(m).get(e));
					});
				});
			
			//--end of debug
			if(xpolicy==null){
				
				System.out.println("--Error impossible to find the policy in the database");
				throw new NotFoundException("Unable to find the policy to validate",
						Response.status(404).entity("Unable to find the policy to validate").build());
			}
			
			try{
				
				String resourceName = baseServiceUrlneo + "resource/node/"
				+mapNffgNodesNffg.get(xpolicy.getNffg()).get(xpolicy.getSrc()) 	//id of src node of nffg relative to the policy
				+"/paths?dst="
				+mapNffgNodesNffg.get(xpolicy.getNffg()).get(xpolicy.getDst()); //id of dst node of nffg relative to the policy
				
				System.out.println(resourceName);
				
				Paths paths = client.target(resourceName)
						.request(MediaType.APPLICATION_XML)
						.accept(MediaType.APPLICATION_XML)
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
				xv.setVerificationTime(convertCalendar(c));
				
				xpolicy.setVerification(xv);
			}catch(Exception e){
				e.printStackTrace();
				System.out.println("Error in retrieving the paths" + e.getMessage());
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
