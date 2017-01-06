package it.polito.dp2.NFFG.sol3.client2;

import java.util.LinkedHashSet;
import java.util.Set;

import it.polito.dp2.NFFG.FunctionalType;
import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NodeReader;
import it.polito.dp2.NFFG.TraversalPolicyReader;
import it.polito.dp2.NFFG.sol3.bindings.XPolicy;

public class TraversalPolicyReaderConcrete  extends ReachabilityPolicyReaderConcrete implements TraversalPolicyReader {

	private Set<FunctionalType> setFunctionalTypes=null;
	
	public TraversalPolicyReaderConcrete(XPolicy policy, NffgReader nffg, NodeReader nrSrc, NodeReader nrDst) {
		super(policy, nffg,nrSrc,nrDst);
		
		setFunctionalTypes = new LinkedHashSet<FunctionalType>();
		
		policy.getTraversal().getFunctionality().forEach(f->{
			setFunctionalTypes.add(FunctionalType.fromValue(f.name()));
		});
		
	
	}

	@Override
	public Set<FunctionalType> getTraversedFuctionalTypes() {
		return setFunctionalTypes;
	}



}
