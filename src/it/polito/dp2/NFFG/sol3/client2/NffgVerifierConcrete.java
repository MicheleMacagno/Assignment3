package it.polito.dp2.NFFG.sol3.client2;

import java.io.File;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.SchemaFactory;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NffgVerifier;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.NodeReader;
import it.polito.dp2.NFFG.PolicyReader;
import it.polito.dp2.NFFG.sol3.bindings.ObjectFactory;
import it.polito.dp2.NFFG.sol3.bindings.XNffgs;
import it.polito.dp2.NFFG.sol3.bindings.XPolicies;
import it.polito.dp2.NFFG.sol3.client1.ReturnStatus;

public class NffgVerifierConcrete implements NffgVerifier {

	private String baseServiceUrl="http://localhost:8080/NffgService/rest/";
	private Client client;
	private ObjectFactory of; //it can be used to debug the code
	
	private Set<NffgReader> setNffgReader = null;
	private Set<PolicyReader> setPolicyReader = null;
	
	private XNffgs xnffgs=null;
	private XPolicies xpolicies = null;
	
	public NffgVerifierConcrete(String baseServiceUrl) throws NffgVerifierException{
		try{
			client = ClientBuilder.newClient();
			of=new ObjectFactory();
			
			setNffgReader = new LinkedHashSet<NffgReader>(0);
			setPolicyReader = new LinkedHashSet<PolicyReader>(0);
			
			//get all nffgs from  web service
			String resourceName = baseServiceUrl + "nffgs";
			try{
				xnffgs=
					client.target(resourceName)
					.request(MediaType.APPLICATION_XML)
					.get(XNffgs.class);
				
				//DEBUG - enable to debug
				//this.printXML(null,of.createNffgs(xnffgs));
			}catch(WebApplicationException e){
				System.out.println(e.getMessage());
				if(e.getResponse().getStatus()==ReturnStatus.NOT_FOUND){
					System.out.println("404 Error - Probably the service is not available!!");
					throw new NffgVerifierException(e);
				}
				throw new NffgVerifierException(e);
			}catch(Exception e){
			
				System.out.println("404 Error - Probably the service is not available!!");
				System.out.println("Error - Unexcpected error while trying to retrieve the set of nffgs contacting the web service");
				//e.printStackTrace();
				System.out.println(e.getMessage());
				throw new NffgVerifierException(e);
			}
			
			//get all policies from web service
			resourceName = baseServiceUrl + "policies";
			try{
				xpolicies =
					client.target(resourceName)
					.request(MediaType.APPLICATION_XML)
					.get(XPolicies.class);
				
				//DEBUG - enable to debug
				//this.printXML(null,of.createPolicies(xpolicies));
				
				createNffgReader();
			}catch(Exception e){
				System.out.println("Error - Unexcpected error while trying to retrieve the set of policies contacting the web service");
				e.printStackTrace();
				throw new NffgVerifierException(e);
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
			throw new NffgVerifierException("Impossible to instantiate the class!");
		}
	}
	

	@Override
	public NffgReader getNffg(String name) {
		for(NffgReader nr : setNffgReader){
			if(nr.getName().equals(name)){
				return nr;
			}
		}
		//in case nothing found it must return null		
		return null;
	}

	@Override
	public Set<NffgReader> getNffgs() {
		//it must never return null
		return setNffgReader;
	}

	@Override
	public Set<PolicyReader> getPolicies() {
		//it must never return null
		return setPolicyReader;
	}

	@Override
	public Set<PolicyReader> getPolicies(String namenffg) {
		Set<PolicyReader> toReturn = 
				setPolicyReader.stream().filter(p->{
			return(	p.getNffg().getName().equals(namenffg) );
		}).collect(Collectors.toSet());
		
		//in case nothing found, return null - by definition
		if(toReturn.size()==0){
			return null;
		}
		else{
			return toReturn;
		}
	}

	@Override
	public Set<PolicyReader> getPolicies(Calendar verificationtime) {
		Set<PolicyReader> toReturn =
				setPolicyReader.stream().filter(p->{
						//verify if verification data exists for the following policy
						if(p.getResult()==null) {
							return false;
						}
						//if exist, verify if the verification date is grater than the current one
						return((p.getResult().getVerificationTime().compareTo(verificationtime))> 0 );
		}).collect(Collectors.toSet());
		
		//in case nothing found, return null - by definition
		if(toReturn.size()==0){
			return null;
		}
		else{
			return toReturn;
		}
	}

	
	public String getBaseServiceUrl() {
		return baseServiceUrl;
	}

	public void setBaseServiceUrl(String baseServiceUrl) {
		this.baseServiceUrl = baseServiceUrl;
	}
	
	private void createNffgReader(){
		//create Set of NffgReader
		xnffgs.getNffg().stream().forEach(nffg->{
			NffgReaderConcrete tmpNffg = new NffgReaderConcrete(nffg);
			setNffgReader.add(tmpNffg);
		});
		
		//extract the correspondent NffgReader
		xnffgs.getNffg().stream().forEach(nffg ->{
			NffgReader nr=
				setNffgReader.stream().filter(a->{
					return a.getName().equals(nffg.getName());
				}).collect(Collectors.toList()).get(0);
			
			xpolicies.getPolicy().stream().filter(p->{
				return(p.getNffg().equals(nffg.getName()));
			})
			.collect(Collectors.toList())
			.stream().forEach(policy -> {
							NodeReader nrSrc = nr.getNodes().stream().filter(n->{
								return(	n.getName().equals(policy.getSrc())	);
								
							}).collect(Collectors.toList()).get(0);
							
							NodeReader nrDst =  nr.getNodes().stream().filter(n->{
								return(	n.getName().equals(policy.getDst())	);
								
							}).collect(Collectors.toList()).get(0);
				
							if(policy.getTraversal()==null){
								//REACHABILITY POLICY
								//it is NULL in case of traversal policy
								
								ReachabilityPolicyReaderConcrete rpr = new ReachabilityPolicyReaderConcrete(policy, nr, nrSrc, nrDst);
								setPolicyReader.add(rpr);
							}
							else{
								//TRAVERSAL POLICY
								TraversalPolicyReaderConcrete tpr = new TraversalPolicyReaderConcrete(policy, nr, nrSrc, nrDst);
								setPolicyReader.add(tpr);
							}
			
			});
		});
			
			
			
			
	}
	
	/*
	 * This method is NEVER called in final release.
	 * It is written only to check on standard outout that the 
	 * result is the expected one.
	 * I do not removed it because it can be useful in the final exam for debug purposes
	 */
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
			//jaxbMarshaller.setSchema(sf.newSchema(new File("xsd/nffgVerifier.xsd")));
			//ObjectFactory objectFactory = new ObjectFactory();
			//JAXBElement<XPolicies> je = objectFactory.createPolicies(xpolicies);
		    		   
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
}
