package it.polito.dp2.NFFG.sol3.service;
import java.net.URI;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

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
	
	/*
	 * The documentation of methods of this class can be found on the PDF or at index.html of the documentation
	 */
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
	@ApiOperation(	value = "Create new set of nffgs", notes = "xml format required")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message= "Created"),
			@ApiResponse(code = 400, message= "The body does not respect the XML schema"),
			@ApiResponse(code = 403, message= "At least a Nffg already existing"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Response storeNffgs(@Context UriInfo uriInfo,JAXBElement<XNffgs> xnffgs) throws ForbiddenException,InternalServerErrorException {
		
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
	public Response getNffgs() throws InternalServerErrorException{
		
		XNffgs rxnffgs = nps.getXNffgs();
		return Response.status(200).entity(of.createNffgs(rxnffgs)).build();
	}
	
		
	@PUT
	@Path("policies/{name}")
	@ApiOperation(	value = "Create or update policy", notes = "xml format required")
	@ApiResponses(value = {
			
			@ApiResponse(code = 200, message= "Updated"),
			@ApiResponse(code = 201, message= "Created"),
			@ApiResponse(code = 400, message= "The body does not respect the XML schema"),
			@ApiResponse(code = 403, message= "Nffg or Node the policy refers to not existing"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response createOrUpdatePolicyByName(
										JAXBElement<XPolicy> jxpolicy,
										@PathParam("name") String name,
										@Context UriInfo uriInfo) throws ForbiddenException,InternalServerErrorException,BadRequestException{
		XPolicy xpolicy = jxpolicy.getValue();
		if(!xpolicy.getName().equals(name)){
			throw new BadRequestException("Error - 400 The name of the policy and the url name are different",
					Response.status(400).entity("Error - 400 The name of the policy and the url name are different").type(MediaType.TEXT_PLAIN).build());
		}
		
		xpolicy.setName(name);
		return(nps.createOrUpdatePolicyVerifyNffg(xpolicy, uriInfo));
	}


	@GET
	@Path("policies/{name}")
	@ApiOperation(	value = "Read an existing policy", notes = "get a single policy")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message= "Policy Not Found"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	@Produces(MediaType.APPLICATION_XML)
	public Response getPolicyByName(@PathParam("name") String name) throws NotFoundException,InternalServerErrorException{
		XPolicy rxpolicy = nps.getXPolicyByName(name);
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
	public Response getPolicies() throws InternalServerErrorException{
		
		XPolicies rxpolicies = nps.getXPolicies();
		return Response.status(200).entity(of.createPolicies(rxpolicies)).build();
	}
	
	
	@POST
	@Path("policies/{name}")
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
	
	

	
	@DELETE
	@Path("policies/{name}")
	@ApiOperation(	value = "Remove Policy given its name", notes = "xml format policy returned")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message= "OK Removed"),
			@ApiResponse(code = 404, message= "Policy not found"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	@Produces(MediaType.APPLICATION_XML)
	public Response deletePolicyByName(@PathParam("name") String name) throws NotFoundException,InternalServerErrorException{
		XPolicy rxpolicy = nps.deletePolicyByName(name);
		return Response.status(200).entity(of.createPolicy(rxpolicy)).build();
	}


	@DELETE
	@Path("policies")
	@ApiOperation(	value = "Remove all policies", notes = "DELETE all available policies")
	@ApiResponses(value = {
			@ApiResponse(code = 204, message= "All policies removed"),
			@ApiResponse(code = 500, message= "Internal server error")
	})
	public Response deletePolicies() throws InternalServerErrorException{
		nps.deleteAllPolicies();
		return Response.status(204).build();
	}
	

	
}
