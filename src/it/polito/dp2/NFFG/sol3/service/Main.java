package it.polito.dp2.NFFG.sol3.service;

import it.polito.dp2.NFFG.lab3.AlreadyLoadedException;
import it.polito.dp2.NFFG.lab3.NFFGClient;
import it.polito.dp2.NFFG.lab3.NFFGClientException;
import it.polito.dp2.NFFG.lab3.ServiceException;
import it.polito.dp2.NFFG.lab3.UnknownNameException;
import it.polito.dp2.NFFG.sol3.bindings.XNffg;
import it.polito.dp2.NFFG.sol3.client1.NFFGClientFactory;

public class Main {
	public static void main(String args[]){
		NFFGClient nc = null;
//		System.setProperty("it.polito.dp2.NFFG.NFFGClientFactory",
//				"it.polito.dp2.NFFG.sol3.client1.NFFGClientFactory");
		System.setProperty("it.polito.dp2.NFFG.lab3.URL", "http://localhost:8081/NffgService/rest");
		
		System.out.println("MAIN IS RUNNING!!!!!!!!!!!!!!!!!!!");
		NFFGClientFactory ncf = (NFFGClientFactory) NFFGClientFactory.newInstance();
		
		
		try {
			nc = ncf.newNFFGClient();
		} catch (NFFGClientException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		try {
//				try {
//					nc.loadNFFG("Nffg0");
//				} catch (UnknownNameException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
////				nc.loadAll();
				
				
				try {
					nc.loadAll();
					int i=0;
					Boolean risultato = true;
					
					while(risultato){
							
						System.out.println("Policy"+i);	
							if((risultato=nc.testReachabilityPolicy("Policy"+i))){
								System.out.println("Reachable");
							}
							else{
								System.out.println("Not Reachable");
							}
							i++;
						}
				} catch (UnknownNameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				
				
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
}
