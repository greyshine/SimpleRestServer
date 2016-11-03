package de.greyshine.restservices.handlers;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import de.greyshine.restservices.Constants;
import de.greyshine.restservices.IBinaryStorageService;
import de.greyshine.restservices.IBinaryStorageService.IBinary;
import de.greyshine.restservices.util.HtmlUtils;
import de.greyshine.restservices.util.Job;

@Path("/")
public class FileHandler extends AbstractHandler {

	@POST
	@Consumes
	@Path("/file")
	public Response upload() throws IOException {
		
		final IBinaryStorageService theBss = application.getBinaryStorageService();
		
		final IBinary theBinary = theBss.create( getRequestEntityStream() );
		
		if ( !theBinary.isExceptional() ) {
			
			return HtmlUtils.respond200Ok( Job.buildAsObject("id", theBinary.getId()) );	
		
		} else {
			
			return HtmlUtils.respond500ServerError();	
		}
	}
	
	@PUT
	@Consumes
	@Path("/file/{id:"
			+ Constants.REGEX_COLLECTION_ITEM_ID + "}")
	public Response put(@PathParam("collection") String inCollectionName, @PathParam("id") String inId) throws IOException {
		
		final IBinaryStorageService theBss = application.getBinaryStorageService();
		final IBinary theBinary = theBss.update(inId, getRequestEntityStream() );
		
		if ( !theBinary.isExceptional() ) {
			
			return HtmlUtils.respond200Ok( Job.buildAsObject("id", theBinary.getId()) );	
		
		} else {
			
			return HtmlUtils.respond500ServerError();	
		}
	}
	
	@GET
	@Path("/file/{id:"+ Constants.REGEX_COLLECTION_ITEM_ID + "}")
	public Response get(@PathParam("id") String inId) throws IOException {
		
		final IBinary theBinary = getBinaryStorageService().read( inId );
		
		if ( theBinary.isExceptional() ) {
			
			return HtmlUtils.respond500ServerError();
		}
		else if ( theBinary.isNotFound() ) {
			
			return HtmlUtils.respond404NotFound();
		}
		
		return Response//
				.ok()//
				.header( "Content-Length" , theBinary.getLength())//
				.entity( theBinary.getStream() )//
				.build();
	}
	
	@DELETE
	@Path("/file/{id:"
			+ Constants.REGEX_COLLECTION_ITEM_ID + "}")
	public Response delete(@PathParam("id") String inId) {
		
		IBinary theBinary = getBinaryStorageService().delete( inId );
		
		if ( theBinary.isExceptional() ) {
			
			return HtmlUtils.respond500ServerError();
		}
		else if ( !theBinary.isFound() ) {
			
			return HtmlUtils.respond404NotFound();
		}
		
		return HtmlUtils.respond200Ok();
	}

}
