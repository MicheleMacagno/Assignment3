package it.polito.dp2.NFFG.sol3.service;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.inject.Singleton;
import javax.jws.WebService;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.WebServiceException;

import com.wordnik.swagger.annotations.ApiOperation;

import it.polito.dp2.NFFG.lab3.AlreadyLoadedException;
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
	
//my working version with xml translation
	
//	@POST
//	@Path("nffg")
//	@ApiOperation(	value = "Create a new Nffg", notes = "xml format required")
//	@Produces(MediaType.APPLICATION_XML)
//	@Consumes(MediaType.APPLICATION_XML)
//	public Response storeNewNffgByName(@PathParam("name") String name, String xmlDocument){
//		NffgPolicyService nps = new NffgPolicyService();
//		Response nffgResponse = nps.unmarshalNffg(xmlDocument);
//		return nffgResponse;
//	}
	
	
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
	public Response storeNewNffgByName(@Context UriInfo uriInfo, XNffg xnffg) throws ForbiddenException {
		
		XNffg rxnffg = NffgPolicyService.addXNffg(xnffg);
		if(rxnffg != null){
			URI uri = uriInfo.getAbsolutePathBuilder().path(rxnffg.getName()).build();
			return Response.created(uri).entity(of.createNffg(rxnffg)).build();
		}
		else{
			return Response.status(404).entity("Impossible to create the object. Verify the name of nffg is not already existing").build();
		}
  	}
	
	@GET
	@Path("/nffg/{name: [a-zA-Z_][a-zA-Z0-9_]*}")
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
		if(rxnffgs != null){
			return Response.status(200).entity(of.createNffgs(xnffgs)).build();
		}
		else{
			return Response.status(404).entity("Impossible to create the object. Verify the name of nffg is not already existing").build();
		}
  	}
	
	@GET
	@Path("/nffgs")
	@Produces(MediaType.APPLICATION_XML)
	public Response getNffgs(){
		
		XNffgs rxnffgs = NffgPolicyService.getXNffgs();
		return Response.status(200).entity(of.createNffgs(rxnffgs)).build();
	}
	
//NOT TO BE IMPLEMENTED	
//	@DELETE
//	@Path("/nffgs")
//	public Response deleteAllNffgs(){
//		NffgPolicyService.deleteAllXNffgs();
//		return Response.status(204).build();
//	}
	
//NOT TO BE IMPLEMENTED	
//	@DELETE
//	@Path("/nffg/{name: [a-zA-Z_][a-zA-Z0-9_]*}")
//	@Produces(MediaType.APPLICATION_XML)
//	public Response deleteNffgByName(
//			@DefaultValue("y") @QueryParam("delpolicy") String delpolicy
//			,@PathParam("name") String name) throws NotFoundException{
//		
//		if(!(delpolicy.equals("y") || delpolicy.equals("n"))){
//			throw new ForbiddenException("You must specify either y/n for delpolicy parameter",
//					Response.status(403).entity("You must specify either y/n for delpolicy parameter").build());
//		}
//		XNffg rxnffg = NffgPolicyService.deleteNffgByName(name,delpolicy);
//		return Response.status(200).entity(of.createNffg(rxnffg)).build();
//	}
	
	
	@POST
	@Path("policy")
	@ApiOperation(	value = "Create a new Policy", notes = "xml format required")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Response storePolicyByName(@Context UriInfo uriInfo,XPolicy xpolicy) throws BadRequestException, ForbiddenException{
		XPolicy rxpolicy = NffgPolicyService.addXPolicyVerifyXNffg(xpolicy);
		
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
			@PathParam("name") String name) throws NotFoundException{
		
		XPolicy rxpolicy = NffgPolicyService.verifyPolicy(name); 
		return Response.status(200).entity(of.createPolicy(rxpolicy)).build();
	}
	
	
	@POST
	@Path("policies")
	@ApiOperation(	value = "Create new set of policies", notes = "xml format required")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Response storePolicies(XPolicies xpolicies) throws ForbiddenException {
		
		XPolicies rxpolicies = NffgPolicyService.addXPolicies(xpolicies);
		if(xpolicies != null){
			return Response.status(200).entity(of.createPolicies(rxpolicies)).build();
		}
		else{
			return Response.status(404).entity("Impossible to create the object. Verify the name of nffg is not already existing").build();
		}
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
	public Response deletePolicyByName(@PathParam("name") String name) throws NotFoundException{
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
	
//	@POST
//	@Path("policies/verification")
//	@Consumes(MediaType.APPLICATION_XML)
//	@Produces(MediaType.APPLICATION_XML)
//	public Response verifyPolicyByName(XVerificationRequest xverificationrequest) throws NotFoundException{
//		XPolicies verified = NffgPolicyService.verifyPolicies(xverificationrequest);
//		return Response.status(200).entity(verified).build();
//	}
//	
	
	
	
}
