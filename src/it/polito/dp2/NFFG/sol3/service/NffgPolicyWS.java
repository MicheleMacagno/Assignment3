package it.polito.dp2.NFFG.sol3.service;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.inject.Singleton;
import javax.jws.WebService;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import it.polito.dp2.NFFG.sol3.bindings.XPolicy;


@Path("/")
@Singleton
public class NffgPolicyWS {
	//TODO: fix it for concurrency
	
	it.polito.dp2.NFFG.sol3.bindings.ObjectFactory of = new it.polito.dp2.NFFG.sol3.bindings.ObjectFactory();
	NffgPolicyService nps = new NffgPolicyService();
	
	public NffgPolicyWS(){
		
	}
	
	
	
//	@GET
//	@Path("nffg/{name: [a-zA-Z_][a-zA-Z0-9_]*}")
//	@Produces(MediaType.APPLICATION_XML)
//	public Response getNffgByName(@PathParam("name") String name){
//		//returns a 404 if the name do not math the regexp
//		XNffg xnffg = NffgPolicyService.getXNffgByName(name);
//		if(xnffg==null){
//			return Response.status(404).entity(new String("The nffg named as " + name + " is not existing!!")).build() ;
//			//throw new WebServiceException("Error 404 - The nffg named as " + name + " is not existing!!"); 
//		}
//		return Response.status(200).entity(xnffg.getName()).build() ;
//	}
	
//	@GET
//	@Path("nffg/{name: [a-zA-Z_][a-zA-Z0-9_]*}")
//	@Produces(MediaType.APPLICATION_XML)
//	public JAXBElement<XNffg> getNffgByName(@PathParam("name") String name){
//		//returns a 404 if the name do not math the regexp
//		XNffg xnffg = NffgPolicyService.getXNffgByName(name);
//		if(xnffg==null){
//			//return Response.status(404).entity(new String("The nffg named as " + name + " is not existing!!")).build() ;
//			return null;
//		}
//		it.polito.dp2.NFFG.sol3.bindings.ObjectFactory of = new it.polito.dp2.NFFG.sol3.bindings.ObjectFactory();
//		of.createNffg(xnffg);
//		return of.createNffg(xnffg);
//	}
//	
	
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
		
//	
//	@POST
//	@Path("policy")
//	@ApiOperation(	value = "Create a new Policy", notes = "xml format required")
//	@Produces(MediaType.APPLICATION_XML)
//	@Consumes(MediaType.APPLICATION_XML)
//	public Response storePolicyByName(@PathParam("name") String name, String xmlDocument){
//		NffgPolicyService nps = new NffgPolicyService();
////		Response policyResponse = nps.unmarshalPolicy(xmlDocument);
//		return policyResponse;
//	}
//	
//	@POST
//	@Path("policies")
//	@ApiOperation(	value = "Create a new set of policies", notes = "xml format required")
//	@Produces(MediaType.APPLICATION_XML)
//	@Consumes(MediaType.APPLICATION_XML)
//	public Response storePolicies(String xmlDocument){
//		
//		NffgPolicyService nps = new NffgPolicyService();
////		Response policiesResponse = nps.unmarshalPolicies(xmlDocument);
//		return policiesResponse;
//	}
//	
	
	
}
