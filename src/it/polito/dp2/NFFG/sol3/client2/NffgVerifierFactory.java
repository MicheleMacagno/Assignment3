package it.polito.dp2.NFFG.sol3.client2;
import it.polito.dp2.NFFG.NffgVerifier;
import it.polito.dp2.NFFG.NffgVerifierException;

public class NffgVerifierFactory extends it.polito.dp2.NFFG.NffgVerifierFactory {

	@Override
	public NffgVerifier newNffgVerifier() throws NffgVerifierException {
		NffgVerifierConcrete nvc;
		String url;
		try{
			if(System.getProperty("it.polito.dp2.NFFG.lab3.URL")!=null){
				url = System.getProperty("it.polito.dp2.NFFG.lab3.URL");
			}
			else{
				url="http://localhost:8080/NffgService/rest/";
			}

			//make the last symbol of the url a /
			if(url.lastIndexOf("/") != url.length()-1){
				url=url+"/";
			}
			nvc = new NffgVerifierConcrete(url);
			return nvc;
			
		}catch(Exception e){
			throw new NffgVerifierException("Error - Unexpected error occurred - Impossible to create the NffgVerifier");
		}catch(Throwable e){
			throw new NffgVerifierException("Error - Unexpected error occurred - Impossible to create the NffgVerifier");
		}
	}
	

}
