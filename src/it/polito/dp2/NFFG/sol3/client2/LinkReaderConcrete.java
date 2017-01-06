package it.polito.dp2.NFFG.sol3.client2;

import it.polito.dp2.NFFG.LinkReader;
import it.polito.dp2.NFFG.NodeReader;

public class LinkReaderConcrete extends NamedEntityReaderConcrete implements LinkReader {

	private NodeReader dstNode=null;
	private NodeReader srcNode=null;
	
	public LinkReaderConcrete(String name,NodeReader src,NodeReader dst){
		super(name);
		this.srcNode=src;
		this.dstNode=dst;
	}
	
	
	@Override
	public NodeReader getDestinationNode() {
		return dstNode;
	}

	@Override
	public NodeReader getSourceNode() {
		return srcNode;
	}

}
