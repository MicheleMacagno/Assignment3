package it.polito.dp2.NFFG.sol3.client1;

import it.polito.dp2.NFFG.lab3.NFFGClient;
import it.polito.dp2.NFFG.lab3.NFFGClientException;

public class NFFGClientFactory extends it.polito.dp2.NFFG.lab3.NFFGClientFactory {

	@Override
	public NFFGClient newNFFGClient() throws NFFGClientException {
		NFFGClient concreteClient = null;
		try{
			concreteClient = new NFFGClientConcrete();
			if(concreteClient==null){
				throw new NFFGClientException("Impossible to instantiate the NFFGClient!");
			}	
		}catch(Exception e){
			e.printStackTrace();
			throw new NFFGClientException("Impossible to instantiate the NFFGClient!");
		}
		return concreteClient;
	}

}
