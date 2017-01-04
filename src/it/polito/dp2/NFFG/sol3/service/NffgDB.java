package it.polito.dp2.NFFG.sol3.service;

import java.util.LinkedHashMap;
import java.util.concurrent.*;

import it.polito.dp2.NFFG.sol3.bindings.*;

public class NffgDB {
	private static ConcurrentHashMap<String,XNffg> mapXNffg = new ConcurrentHashMap<String,XNffg>();
	private static ConcurrentHashMap<String,XPolicy> mapXPolicy = new ConcurrentHashMap<String,XPolicy>();
//	private static ConcurrentHashMap<String,Node> mapNameNodesNeo  = new ConcurrentHashMap<String,Node>();
	//stores the mapping between the Name of the Node and the relative ID
	private static ConcurrentHashMap<String,Integer> mapNameNodesNeo  = new ConcurrentHashMap<String,Integer>();
	
	public static ConcurrentHashMap<String,XNffg> getMapXNffg(){
		return mapXNffg;
	}
	
	public static ConcurrentHashMap<String,XPolicy> getMapXPolicy(){
		return mapXPolicy;
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