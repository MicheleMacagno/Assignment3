package it.polito.dp2.NFFG.sol3.client2;
import it.polito.dp2.NFFG.NffgVerifier;
import it.polito.dp2.NFFG.NffgVerifierException;

public class NffgVerifierFactory extends it.polito.dp2.NFFG.NffgVerifierFactory {

	@Override
	public NffgVerifier newNffgVerifier() throws NffgVerifierException {
		try{
//TODO: uncomment it 8081 8080
//		if(System.getProperty("it.polito.dp2.NFFG.lab3.URL")!=null){
//			NffgVerifierConcrete nvc = new NffgVerifierConcrete(System.getProperty("it.polito.dp2.NFFG.lab3.URL"));
//		}
		if(false){ return null;}
		else{
//TODO : modify 8081 to 8080
			NffgVerifierConcrete nvc = new NffgVerifierConcrete("http://localhost:8081/NffgService/rest/");
			return nvc;
		}
			
		}catch(Throwable e){
			throw new NffgVerifierException("Error - Unexpected error occurred - Impossible to create the NffgVerifier");
		}
	}

}
