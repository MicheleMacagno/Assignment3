package it.polito.dp2.NFFG.sol3.client2;

import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import it.polito.dp2.NFFG.NffgReader;
import it.polito.dp2.NFFG.NodeReader;
import it.polito.dp2.NFFG.sol3.bindings.XLink;
import it.polito.dp2.NFFG.sol3.bindings.XNffg;
import it.polito.dp2.NFFG.sol3.bindings.XNode;
import it.polito.dp2.NFFG.sol3.bindings.XNodes;

public class NffgReaderConcrete extends NamedEntityReaderConcrete implements NffgReader {

	private Calendar updateTime = null;
	private Set<NodeReader> setNodesReaderConcrete = null;
	
	public NffgReaderConcrete(XNffg nffg){
		
		super(nffg.getName());
		
		updateTime = nffg.getLastUpdate().toGregorianCalendar();
		XNodes listxnodes = nffg.getNodes();
		//create the NodeReaderConcrete for each node and add to the set
		setNodesReaderConcrete = new LinkedHashSet<NodeReader>();
		for(XNode xn : listxnodes.getNode()){
			NodeReader nr = new NodeReaderConcrete(xn);
			setNodesReaderConcrete.add(nr);
		}
		
		//for each node create the correspondant link
		//filter the links having src or dst equal to the current node and save it to a list
     	//extract the correct node reader element and add the available links
		for(XNode xn : listxnodes.getNode()){
			List<XLink> lll = 
					//filter links having as source node the current node n
					nffg.getLinks().getLink().stream().filter(l->{
						return(l.getSrc().equals(xn.getName()));
					}).collect(Collectors.toList());
			
			//add the created link to the correspondant node reader they refer to
			for(NodeReader nr : setNodesReaderConcrete){
				if(nr.getName().equals(xn.getName())){
					((NodeReaderConcrete) nr).addLinks(setNodesReaderConcrete, lll, xn);
				}
			}
		}
	}
	

	@Override
	public NodeReader getNode(String name) {
		for(NodeReader nrc : setNodesReaderConcrete){
			if(nrc.getName().equals(name)){
				return nrc;
			}
		}
		return null;
	}

	@Override
	public Set<NodeReader> getNodes() {
		return setNodesReaderConcrete;
	}
	

	@Override
	public Calendar getUpdateTime() {
		return updateTime;
	}

}
