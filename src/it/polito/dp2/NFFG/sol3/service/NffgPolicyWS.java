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
import javax.xml.bind.JAXBElement;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import it.polito.dp2.NFFG.sol3.bindings.XNffg;
import it.polito.dp2.NFFG.sol3.bindings.XNffgs;
import it.polito.dp2.NFFG.sol3.bindings.XPolicies;
import it.polito.dp2.NFFG.sol3.bindings.XPolicy;


@Path("/")
public class NffgPolicyWS {
	
	it.polito.dp2.NFFG.sol3.bindings.ObjectFactory of = new it.polito.dp2.NFFG.sol3.bindings.ObjectFactory();
	NffgPolicyService nps = new NffgPolicyService();
	
	public NffgPolicyWS(){
		
	}
	
	
	@POST
	@Path("nffg")
	@ApiOperation(	value = "Create a new Nffg", notes = "xml format required")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message= "Created"),
			@ApiResponse(code = 400, message= "The body does not respect the XML schema"),
			@ApiResponse(code = 403, message= "Nffg already existing"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Response storeNewNffgByName(@Context UriInfo uriInfo, JAXBElement<XNffg> xnffg) throws ForbiddenException, InternalServerErrorException {
		
		
		XNffg rxnffg = nps.addXNffg(xnffg.getValue(),uriInfo);
		URI uri = uriInfo.getAbsolutePathBuilder().path(rxnffg.getName()).build();
		return Response.created(uri).entity(of.createNffg(rxnffg)).build();
  	}
	
	@GET
	@Path("nffgs/{name: [a-zA-Z][a-zA-Z0-9]*}")
	@ApiOperation( value = "Retrieve an Nffg given its name", notes="single nffg")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message= "Nffg not found"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	@Produces(MediaType.APPLICATION_XML)
	public Response getNffgByName(@PathParam("name") String name) throws NotFoundException{
		
		XNffg rxnffg = nps.getXNffgByName(name);
		return Response.status(200).entity(of.createNffg(rxnffg)).build();
	}
	
	@POST
	@Path("nffgs")
	@ApiOperation(	value = "Create new nffgs", notes = "xml format required")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message= "Created"),
			@ApiResponse(code = 400, message= "The body does not respect the XML schema"),
			@ApiResponse(code = 403, message= "At least a Nffg already existing"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Response storeNffgsByName(@Context UriInfo uriInfo,JAXBElement<XNffgs> xnffgs) throws ForbiddenException {
		
		XNffgs rxnffgs = nps.addXNffgs(xnffgs.getValue(),uriInfo);
		return Response.status(201).entity(of.createNffgs(rxnffgs)).build();
  	}
	
	@GET
	@Path("nffgs")
	@ApiOperation(	value = "Retrieve all nffgs", notes = "get all available nffgs")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	@Produces(MediaType.APPLICATION_XML)
	public Response getNffgs(){
		
		XNffgs rxnffgs = nps.getXNffgs();
		return Response.status(200).entity(of.createNffgs(rxnffgs)).build();
	}
	
		
	@GET
	@Path("policies/{name: [a-zA-Z][a-zA-Z0-9]*}")
	@ApiOperation(	value = "Read an existing policy", notes = "get a single policy")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message= "Policy Not Found"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	@Produces(MediaType.APPLICATION_XML)
	public Response getPolicyByName(@PathParam("name") String name) throws NotFoundException{
		XPolicy rxpolicy = nps.getXPolicyByName(name);
		return Response.status(200).entity(of.createPolicy(rxpolicy)).build();
		
	}
	
	@POST
	@Path("policies/{name: [a-zA-Z][a-zA-Z0-9]*}")
	@ApiOperation(	value = "Verify an existing policy", notes = "Empty Body")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message= "Policy Not Found"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	@Produces(MediaType.APPLICATION_XML)
	public Response verifyPolicyByName(
			@PathParam("name") String name) throws NotFoundException,InternalServerErrorException{
		
		XPolicy rxpolicy = nps.verifyPolicy(name); 
		return Response.status(200).entity(of.createPolicy(rxpolicy)).build();
	}
	
	
	@GET
	@Path("policies")
	@ApiOperation(	value = "Retrieve all policies", notes = "Get all available policies")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	@Produces(MediaType.APPLICATION_XML)
	public Response getPolicies(){
		
		XPolicies rxpolicies = nps.getXPolicies();
		return Response.status(200).entity(of.createPolicies(rxpolicies)).build();
	}
	
	@DELETE
	@Path("policies")
	@ApiOperation(	value = "Remove all policies", notes = "DELETE all available policies")
	@ApiResponses(value = {
			@ApiResponse(code = 204, message= "All policies removed"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	public Response deletePolicies(){
		nps.deleteAllPolicies();
		return Response.status(204).build();
	}
	
	@DELETE
	@Path("policies/{name: [a-zA-Z][a-zA-Z0-9]*}")
	@ApiOperation(	value = "Remove Policy given its name", notes = "xml format policy returned")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message= "OK Removed"),
			@ApiResponse(code = 404, message= "Policy not found"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	@Produces(MediaType.APPLICATION_XML)
	public Response deletePolicyByName(@PathParam("name") String name) throws NotFoundException,Exception{
		XPolicy rxpolicy = nps.deletePolicyByName(name);
		return Response.status(200).entity(of.createPolicy(rxpolicy)).build();
	}
	
	
	@PUT
	@Path("policies/{name: [a-zA-Z][a-zA-Z0-9]*}")
	@ApiOperation(	value = "Create or update policy", notes = "xml format required")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message= "Updated"),
			@ApiResponse(code = 201, message= "Created"),
			@ApiResponse(code = 403, message= "Nffg or Node the policy refers to not existing"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response createOrUpdatePolicyByName(	@Context UriInfo uriInfo,
										@PathParam("name") String name,
										JAXBElement<XPolicy> jxpolicy) throws NotFoundException,ForbiddenException{
		XPolicy xpolicy = jxpolicy.getValue();
		xpolicy.setName(name);
		return(nps.createOrUpdatePolicyVerifyNffg(xpolicy, uriInfo));
	}
	
//TODO remove
	@POST
	@Path("errore")
	@ApiOperation(	value = "TODO- REMOVE", notes = "xml format required")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message= "Updated"),
			@ApiResponse(code = 403, message= "Nffg of updated policy Not Existing"),
			@ApiResponse(code = 404, message= "Policy not found"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response produceErrore(XPolicy xpolicy){
		throw new InternalServerErrorException("prova",Response.status(500).entity("prova di errore").build());
	}
	
	
	
}
