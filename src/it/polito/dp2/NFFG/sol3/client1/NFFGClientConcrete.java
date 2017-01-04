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
	 * Usedb by mane methods
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

	@Override
	public void loadAll() throws AlreadyLoadedException, ServiceException {
	
//		LinkedHashMap<String,XNffg> mapXNffg = new LinkedHashMap<String,XNffg>();
		Set<NffgReader> nffgsToAdd = monitor.getNffgs();
		XNffgs response=null;
		//verify there are no duplicated nffgs in the system
		
		XNffgs xnffgs = new XNffgs();
		for(NffgReader nr : nffgsToAdd){
			XNffg xnffg = this.prepareXNffg(nr);
			xnffgs.getNffg().add(xnffg);
		}
		
		String resourceName = baseServiceUrl + "/nffgs";
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
//TODO -- SONO arrivato qui		
		//eventually remove the policies
		
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
