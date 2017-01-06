package it.polito.dp2.NFFG.sol3.client2;

import it.polito.dp2.NFFG.NamedEntityReader;

public class NamedEntityReaderConcrete implements NamedEntityReader {

	private String name=null;
		
	public NamedEntityReaderConcrete(String name){
		this.name=name;
	}
	
	@Override
	public String getName() {
		return name;
	}

}
