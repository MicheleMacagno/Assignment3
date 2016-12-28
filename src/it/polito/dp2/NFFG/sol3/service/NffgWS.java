package it.polito.dp2.NFFG.sol3.service;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("nffg")
public class NffgWS {
	
	public NffgWS() { 
		
	}
	@GET 
	@Produces("text/plain") 
	public String getTime() { 
		return new String("Bravo che sei riuscito");
	} 
}
