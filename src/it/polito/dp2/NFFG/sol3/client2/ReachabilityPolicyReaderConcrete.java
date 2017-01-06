package it.polito.dp2.NFFG.sol3.client2;


import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NodeReader;
import it.polito.dp2.NFFG.ReachabilityPolicyReader;
import it.polito.dp2.NFFG.sol3.bindings.XPolicy;

public class ReachabilityPolicyReaderConcrete extends PolicyReaderConcrete implements ReachabilityPolicyReader {

	private NodeReader nrSrc=null;
	private NodeReader nrDst=null;
	
	public ReachabilityPolicyReaderConcrete(XPolicy policy, NffgReader nffg,NodeReader nrSrc,NodeReader nrDst) {
		
		super(policy, nffg);
		
		this.nrSrc = nrSrc;
		this.nrDst = nrDst;
	}

	@Override
	public NodeReader getDestinationNode() {
		return nrDst;
	}

	@Override
	public NodeReader getSourceNode() {
		return nrSrc;
	}

	

}
