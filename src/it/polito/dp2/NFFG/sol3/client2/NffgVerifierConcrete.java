package it.polito.dp2.NFFG.sol3.client2;

import java.util.Calendar;
import java.util.Set;

import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NffgVerifier;
import it.polito.dp2.NFFG.PolicyReader;

public class NffgVerifierConcrete implements NffgVerifier {

//TODO: modify 8081 to 8080
	private String baseServiceURL="http://localhost:8081/NffgService/rest/";
	
	

	@Override
	public NffgReader getNffg(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<NffgReader> getNffgs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PolicyReader> getPolicies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PolicyReader> getPolicies(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PolicyReader> getPolicies(Calendar arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getBaseServiceURL() {
		return baseServiceURL;
	}

	public void setBaseServiceURL(String baseServiceURL) {
		this.baseServiceURL = baseServiceURL;
	}
}
