package it.polito.dp2.NFFG.sol3.client2;
import it.polito.dp2.NFFG.NffgVerifier;
import it.polito.dp2.NFFG.NffgVerifierException;

public class NffgVerifierFactory extends it.polito.dp2.NFFG.NffgVerifierFactory {

	@Override
	public NffgVerifier newNffgVerifier() throws NffgVerifierException {
		try{
			NffgVerifierConcrete nvc = new NffgVerifierConcrete();
//TODO: uncomment it 8081 8080
//		if(System.getProperty("it.polito.dp2.NFFG.lab3.URL")!=null){
//			nvc.setBaseServiceURL(System.getProperty("it.polito.dp2.NFFG.lab3.URL"));
//		}
			
			return nvc;
		}catch(Throwable e){
			throw new NffgVerifierException("Error - Unexpected error occurred - Impossible to create the NffgVerifier");
		}
	}

}
