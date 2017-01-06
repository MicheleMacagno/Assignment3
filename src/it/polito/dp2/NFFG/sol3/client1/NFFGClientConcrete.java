package it.polito.dp2.NFFG.sol3.client1;

import it.polito.dp2.NFFG.lab3.AlreadyLoadedException;
import it.polito.dp2.NFFG.lab3.NFFGClient;
import it.polito.dp2.NFFG.lab3.ServiceException;
import it.polito.dp2.NFFG.lab3.UnknownNameException;
import it.polito.dp2.NFFG.sol3.bindings.XLinks;
import it.polito.dp2.NFFG.sol3.bindings.ObjectFactory;
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
import com.sun.jersey.api.client.UniformInterfaceException;

import java.io.File;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import it.polito.dp2.NFFG.*;


public class NFFGClientConcrete implements it.polito.dp2.NFFG.lab3.NFFGClient {
	NffgVerifier monitor;
	NffgVerifierFactory factory=null;
	String baseServiceUrl = null;
	Client client=null;
	ObjectFactory of = new ObjectFactory();
	
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
					client.resource(resourceName)
					.type(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_XML)
					.entity(of.createNffg(xnffg))
					.post(XNffg.class);
			
			//DEBUG
//			this.printXML(null,of.createNffg(response));
		}catch(UniformInterfaceException e){
			System.out.println(e.getResponse().getEntity(String.class));
			switch(e.getResponse().getStatus()){
			case ReturnStatus.FORBIDDEN:
				throw new AlreadyLoadedException(ReturnStatus.FORBIDDEN +" - Error - Nffg already existing! Impossible to create it!");
			
			default:
				throw new ServiceException(e.getResponse().getStatus()+" Error - Unexpected exception, impossble to create the nffg");
			}
		
		}catch(ForbiddenException fe){
			System.out.println("Nffg already existing! Impossible to create it!");
			fe.printStackTrace();
			throw new AlreadyLoadedException("Error - Nffg already existing! Impossible to create it!");
		}
		catch(Exception e){
			e.printStackTrace();
			throw new ServiceException("Error - Unexpected exception, impossble to create the nffg ");
		}
		
		
		
		
	}
	
	

	/*
	 * NB as we have no @XmlRootElement, you have to post objectFactory.createXPolicies and createXNffgs
	 * (non-Javadoc)
	 * @see it.polito.dp2.NFFG.lab3.NFFGClient#loadAll()
	 */
	@Override
	public void loadAll() throws AlreadyLoadedException, ServiceException {
	
//		LinkedHashMap<String,XNffg> mapXNffg = new LinkedHashMap<String,XNffg>();
		Set<NffgReader> nffgsToAdd = monitor.getNffgs();
		Set<PolicyReader> policiesToAdd = monitor.getPolicies();
		XNffgs response=null;
		//verify there are no duplicated nffgs in the system
		
		ObjectFactory of = new ObjectFactory();
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
			 client.resource(resourceName)
					.accept(MediaType.APPLICATION_XML)
					.type(MediaType.APPLICATION_XML)
					.entity(of.createNffgs(xnffgs))
					.post(XNffgs.class);
		}catch(UniformInterfaceException e){
			System.out.println(e.getResponse().getEntity(String.class));

			switch(e.getResponse().getStatus()){
			case ReturnStatus.FORBIDDEN:
				throw new AlreadyLoadedException(ReturnStatus.FORBIDDEN +" - Error - Nffgs At least one nffg is already existing in the server!");
			
			default:
				throw new ServiceException(e.getResponse().getStatus()+" Error - Nffgs reported an unexpected error while adding them.");
			}
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new ServiceException("--Error - Nffgs Unexpected error while creating the set of nffgs");
		}
		
		//eventually remove the policies
		XPolicies xpolicies = of.createXPolicies();
		for(PolicyReader pr : policiesToAdd){
			try{
				XPolicy xpolicy = this.prepareXPolicy(pr);
				xpolicies.getPolicy().add(xpolicy);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		//send the set of policies all together
		resourceName = baseServiceUrl + "policies";
		System.out.println(resourceName);
		XPolicies responseP;

//		DEBUG
//		xpolicies.getPolicy().forEach(p->{
//			System.out.println(p.getName());
//			System.out.println(p.getNffg());
//			System.out.println(p.getSrc());
//			System.out.println(p.getDst());
//			System.out.println(p.getTraversal());
//			System.out.println(p.getVerification());
//		});
		
//		this.printXML("prova.xml", xpolicies);
		
		try{
			responseP = client.resource(resourceName)
					.accept("application/xml")
					.type("application/xml")
					.entity(of.createPolicies(xpolicies))
					.post(XPolicies.class);
			
//			this.printXML(null, of.createPolicies(responseP));
		
		}catch(UniformInterfaceException e){
			System.out.println(e.getResponse().getEntity(String.class));
			System.out.println(e.getMessage());

			switch(e.getResponse().getStatus()){
			case ReturnStatus.NOT_FOUND:
				throw new ServiceException(ReturnStatus.NOT_FOUND +" - Error - Nffg referring to the policy is not existing");
			
			case ReturnStatus.NOT_ALLOWED:
				throw new ServiceException(ReturnStatus.NOT_ALLOWED +" Error - Wrong query url! Use ?overwrite=y");
			
			default:
				throw new ServiceException(e.getResponse().getStatus()+" Error - Policy unexpected error during deletion of the policy");
			}
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new ServiceException("-- Error - Policies Unexpected error while creating the set of policies");
		}
	}

	@Override
	public void loadReachabilityPolicy(String name, String nffgName, boolean isPositive, String srcNodeName,String dstNodeName) throws UnknownNameException, ServiceException {
		
		XPolicy xpolicy = of.createXPolicy();
		xpolicy.setName(name);
		xpolicy.setNffg(nffgName);
		xpolicy.setSrc(srcNodeName);
		xpolicy.setDst(dstNodeName);
		xpolicy.setPositivity(isPositive);
		xpolicy.setVerification(null);
		
		String resourceName = baseServiceUrl + "policy";
		XPolicy response;
		try{
			response = client.resource(resourceName)
					.accept(MediaType.APPLICATION_XML)
					.type(MediaType.APPLICATION_XML)
					.entity(of.createPolicy(xpolicy))
					.post(XPolicy.class);
//			this.printXML(null, of.createPolicy(response));
		}catch(UniformInterfaceException e){
			System.out.println(e.getResponse().getEntity(String.class));
			System.out.println(e.getMessage());

			if((e.getResponse().getStatus()==ReturnStatus.NOT_FOUND)){
				System.out.println(e.getResponse().getEntity(String.class));
				throw new UnknownNameException(ReturnStatus.NOT_FOUND+ " - Error - Nffg referring to the policy is not existing");
			}
			throw new ServiceException("Error - Policy unexpected error during deletion of the policy");
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new ServiceException("Error - Policy Unexpected error while creating the set of policies");
		}
		
	}

	/*
	 * This method contains the correct way to check the received exception
	 * Please extend the usage of UniformInterfaceException to all other methods
	 * (non-Javadoc)
	 * @see it.polito.dp2.NFFG.lab3.NFFGClient#unloadReachabilityPolicy(java.lang.String)
	 */
	@Override
	public void unloadReachabilityPolicy(String name) throws UnknownNameException, ServiceException {
		String resourceName = baseServiceUrl + "policy/"+ name;
		XPolicy response;
		try{
			response = client.resource(resourceName)
					.accept(MediaType.APPLICATION_XML)
					.type(MediaType.APPLICATION_XML)
					.delete(XPolicy.class);
			
//			this.printXML(null, of.createPolicy(response));
		}catch(UniformInterfaceException e){
			System.out.println(e.getResponse().getEntity(String.class));
			System.out.println(e.getMessage());

			if((e.getResponse().getStatus()==ReturnStatus.NOT_FOUND)){
				System.out.println(e.getResponse().getEntity(String.class));
				throw new UnknownNameException(ReturnStatus.NOT_FOUND + " Error - The policy you want to delete is not existing");
			}
			throw new ServiceException("--Error - Policy unexpected error during deletion of the policy");
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new ServiceException("--Error - Policy unexpected error during deletion of the policy");
		}
		
	}

	@Override
	public boolean testReachabilityPolicy(String name) throws UnknownNameException, ServiceException {
		String resourceName = baseServiceUrl + "policy/"+ name;
		
		XPolicy response;
		try{
			response = client.resource(resourceName)
					.accept(MediaType.APPLICATION_XML)
					.type(MediaType.APPLICATION_XML)
					.post(XPolicy.class);
		}catch(UniformInterfaceException e){
			System.out.println(e.getResponse().getEntity(String.class));
			System.out.println(e.getMessage());

			if((e.getResponse().getStatus()==ReturnStatus.NOT_FOUND)){
				System.out.println(e.getResponse().getEntity(String.class));
				throw new UnknownNameException(ReturnStatus.NOT_FOUND + " Error - The policy you want to verify is not existing");
			}
			throw new ServiceException(e.getResponse().getStatus()+ "--Error - Policy unexpected error verification of the policy");
		
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new ServiceException("Error - Policy unexpected error during deletion of the policy");
		}
//TODO: verify it is really necessary		

//		if(response.getTraversal()!=null){
//			System.out.println("Error - The policy is a Traversal one, not a reachability one!!");
//			throw new ServiceException("Error - The policy is a Traversal one, not a reachability one!! - can't be verified");
//		}
		
		return response.getVerification().isResult();
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
	
	public <T> void printXML(String filename, JAXBElement<T> je){
    	File file = null;
    	
    	JAXBContext jaxbContext;
		try {
		
			SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
			jaxbContext = JAXBContext.newInstance("it.polito.dp2.NFFG.sol3.bindings");
			Marshaller jaxbMarshaller = null;
			jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			//Validation of the schema - from JAXB-unmarshal-validate
//			jaxbMarshaller.setSchema(sf.newSchema(new File("xsd/nffgVerifier.xsd")));
			ObjectFactory objectFactory = new ObjectFactory();
//		    JAXBElement<XPolicies> je = objectFactory.createPolicies(xpolicies);
		    		   
//		     AddressType shipping = je.getValue();
			if(filename!=null){
				try{
					file = new File(filename);
					jaxbMarshaller.marshal(file, System.out);
					System.out.println("XML file correctly written in " + filename);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}else{
				jaxbMarshaller.marshal(je, System.out);
			}
			
		
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
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
	private XPolicy prepareXPolicy(PolicyReader pr) throws Exception{
		ObjectFactory of = new ObjectFactory();
		XPolicy xpolicy = of.createXPolicy();
		xpolicy.setName(pr.getName());
		xpolicy.setNffg(pr.getNffg().getName());
		xpolicy.setPositivity(pr.isPositive());
		
		//policy verification data
		XVerification xverification = new XVerification();
		if(pr.getResult()!=null){
			xverification.setMessage(pr.getResult().getVerificationResultMsg());
			xverification.setVerificationTime(this.convertCalendar(pr.getResult().getVerificationTime()));
			xverification.setResult(pr.getResult().getVerificationResult());
		}
		xpolicy.setVerification(xverification);
		
		

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
