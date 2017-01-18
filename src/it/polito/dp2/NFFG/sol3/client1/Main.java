package it.polito.dp2.NFFG.sol3.client1;

import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.ClientHandler;

import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NffgVerifier;
import it.polito.dp2.NFFG.NffgVerifierException;
import it.polito.dp2.NFFG.NffgVerifierFactory;
import it.polito.dp2.NFFG.lab3.AlreadyLoadedException;
import it.polito.dp2.NFFG.lab3.NFFGClient;
import it.polito.dp2.NFFG.lab3.NFFGClientException;
import it.polito.dp2.NFFG.lab3.ServiceException;
import it.polito.dp2.NFFG.lab3.UnknownNameException;
import it.polito.dp2.NFFG.sol3.bindings.XNffg;
import it.polito.dp2.NFFG.sol3.bindings.XPolicy;
import it.polito.dp2.NFFG.sol3.client2.NffgVerifierConcrete;

public class Main {
	public static void main(String args[]){
//		testGetEmptySetNffgs();
//		testPostGetSetNffgs();
//		testGeneraErrori();
//		testPostSingleNffg();
//		testVerificaPolicyErrate();
		testVerificaPolicyOnTheGo();
	}
	
	public static void testPostSingleNffg(){
//		System.setProperty("it.polito.dp2.NFFG.NFFGClientFactory",
//				"it.polito.dp2.NFFG.sol3.client1.NFFGClientFactory");
		System.setProperty("it.polito.dp2.NFFG.lab3.URL", "http://localhost:8080/NffgService/rest/");
		
		System.out.println("MAIN IS RUNNING!!!!!!!!!!!!!!!!!!!");
		NFFGClient nc = null;
		NFFGClientFactory ncf = (NFFGClientFactory) NFFGClientFactory.newInstance();
		NffgVerifierFactory nvf = NffgVerifierFactory.newInstance();
		NffgVerifier nv = null;
		try {
			nv = nvf.newNffgVerifier();
		} catch (NffgVerifierException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			nc = ncf.newNFFGClient();
		} catch (NFFGClientException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		try {
			
// --TEST INSERT SINGLE NFFG
				try {
					nc.loadNFFG("Nffg2");
				} catch (UnknownNameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
// --TEST INSERT SINGLE NFFG

//----------------------------------------------------------------------------------------
				

				//------------------------------------------------------------------------
//				nc.loadAll();
				
				
//				try {
//					nc.loadAll();
//					int i=0;
//					Boolean risultato = true;
//					
//					while(risultato){
//							
//						System.out.println("Policy"+i);	
//							if((risultato=nc.testReachabilityPolicy("Policy"+i))){
//								System.out.println("Reachable");
//							}
//							else{
//								System.out.println("Not Reachable");
//							}
//							i++;
//						}
//				} catch (UnknownNameException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			
				
				
//				try {
//				
//					nc.loadReachabilityPolicy("PolicyMiky", "Nffg0", true, "WEBSERVER1", "MAILCLIENT1");
//					nc.unloadReachabilityPolicy("PolicyMiky");
//					nc.unloadReachabilityPolicy("PolicyMiky");
//					
//				} catch (UnknownNameException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				
		} catch (AlreadyLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testPostSetNffgs(){
		NFFGClient nc = null;
//		System.setProperty("it.polito.dp2.NFFG.NFFGClientFactory",
//				"it.polito.dp2.NFFG.sol3.client1.NFFGClientFactory");
		System.setProperty("it.polito.dp2.NFFG.lab3.URL", "http://localhost:8080/NffgService/rest/");
		
		System.out.println("MAIN IS RUNNING!!!!!!!!!!!!!!!!!!!");
		NFFGClientFactory ncf = (NFFGClientFactory) NFFGClientFactory.newInstance();
		NffgVerifierFactory nvf = NffgVerifierFactory.newInstance();
		NffgVerifier nv = null;
		try {
			nv = nvf.newNffgVerifier();
		} catch (NffgVerifierException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			nc = ncf.newNFFGClient();
		} catch (NFFGClientException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		try {
			
// --TEST INSERT SINGLE NFFG
				try {
					nc.loadNFFG("Nffg0");
				} catch (UnknownNameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
// --TEST INSERT SINGLE NFFG

//----------------------------------------------------------------------------------------
				

				//------------------------------------------------------------------------
//				nc.loadAll();
				
				
//				try {
//					nc.loadAll();
//					int i=0;
//					Boolean risultato = true;
//					
//					while(risultato){
//							
//						System.out.println("Policy"+i);	
//							if((risultato=nc.testReachabilityPolicy("Policy"+i))){
//								System.out.println("Reachable");
//							}
//							else{
//								System.out.println("Not Reachable");
//							}
//							i++;
//						}
//				} catch (UnknownNameException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			
				
				
//				try {
//				
//					nc.loadReachabilityPolicy("PolicyMiky", "Nffg0", true, "WEBSERVER1", "MAILCLIENT1");
//					nc.unloadReachabilityPolicy("PolicyMiky");
//					nc.unloadReachabilityPolicy("PolicyMiky");
//					
//				} catch (UnknownNameException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				
		} catch (AlreadyLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testGetEmptySetNffgs(){
//		TEST GET EMPTY SET OF POLICIES
		it.polito.dp2.NFFG.sol3.client2.NffgVerifierFactory nvf = it.polito.dp2.NFFG.sol3.client2.NffgVerifierFactory.newInstance();
		NffgVerifierConcrete nv = null;
		try {
			nv = (NffgVerifierConcrete) nvf.newNffgVerifier();
		} catch (NffgVerifierException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			Set<NffgReader> set = nv.getNffgs();
			set.stream().forEach(n->{
				System.out.println(n.getName());
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
//TEST GET EMPTY SET OF POLICIES
	}
	
	public static void testPostGetSetNffgs(){
//		TEST GET EMPTY SET OF POLICIES
	
		
		NFFGClient nc = null;
		NffgVerifierConcrete nv = null;

		NFFGClientFactory ncf = (NFFGClientFactory) NFFGClientFactory.newInstance();
		try {
			nc = ncf.newNFFGClient();
			try {
				nc.loadAll();
			} catch (AlreadyLoadedException | ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NFFGClientException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {
			it.polito.dp2.NFFG.sol3.client2.NffgVerifierFactory nvf = it.polito.dp2.NFFG.sol3.client2.NffgVerifierFactory.newInstance();
			nv = (NffgVerifierConcrete) nvf.newNffgVerifier();
		} catch (NffgVerifierException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			Set<NffgReader> set = nv.getNffgs();
			set.stream().forEach(n->{
				System.out.println(n.getName());
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
//TEST GET EMPTY SET OF POLICIES
	}
	
	public static void testVerificaPolicyErrate(){
		NFFGClient nc = null;
		NffgVerifierConcrete nv = null;

		NFFGClientFactory ncf = (NFFGClientFactory) NFFGClientFactory.newInstance();
		try {
			nc = ncf.newNFFGClient();
			try {
				nc.loadAll();
				try {
					nc.testReachabilityPolicy("Policy0");
				} catch (UnknownNameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					nc.testReachabilityPolicy("PolicyWrong");
				} catch (UnknownNameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (AlreadyLoadedException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(ServiceException e) {
				e.printStackTrace();
			}
		} catch (NFFGClientException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {
			it.polito.dp2.NFFG.sol3.client2.NffgVerifierFactory nvf = it.polito.dp2.NFFG.sol3.client2.NffgVerifierFactory.newInstance();
			nv = (NffgVerifierConcrete) nvf.newNffgVerifier();
			
		} catch (NffgVerifierException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			Set<NffgReader> set = nv.getNffgs();
			set.stream().forEach(n->{
				System.out.println(n.getName());
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testGeneraErrori(){
		NFFGClient nc = null;
		NffgVerifierConcrete nv = null;

		NFFGClientFactory ncf = (NFFGClientFactory) NFFGClientFactory.newInstance();
		try {
			nc = ncf.newNFFGClient();
			try {
				((NFFGClientConcrete) nc).generaErrori();
				nc.loadAll();
			} catch (AlreadyLoadedException | ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NFFGClientException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {
			it.polito.dp2.NFFG.sol3.client2.NffgVerifierFactory nvf = it.polito.dp2.NFFG.sol3.client2.NffgVerifierFactory.newInstance();
			nv = (NffgVerifierConcrete) nvf.newNffgVerifier();
			
		} catch (NffgVerifierException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			Set<NffgReader> set = nv.getNffgs();
			set.stream().forEach(n->{
				System.out.println(n.getName());
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testVerificaPolicyOnTheGo(){
		Client client;
		client = ClientBuilder.newClient();
		String baseServiceUrl = "http://localhost:8081/NffgService/rest/";
		it.polito.dp2.NFFG.sol3.bindings.ObjectFactory of = new it.polito.dp2.NFFG.sol3.bindings.ObjectFactory();
		
		XPolicy policy = new XPolicy();
		policy.setNffg("Nffg3");
		policy.setName("OnTheFlyPippo");
		policy.setSrc("WEBCLIENT0");
		policy.setDst("MAILSERVER3");
		policy.setPositivity(true);
		
		
		client.target(baseServiceUrl+"policy")
		.request(MediaType.APPLICATION_XML)
		.accept(MediaType.APPLICATION_XML)
		.post(Entity.xml(of.createPolicy(policy)),XPolicy.class);
		
		client.target(baseServiceUrl+"policy/OnTheFlyPippo")
		.request(MediaType.APPLICATION_XML)
		
		.post(null,XPolicy.class);
		
		
	}
}
