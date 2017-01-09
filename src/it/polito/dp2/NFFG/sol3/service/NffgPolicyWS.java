package it.polito.dp2.NFFG.sol3.service;
import java.net.URI;

import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.wordnik.swagger.annotations.ApiOperation;

import it.polito.dp2.NFFG.sol3.bindings.XNffg;
import it.polito.dp2.NFFG.sol3.bindings.XNffgs;
import it.polito.dp2.NFFG.sol3.bindings.XPolicies;
import it.polito.dp2.NFFG.sol3.bindings.XPolicy;


@Path("/")
@Singleton
public class NffgPolicyWS {
	//TODO: fix it for concurrency
	
	it.polito.dp2.NFFG.sol3.bindings.ObjectFactory of = new it.polito.dp2.NFFG.sol3.bindings.ObjectFactory();
	NffgPolicyService nps = new NffgPolicyService();
	
	public NffgPolicyWS(){
		
	}
	
	/**
	 * 
	 * @param xnffg
	 * @return Response type containing all the information about the XNffg sent.
	 */
	@POST
	@Path("nffg")
	@ApiOperation(	value = "Create a new Nffg", notes = "xml format required")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Response storeNewNffgByName(@Context UriInfo uriInfo, XNffg xnffg) throws ForbiddenException, InternalServerErrorException {
		
		XNffg rxnffg = NffgPolicyService.addXNffg(xnffg);
		URI uri = uriInfo.getAbsolutePathBuilder().path(rxnffg.getName()).build();
		return Response.created(uri).entity(of.createNffg(rxnffg)).build();
  	}
	
	@GET
	@Path("/nffg/{name: [a-zA-Z_][a-zA-Z0-9_]*}")
	@ApiOperation( value = "Retrieve an Nffg given its name", notes="returns xml format")
	@Produces(MediaType.APPLICATION_XML)
	public Response getNffgByName(@PathParam("name") String name) throws NotFoundException{
		
		XNffg rxnffg = NffgPolicyService.getXNffgByName(name);
		return Response.status(200).entity(of.createNffg(rxnffg)).build();
	}
	
	@POST
	@Path("nffgs")
	@ApiOperation(	value = "Create new nffgs", notes = "xml format required")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Response storeNffgsByName(XNffgs xnffgs) throws ForbiddenException {
		
		XNffgs rxnffgs = NffgPolicyService.addXNffgs(xnffgs);
		return Response.status(201).entity(of.createNffgs(rxnffgs)).build();
  	}
	
	@GET
	@Path("/nffgs")
	@Produces(MediaType.APPLICATION_XML)
	public Response getNffgs(){
		
		XNffgs rxnffgs = NffgPolicyService.getXNffgs();
		return Response.status(200).entity(of.createNffgs(rxnffgs)).build();
	}
	
	@POST
	@Path("policy")
	@ApiOperation(	value = "Create a new Policy", notes = "xml format required")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Response storePolicyByName(@DefaultValue("y") @QueryParam("overwrite") String overwrite,
										@Context UriInfo uriInfo,XPolicy xpolicy) throws BadRequestException, ForbiddenException{
		XPolicy rxpolicy=null;
		if(overwrite.equals("y")){
			rxpolicy = NffgPolicyService.addXPolicyVerifyXNffg(xpolicy,true);
		}
		else if(overwrite.equals("n")){
			rxpolicy = NffgPolicyService.addXPolicyVerifyXNffg(xpolicy,false);
		}else{
			throw new ForbiddenException("Parameter overwrite can be y or n, or can be avoided. No other values are accepted.",
					Response.status(403).entity("Parameter overwrite can be y or n, or can be avoided. No other values are accepted.").build());
		}
		URI uri = uriInfo.getAbsolutePathBuilder().path(rxpolicy.getName()).build();
		return Response.created(uri).entity(of.createPolicy(rxpolicy)).build();
	}
		
	@GET
	@Path("policy/{name: [a-zA-Z_][a-zA-Z0-9_]*}")
	@ApiOperation(	value = "Read an existing policy", notes = "xml format required")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPolicyByName(@PathParam("name") String name) throws NotFoundException{
		XPolicy rxpolicy = NffgPolicyService.getXPolicyByName(name);
		return Response.status(200).entity(of.createPolicy(rxpolicy)).build();
		
	}
	
	@POST
	@Path("policy/{name: [a-zA-Z_][a-zA-Z0-9_]*}")
	@ApiOperation(	value = "Verify an existing policy", notes = "xml format required")
	@Produces(MediaType.APPLICATION_XML)
	public Response verifyPolicyByName(
			@PathParam("name") String name) throws NotFoundException,InternalServerErrorException{
		
		XPolicy rxpolicy = NffgPolicyService.verifyPolicy(name); 
		return Response.status(200).entity(of.createPolicy(rxpolicy)).build();
	}
	
//TODO: verify the NOT ALLOWED exception error 405
	@POST
	@Path("policies")
	@ApiOperation(	value = "Create new set of policies", notes = "xml format required")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Response storePolicies(
								XPolicies xpolicies,
								@DefaultValue("y") @QueryParam("overwrite") String overwrite) throws ForbiddenException,NotFoundException,Exception {
		XPolicies rxpolicies=null;
		if(overwrite.equals("y")){
			System.out.println("Overwriting Policies");
			rxpolicies = NffgPolicyService.addXPolicies(xpolicies,true);
		}
		else if(overwrite.equals("n")){
			System.out.println("Not Overwriting Policies");
			rxpolicies = NffgPolicyService.addXPolicies(xpolicies,false);
		}
		else{
			throw new NotAllowedException("The parameter overwrite can be only y or n",
					Response.status(405).entity("The parameter overwrite can be only y or n").build());
		}
			
		return Response.status(201).entity(of.createPolicies(rxpolicies)).build();
		
  	}
	
	@GET
	@Path("policies")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPolicies()/* throws NotFoundException*/{
		
		XPolicies rxpolicies = NffgPolicyService.getXPolicies();
		return Response.status(200).entity(of.createPolicies(rxpolicies)).build();
	}
	
	@DELETE
	@Path("policies")
	public Response deletePolicies(){
		NffgPolicyService.deleteAllPolicies();
		return Response.status(204).build();
	}
	
	@DELETE
	@Path("/policy/{name: [a-zA-Z_][a-zA-Z0-9_]*}")
	@Produces(MediaType.APPLICATION_XML)
	public Response deletePolicyByName(@PathParam("name") String name) throws NotFoundException,Exception{
		XPolicy rxpolicy = NffgPolicyService.deletePolicyByName(name);
		return Response.status(200).entity(of.createPolicy(rxpolicy)).build();
	}
	
	@PUT
	@Path("policy/{name: [a-zA-Z_][a-zA-Z0-9_]*}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response updatePolicyByName(@PathParam("name") String name,XPolicy xpolicy) throws NotFoundException,ForbiddenException{
		XPolicy rxpolicy = NffgPolicyService.updatePolicyByName(name, xpolicy);
		return Response.status(200).entity(of.createPolicy(rxpolicy)).build();
	}
	
//TODO remove
	@POST
	@Path("errore")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response produceErrore(XPolicy xpolicy){
		throw new InternalServerErrorException("prova",Response.status(500).entity("prova di errore").build());
	}
	
	
	
}
