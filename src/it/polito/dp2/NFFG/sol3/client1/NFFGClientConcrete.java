package it.polito.dp2.NFFG.sol3.client1;

import it.polito.dp2.NFFG.lab3.AlreadyLoadedException;
import it.polito.dp2.NFFG.lab3.ServiceException;
import it.polito.dp2.NFFG.lab3.UnknownNameException;
import it.polito.dp2.NFFG.sol3.bindings.XLinks;
import it.polito.dp2.NFFG.sol3.bindings.ObjectFactory;
import it.polito.dp2.NFFG.sol3.bindings.XFunctionality;
import it.polito.dp2.NFFG.sol3.bindings.XLink;
import it.polito.dp2.NFFG.sol3.bindings.XNffg;
import it.polito.dp2.NFFG.sol3.bindings.XNffgs;
import it.polito.dp2.NFFG.sol3.bindings.XNodes;
import it.polito.dp2.NFFG.sol3.bindings.XPolicy;
import it.polito.dp2.NFFG.sol3.bindings.XTraversal;
import it.polito.dp2.NFFG.sol3.bindings.XVerification;
import it.polito.dp2.NFFG.sol3.bindings.XNode;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


import it.polito.dp2.NFFG.*;


public class NFFGClientConcrete implements it.polito.dp2.NFFG.lab3.NFFGClient {
	NffgVerifier monitor;
	NffgVerifierFactory factory=null;
	String baseServiceUrl = null;
	Client client=null;
	ObjectFactory of = new ObjectFactory();
	
	/*
	 * The constructor set up the system, preparing the url of the service.
	 * Throws an exception in order to allow the method NFFGClientFactory.newNFFGClient() 
	 * of class NFFGClientFactory 
	 * to throw, eventually,
	 * the NFFGClientException if some errors occurs
	 */
	public NFFGClientConcrete() throws Exception{
		try {
			
			client = ClientBuilder.newClient();
			factory = it.polito.dp2.NFFG.NffgVerifierFactory.newInstance();
			monitor = factory.newNffgVerifier();
		
		
		}catch(FactoryConfigurationError e){
			e.printStackTrace();
			throw new Exception("Error 0 during creation of NffgVerifier");
		} catch (NffgVerifierException e) {
			e.printStackTrace();
			throw new Exception("Error 1 during creation of NffgVerifier");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error 2 during creation of NffgVerifier");
		}catch(Throwable e){
			e.printStackTrace();
			throw new Exception("Error 3 during creation of NffgVerifier");
		}
		
		
		
		baseServiceUrl = System.getProperty("it.polito.dp2.NFFG.lab3.URL");
		if(baseServiceUrl==null){
			System.out.println("it.polito.dp2.NFFG.lab3.URL is not set, using:");
			baseServiceUrl=new String("http://localhost:8080/NffgService/rest/");
		}
		//make the last symbol of the path a /
		if(baseServiceUrl.lastIndexOf("/") != baseServiceUrl.length()-1){
			baseServiceUrl = baseServiceUrl + "/";
		}
		System.out.println("Base service URL: " + baseServiceUrl);
	}
	
	@Override
	public void loadNFFG(String name) throws UnknownNameException, AlreadyLoadedException, ServiceException {
		
		NffgReader nr = monitor.getNffg(name);
		
		if(nr==null){
			throw new UnknownNameException("Error - The nffg named " + name + " does not exist");
		}
		XNffg xnffg = this.prepareXNffg(nr);
		
		String resourceName = baseServiceUrl + "nffg";
		try{
			XNffg response=
					client.target(resourceName)
					.request(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_XML)
					.post(Entity.xml(of.createNffg(xnffg)),XNffg.class);
			
		}catch(WebApplicationException e){
			
			System.out.println(e.getMessage());
			switch(e.getResponse().getStatus()){
			case ReturnStatus.FORBIDDEN:
				throw new AlreadyLoadedException(ReturnStatus.FORBIDDEN +" - Error - Nffg already existing! Impossible to create it!\n" +e.getMessage());
			case ReturnStatus.INTERNAL_SERVER_ERROR:
				throw new  ServiceException(e.getResponse().getStatus()+" - Error " +e.getMessage());
			case ReturnStatus.BAD_REQUEST:
				throw new  ServiceException(e.getResponse().getStatus()+" - Error " +e.getMessage());
			default:
				throw new ServiceException(e.getResponse().getStatus()+" Error - Unexpected exception, impossble to create the nffg\n" + e.getMessage());
			}
		
		}
		catch(Exception e){
			e.printStackTrace();
			throw new ServiceException("Error - Unexpected exception, impossble to create the nffg \n" + e.getMessage());
		}
		
		
		
		
	}
	
	
	@Override
	public void loadAll() throws AlreadyLoadedException, ServiceException {
	
		Set<NffgReader> nffgsToAdd = monitor.getNffgs();
		Set<PolicyReader> policiesToAdd = monitor.getPolicies();
		XNffgs response=null;
		
		XNffgs xnffgs = of.createXNffgs();
		for(NffgReader nr : nffgsToAdd){
			XNffg xnffg = this.prepareXNffg(nr);
			xnffgs.getNffg().add(xnffg);
		}
		
		String resourceName = baseServiceUrl + "nffgs";
		
		System.out.println(resourceName);
		//send the set of nffgs without policies
		try{
			response = 
			 client.target(resourceName)
					 .request(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_XML)
					.post(Entity.xml(of.createNffgs(xnffgs)),XNffgs.class);
		}catch(WebApplicationException e){
			System.out.println(e.getMessage());
			switch(e.getResponse().getStatus()){
			case ReturnStatus.FORBIDDEN:
				throw new AlreadyLoadedException(ReturnStatus.FORBIDDEN +" - Error - Nffgs At least one nffg is already existing in the server!");
			
			case ReturnStatus.INTERNAL_SERVER_ERROR:
				throw new  ServiceException(e.getResponse().getStatus()+" - Error " +e.getMessage());
			
			case ReturnStatus.BAD_REQUEST:
				throw new  ServiceException(e.getResponse().getStatus()+" - Error " +e.getMessage());
				
			default:
				throw new ServiceException(e.getResponse().getStatus()+" Error - Nffgs reported an unexpected error while adding them.");
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new ServiceException("--Error - Nffgs Unexpected error while creating the set of nffgs");
		}
		
		
		
		//Create and eventually overwrite the policies
		for(PolicyReader pr : policiesToAdd){
				XPolicy xpolicy = this.prepareXPolicy(pr);
				
				XPolicy responseP;
				resourceName = baseServiceUrl + "policies/"+xpolicy.getName();
				System.out.println(resourceName);
				try{
					responseP = client.target(resourceName)
							.request(MediaType.APPLICATION_XML)
							.accept(MediaType.APPLICATION_XML)
							.put(Entity.xml(of.createPolicy(xpolicy)),XPolicy.class);
					
				
				}catch(WebApplicationException e){
					System.out.println(e.getMessage());

					switch(e.getResponse().getStatus()){
					case ReturnStatus.FORBIDDEN:
						throw new  ServiceException(e.getResponse().getStatus()+" - Error - Nffg or Node Not Existing\n" +e.getMessage());
						
					case ReturnStatus.INTERNAL_SERVER_ERROR:
						throw new  ServiceException(e.getResponse().getStatus()+" - Error " +e.getMessage());
					
					case ReturnStatus.BAD_REQUEST:
						throw new  ServiceException(e.getResponse().getStatus()+" - Error " +e.getMessage());
					
					default:
						throw new ServiceException(e.getResponse().getStatus()+" Error - while creating a new policy");
					}
				}catch(Exception e){
					e.printStackTrace();
					System.out.println(e.getMessage());
					throw new ServiceException("Error - Unexpected error while creating the set of policies");
				}
		}
		
		
	}
	
	@Override
	public void loadReachabilityPolicy(String name, String nffgName, boolean isPositive, String srcNodeName,String dstNodeName) throws UnknownNameException, ServiceException {
		
		
		//Local verification of nffg name and nodes to throw eventually an exception
		if(monitor.getNffg(nffgName)==null){
			System.out.println("Error - The Nffg the policy refers to is not existing");
			throw new UnknownNameException("Error - The Nffg the policy refers to is not existing");
		}else{
			if(monitor.getNffg(nffgName).getNode(srcNodeName)==null || 
					monitor.getNffg(nffgName).getNode(dstNodeName)==null){
				System.out.println("Error - At least one node the policy refers to it is not existing in the Nffg!");
				throw new UnknownNameException("Error - At least one node the policy refers to it is not existing in the Nffg!");
			}
		}
		
		XPolicy xpolicy = of.createXPolicy();
		xpolicy.setName(name);
		xpolicy.setNffg(nffgName);
		xpolicy.setSrc(srcNodeName);
		xpolicy.setDst(dstNodeName);
		xpolicy.setPositivity(isPositive);
		xpolicy.setVerification(null);
		
		String resourceName = baseServiceUrl + "policies/"+name;
		XPolicy response;
		try{
			response = client.target(resourceName)
					.request(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_XML)
					.put(Entity.xml(of.createPolicy(xpolicy)),XPolicy.class);
			
		}catch(WebApplicationException e){
			System.out.println(e.getMessage());

			
			switch(e.getResponse().getStatus()){
			
			case ReturnStatus.FORBIDDEN:
				System.out.println(e.getMessage());
				throw new UnknownNameException(e.getResponse().getStatus() + " - Error - Nffg or node of the policy refers to is not existing");
			
			case ReturnStatus.INTERNAL_SERVER_ERROR:
				throw new  ServiceException(e.getResponse().getStatus()+" - Error " +e.getMessage());
			
			case ReturnStatus.BAD_REQUEST:
				throw new  ServiceException(e.getResponse().getStatus()+" - Error " +e.getMessage());
			default:
				throw new ServiceException("Error - Policy unexpected error during deletion of the policy");
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new ServiceException("Error - Policy Unexpected error while creating the set of policies");
		}
		
	}



	@Override
	public void unloadReachabilityPolicy(String name) throws UnknownNameException, ServiceException {
		String resourceName = baseServiceUrl + "policies/"+ name;
		XPolicy response;
		try{
			response = client.target(resourceName)
					.request()
					.accept(MediaType.APPLICATION_XML)
					.delete(XPolicy.class);
			
		}catch(WebApplicationException e){
			System.out.println(e.getMessage());

			switch(e.getResponse().getStatus()){
			case ReturnStatus.NOT_FOUND:
				System.out.println(e.getMessage());
				throw new UnknownNameException(ReturnStatus.NOT_FOUND + " Error - The policy you want to delete is not existing");
			case ReturnStatus.INTERNAL_SERVER_ERROR:
				throw new  ServiceException(e.getResponse().getStatus()+" - Error " +e.getMessage());
			default:
				throw new ServiceException("--Error - Policy unexpected error during deletion of the policy");
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new ServiceException("--Error - Policy unexpected error during deletion of the policy");
		}
		
	}

	@Override
	public boolean testReachabilityPolicy(String name) throws UnknownNameException, ServiceException {
		String resourceName = baseServiceUrl + "policies/"+ name;
		
		XPolicy response;
		try{
			response = client.target(resourceName)
					.request(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_XML)
					.post(null,XPolicy.class);
		}catch(WebApplicationException e){
			System.out.println(e.getMessage());

			switch(e.getResponse().getStatus()){
			case ReturnStatus.NOT_FOUND:
				throw new UnknownNameException(ReturnStatus.NOT_FOUND + " Error - The policy you want to verify is not existing");
			
			case ReturnStatus.INTERNAL_SERVER_ERROR:
				throw new  ServiceException(e.getResponse().getStatus()+" - Error " +e.getMessage());
			
			default:			
				throw new ServiceException(e.getResponse().getStatus()+ "--Error - Policy unexpected error verification of the policy");
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new ServiceException("Error - Policy unexpected error during deletion of the policy");
		}
		
		//accept also traversal policies
		return response.getVerification().isResult();
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
			System.out.println("Error in converting data type");
			e.printStackTrace();
		}
		return date2;
	}
	
	
	/*
	 * Method used to prepare a xNffg to send it to the web service.
	 * Used by many methods
	 */
	private XNffg prepareXNffg(NffgReader nr){
		XNffg xnffg = new XNffg();
		xnffg.setName(nr.getName());
		xnffg.setLastUpdate(this.convertCalendar(nr.getUpdateTime()));
	
		//create the list of nodes
		XNodes xnodes = new XNodes();
		for(NodeReader node : nr.getNodes()){
			XNode xnode = new XNode();
			xnode.setName(node.getName());
			xnode.setFunctionality(XFunctionality.fromValue(node.getFuncType().toString()));
			xnodes.getNode().add(xnode);
		}
		xnffg.setNodes(xnodes);
		
		//create the list of links
		XLinks xlinks = new XLinks();
		nr.getNodes().stream().forEach(n->{
			n.getLinks().forEach(l->{
				XLink xlink = new XLink();
				xlink.setName(l.getName());
				xlink.setSrc(n.getName());
				xlink.setDst(l.getDestinationNode().getName());
				
				xlinks.getLink().add(xlink);
			});
		});
		
		xnffg.setLinks(xlinks);
		return xnffg;
	}
	
	/*
	 * Method used to prepare a Policy to send it to the web service.
	 * Used by mane methods
	 */
	private XPolicy prepareXPolicy(PolicyReader pr){
		XPolicy xpolicy = of.createXPolicy();
		xpolicy.setName(pr.getName());
		xpolicy.setNffg(pr.getNffg().getName());
		xpolicy.setPositivity(pr.isPositive());
		
		//policy verification data
		XVerification xverification = new XVerification();
		if(pr.getResult()!=null){
			if(pr.getResult().getVerificationTime()!=null){
				xverification.setMessage(pr.getResult().getVerificationResultMsg());
				xverification.setVerificationTime(this.convertCalendar(pr.getResult().getVerificationTime()));
				xverification.setResult(pr.getResult().getVerificationResult());
				xpolicy.setVerification(xverification);
			}else{
				xpolicy.setVerification(null);
			}
		}
		else{
			xpolicy.setVerification(null);
		}
		
		

		//policy source/destination
		if(pr instanceof ReachabilityPolicyReader){
			xpolicy.setSrc(((ReachabilityPolicyReader) pr).getSourceNode().getName());
			xpolicy.setDst(((ReachabilityPolicyReader) pr).getDestinationNode().getName());
		}
		//for traversal policies also include the traversed functions 
		if(pr instanceof TraversalPolicyReader){
			xpolicy.setSrc(((ReachabilityPolicyReader) pr).getSourceNode().getName());
			xpolicy.setDst(((ReachabilityPolicyReader) pr).getDestinationNode().getName());
			XTraversal xtraversal = of.createXTraversal();
			((TraversalPolicyReader) pr).getTraversedFuctionalTypes().stream().forEach(f->{
				XFunctionality.fromValue(f.toString());
				xtraversal.getFunctionality().add(XFunctionality.fromValue(f.toString()));
			});
			xpolicy.setTraversal(xtraversal);
		}
		return xpolicy;
	}
	
}
