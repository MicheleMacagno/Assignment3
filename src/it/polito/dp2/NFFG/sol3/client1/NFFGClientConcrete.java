package it.polito.dp2.NFFG.sol3.client1;

import it.polito.dp2.NFFG.lab3.AlreadyLoadedException;
import it.polito.dp2.NFFG.lab3.NFFGClient;
import it.polito.dp2.NFFG.lab3.ServiceException;
import it.polito.dp2.NFFG.lab3.UnknownNameException;
import it.polito.dp2.NFFG.sol3.bindings.XLinks;
import it.polito.dp2.NFFG.sol3.bindings.XFunctionality;
import it.polito.dp2.NFFG.sol3.bindings.XLink;
import it.polito.dp2.NFFG.sol3.bindings.XNffg;
import it.polito.dp2.NFFG.sol3.bindings.XNffgs;
import it.polito.dp2.NFFG.sol3.bindings.XNodes;
import it.polito.dp2.NFFG.sol3.bindings.XPolicies;
import it.polito.dp2.NFFG.sol3.bindings.XPolicy;
import it.polito.dp2.NFFG.sol3.bindings.XTraversal;
import it.polito.dp2.NFFG.sol3.bindings.XVerification;
import it.polito.dp2.NFFG.sol3.bindings.XNode;
import com.sun.jersey.api.client.Client;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
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
	
	public NFFGClientConcrete() throws ServiceException{
		try {
			client = Client.create();
			factory = it.polito.dp2.NFFG.NffgVerifierFactory.newInstance();
			monitor = factory.newNffgVerifier();
		} catch (NffgVerifierException e) {
			e.printStackTrace();
			throw new ServiceException("Error 1 during creation of NffgVerifier");
		}catch(Exception e){
			e.printStackTrace();
			throw new ServiceException("Error 2 during creation of NffgVerifier");
		}
		
		baseServiceUrl = System.getProperty("it.polito.dp2.NFFG.lab3.URL");
		if(baseServiceUrl==null){
			baseServiceUrl=new String("http://localhost:8080/NffgService/rest/");
		}
		System.out.println(baseServiceUrl);
	}
	
	@Override
	public void loadNFFG(String name) throws UnknownNameException, AlreadyLoadedException, ServiceException {
		
		NffgReader nr = monitor.getNffg(name);
		
		if(nr==null){
			throw new UnknownNameException("The nffg named " + name + " does not exist");
		}
		XNffg xnffg = this.prepareXNffg(nr);
		
		String resourceName = baseServiceUrl + "/nffg";
		try{
			XNffg response=
					client.resource(resourceName)
					.type(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_XML)
					.post(XNffg.class,xnffg);
		}catch(ForbiddenException fe){
			System.out.println("Nffg already existing! Impossible to create it!");
			fe.printStackTrace();
			throw new AlreadyLoadedException("Error - Nffg already existing! Impossible to create it!");
		}
		catch(Exception e){
			throw new ServiceException("Error - Unexpected exception, impossble to create the nffg ");
		}
		
		
		
		
	}
	
	/*
	 * Method used to prepare a Nffg to send it to the web service.
	 * Used by mane methods
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
		return xnffg;
	}
	
	/*
	 * Method used to prepare a Policy to send it to the web service.
	 * Used by mane methods
	 */
	private XPolicy prepareXPolicy(PolicyReader pr){
		XPolicy xpolicy = new XPolicy();
		xpolicy.setName(pr.getName());
		xpolicy.setNffg(pr.getNffg().getName());
		xpolicy.setPositivity(pr.isPositive());
		
		//policy verification data
		XVerification xverification = new XVerification();
		xverification.setMessage(pr.getResult().getVerificationResultMsg());
		xverification.setVerificationTime(this.convertCalendar(pr.getResult().getVerificationTime()));
		xverification.setResult(pr.getResult().getVerificationResult());

		//policy source/destination
		if(pr instanceof ReachabilityPolicyReader){
			xpolicy.setSrc(((ReachabilityPolicyReader) pr).getSourceNode().getName());
			xpolicy.setDst(((ReachabilityPolicyReader) pr).getDestinationNode().getName());
		}
		//for traversal policies also include the traversed functions 
		if(pr instanceof TraversalPolicyReader){
			xpolicy.setSrc(((ReachabilityPolicyReader) pr).getSourceNode().getName());
			xpolicy.setDst(((ReachabilityPolicyReader) pr).getDestinationNode().getName());
			XTraversal xtraversal = new XTraversal();
			((TraversalPolicyReader) pr).getTraversedFuctionalTypes().stream().forEach(f->{
				XFunctionality.fromValue(f.toString());
				xtraversal.getFunctionality().add(XFunctionality.fromValue(f.toString()));
			});
			xpolicy.setTraversal(xtraversal);
		}
		return xpolicy;
	}

	@Override
	public void loadAll() throws AlreadyLoadedException, ServiceException {
	
//		LinkedHashMap<String,XNffg> mapXNffg = new LinkedHashMap<String,XNffg>();
		Set<NffgReader> nffgsToAdd = monitor.getNffgs();
		Set<PolicyReader> policiesToAdd = monitor.getPolicies();
		XNffgs response=null;
		//verify there are no duplicated nffgs in the system
		
		XNffgs xnffgs = new XNffgs();
		for(NffgReader nr : nffgsToAdd){
			XNffg xnffg = this.prepareXNffg(nr);
			xnffgs.getNffg().add(xnffg);
		}
		
		String resourceName = baseServiceUrl + "/nffgs";
		//send the set of nffgs without policies
		try{
			response = client.resource(resourceName)
					.accept(MediaType.APPLICATION_XML)
					.type(MediaType.APPLICATION_XML)
					.post(XNffgs.class,xnffgs);
		}catch(ForbiddenException e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new AlreadyLoadedException("Error - Nffgs At least one nffg is already existing in the server!");
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new ServiceException("Error - Nffgs Unexpected error while creating the set of nffgs");
		}
		
		//eventually remove the policies
		XPolicies xpolicies = new XPolicies();
		for(PolicyReader pr : policiesToAdd){
			XPolicy xpolicy = this.prepareXPolicy(pr);
			xpolicies.getPolicy().add(xpolicy);
		}
		
		//send the set of policies all together
		resourceName = baseServiceUrl + "/policies?overwrite=y";
		XPolicies responseP;
		try{
			responseP = client.resource(resourceName)
					.accept(MediaType.APPLICATION_XML)
					.type(MediaType.APPLICATION_XML)
					.post(XPolicies.class,xpolicies);
		}catch(NotAllowedException e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new ServiceException("Error - Wrong query url! Use ?overwrite=y");
		}catch(NotFoundException e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new ServiceException("Error - Nffg referring to a policy is not existing");
			
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new ServiceException("Error - Policies Unexpected error while creating the set of policies");
		}
	}

	@Override
	public void loadReachabilityPolicy(String name, String nffgName, boolean isPositive, String srcNodeName,
			String dstNodeName) throws UnknownNameException, ServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unloadReachabilityPolicy(String name) throws UnknownNameException, ServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean testReachabilityPolicy(String name) throws UnknownNameException, ServiceException {
		// TODO Auto-generated method stub
		return false;
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
