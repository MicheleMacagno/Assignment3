package it.polito.dp2.NFFG.sol3.service;

import java.util.concurrent.*;

import it.polito.dp2.NFFG.sol3.bindings.*;

public class NffgDB {
	private static ConcurrentMap<String,XNffg> mapXNffg = new ConcurrentHashMap<String,XNffg>();
	private static ConcurrentMap<String,XPolicy> mapXPolicy = new ConcurrentHashMap<String,XPolicy>();

	public static ConcurrentMap<String,XNffg> getMapXNffg(){
		return mapXNffg;
	}
	
	public static ConcurrentMap<String,XPolicy> getMapXPolicy(){
		return mapXPolicy;
	}
	
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

//import java.util.HashMap;
//import java.util.Map;
//
//import it.polito.dp2.rest.negotiate.model.Negotiate;
//
//public class NegotiateDB {
//
//	// this is a database class containing a static Map of negotiate objects
//	private static Map<Long,Negotiate> map = new HashMap<Long, Negotiate>();
//	private static long last=0;
//
//	public static Map<Long, Negotiate> getMap() {
//		return map;
//	}
//
//	public static void setMap(Map<Long, Negotiate> map) {
//		NegotiateDB.map = map;
//	}
//	
//	public static long getNext() {
//		return ++last;
//	}
//
//}