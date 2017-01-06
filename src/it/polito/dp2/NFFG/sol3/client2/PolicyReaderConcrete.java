package it.polito.dp2.NFFG.sol3.client2;


import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.PolicyReader;
import it.polito.dp2.NFFG.VerificationResultReader;
import it.polito.dp2.NFFG.sol3.bindings.XPolicy;
import it.polito.dp2.NFFG.sol3.bindings.XVerification;

public class PolicyReaderConcrete extends NamedEntityReaderConcrete implements PolicyReader {

	private NffgReader nffgReader=null;
	private Boolean positive=false;
	private VerificationResultReader verificationResult=null;
	
	public PolicyReaderConcrete(XPolicy policy, NffgReader nffg){
		super(policy.getName());
		this.nffgReader=nffg;
		positive = policy.isPositivity();
		XVerification ver = policy.getVerification();
		
		if(ver!=null){
			this.verificationResult = new VerificationResultReaderConcrete(
					this,
					ver.getVerificationTime() == null ? null : ver.getVerificationTime().toGregorianCalendar(),
					ver.isResult(),
					ver.getMessage()
					);
		}
	}

	@Override
	public NffgReader getNffg() {
		return nffgReader;
	}

	@Override
	public VerificationResultReader getResult() {
		//it can return null if the verification data are not existing
		return verificationResult;
	}

	@Override
	public Boolean isPositive() {
		return positive;
	}

}
