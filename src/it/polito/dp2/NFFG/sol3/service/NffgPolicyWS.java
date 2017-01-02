package it.polito.dp2.NFFG.sol3.service;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("nffg/{name: [a-zA-Z_][a-zA-Z0-9_]*}")
public class NffgIDWS {

	public NffgIDWS(){}
	
	@GET
	@Produces("application/xml")
	@Consumes("text/plain")
	public Response getNffgByName(@PathParam("name") String name){
		//returns a 404 if the name do not math the regexp
		
		return Response.status(200).entity(name).build() ;
	}
	
}
