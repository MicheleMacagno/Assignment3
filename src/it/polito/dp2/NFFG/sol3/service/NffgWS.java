package it.polito.dp2.NFFG.sol3.service;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.ApiOperation;

@Path("nffgsasd")
public class NffgWS {
	
	public static String prova=null;
	
	public NffgWS() { 
		
	}
	
	/*
	 * I can have more than one GET for the same resource
	 * Here I use APPLICATION_XML
	 * so specifing Accept = application/xml in the GET header this method is called
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML) 
	public Response getNffg() { 
		return Response.status(200).entity(prova).build() ;
	}
	
	/*
	 * I can have more than one GET for the same resource
	 * Here I used text/plain
	 * so specifing Accept = text/plain in the GET header this method is called
	 */
	@GET
	@Produces("text/plain") 
	public Response getNffgInTextPlain() { 
		return Response.status(200).entity(new String("ho ricevuto una stringa")).build() ;
	}
	
	@POST
	@ApiOperation(	value = "Create a new Nffg", notes = "xml format required"
			)
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response createNffg(String myxml){
		String result = "prova di creazione\n"+ myxml;
		prova = "returned" +myxml;
		return Response.status(201).entity(result).build() ;
	}
}
