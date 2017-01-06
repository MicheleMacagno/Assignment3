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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;

import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NffgVerifier;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.NodeReader;
import it.polito.dp2.NFFG.PolicyReader;
import it.polito.dp2.NFFG.lab3.AlreadyLoadedException;
import it.polito.dp2.NFFG.lab3.ServiceException;
import it.polito.dp2.NFFG.sol3.bindings.ObjectFactory;
import it.polito.dp2.NFFG.sol3.bindings.XNffg;
import it.polito.dp2.NFFG.sol3.bindings.XNffgs;
import it.polito.dp2.NFFG.sol3.bindings.XPolicies;
import it.polito.dp2.NFFG.sol3.client1.ReturnStatus;

public class NffgVerifierConcrete implements NffgVerifier {

//TODO: modify 8081 to 8080
	private String baseServiceUrl="http://localhost:8081/NffgService/rest/";
	private Client client;
	private ObjectFactory of;
	
	private Set<NffgReader> setNffgReader = null;
	private Set<PolicyReader> setPolicyReader = null;
	
	private XNffgs xnffgs=null;
	private XPolicies xpolicies = null;
	
	public NffgVerifierConcrete(String baseServiceUrl) throws NffgVerifierException{
		try{
			client = Client.create();
			of=new ObjectFactory();
			
			setNffgReader = new LinkedHashSet<NffgReader>(0);
			setPolicyReader = new LinkedHashSet<PolicyReader>(0);
			
			//get all nffgs from  web service
			String resourceName = baseServiceUrl + "nffgs";
			try{
				xnffgs=
					client.resource(resourceName)
					.accept(MediaType.APPLICATION_XML)
					.get(XNffgs.class);
				
				//DEBUG
//				this.printXML(null,of.createNffgs(xnffgs));
			}catch(Exception e){
				System.out.println("Error - Unexcpected error while trying to retrieve the set of nffgs contacting the web service");
				e.printStackTrace();
				throw e;
			}
			
			//get all policies from web service
			resourceName = baseServiceUrl + "policies";
			try{
				xpolicies =
					client.resource(resourceName)
					.accept(MediaType.APPLICATION_XML)
					.get(XPolicies.class);
				
				//DEBUG
//				this.printXML(null,of.createPolicies(xpolicies));
				
				createNffgReader();
			}catch(Exception e){
				System.out.println("Error - Unexcpected error while trying to retrieve the set of policies contacting the web service");
				e.printStackTrace();
				throw e;
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
			throw new NffgVerifierException("Impossible to instantiate the class!");
		}
	}
	

	@Override
	public NffgReader getNffg(String arg0) {
		for(NffgReader nr : setNffgReader){
			if(nr.getName().equals(arg0)){
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
	public Set<PolicyReader> getPolicies(String arg0) {
		Set<PolicyReader> toReturn = 
				setPolicyReader.stream().filter(p->{
			return(	p.getNffg().getName().equals(arg0) );
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
	public Set<PolicyReader> getPolicies(Calendar arg0) {
		Set<PolicyReader> toReturn =
				setPolicyReader.stream().filter(p->{
						//verify if verification data exists for the following policy
						if(p.getResult()==null) {
							return false;
						}
						//if exist, verify if the verification date is grater than the current one
						return((p.getResult().getVerificationTime().compareTo(arg0))> 0 );
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
//TODO: FIX IT
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
								
//									Traversal t = policy.getTraversal();
								
								TraversalPolicyReaderConcrete tpr = new TraversalPolicyReaderConcrete(policy, nr, nrSrc, nrDst);
								setPolicyReader.add(tpr);
							}
			
			});
		});
			
			
			
			
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
//			jaxbMarshaller.setSchema(sf.newSchema(new File("xsd/nffgInfo.xsd")));
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
}
