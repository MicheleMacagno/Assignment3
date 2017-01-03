package it.polito.dp2.NFFG.sol3.service;

import it.polito.dp2.NFFG.sol3.bindings.XNffg;

public class Main {
	public static void main(String args[]){
		XNffg prova = new XNffg();
		prova.setName("pippo");
		NffgPolicyService.addXNffg(prova);
		System.out.println(NffgPolicyService.getXNffgByName("pippo").getName());
		
		
	
	}
}
