package it.polito.dp2.NFFG.sol3.service;

import java.util.concurrent.ConcurrentHashMap;

import it.polito.dp2.NFFG.sol3.bindings.XNffg;
import it.polito.dp2.NFFG.sol3.bindings.XPolicy;

public class NffgPolicyService {
	static ConcurrentHashMap<String,XNffg> mapXNffg = NffgDB.getMapXNffg();
	static ConcurrentHashMap<String,XPolicy> mapXPolicy = NffgDB.getMapXPolicy();
	
	public static XNffg getXNffgByName(String name){
		if(mapXNffg.containsKey(name)){
			return mapXNffg.get(name);
		}
		else{
			return null;
		}
	}
	
	public static Boolean addXNffg(XNffg nffg){
		try{
			if(mapXNffg.putIfAbsent(nffg.getName(), nffg) == null){
				//able to insert the element in the map
				System.out.println("Nffg corretly inserted in the map");
				return true;
			}
			else{
				System.out.println("--error -- Nffg NOT INSERTED in the map");
				return false;
			}
		}catch(Exception e){
			//the put can throw many exceptions
			System.out.println("--error -- Nffg NOT INSERTED in the map");
			return false;
		}
	}
	
	public static XPolicy getXPolicyByName(String name){
		if(mapXPolicy.containsKey(name)){
			return mapXPolicy.get(name);
		}
		else{
			return null;
		}
	}
	
	public static Boolean addXPolicy(XPolicy policy){
		try{
			if(mapXPolicy.putIfAbsent(policy.getName(), policy) == null){
				//able to insert the element in the map
				System.out.println("Policy corretly inserted in the map");
				return true;
			}
			else{
				System.out.println("--error -- Policy NOT INSERTED in the map");
				return false;
			}
		}catch(Exception e){
			//the put can throw many exceptions
			System.out.println("--error -- Policy NOT INSERTED in the map");
			return false;
		}
	}
}
