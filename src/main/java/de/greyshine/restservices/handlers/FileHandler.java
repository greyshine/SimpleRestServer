package de.greyshine.restservices.handlers;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.google.gson.JsonArray;

import de.greyshine.restservices.Constants;
import de.greyshine.restservices.IBinaryStorageService;
import de.greyshine.restservices.IBinaryStorageService.IBinary;
import de.greyshine.restservices.util.HtmlUtils;
import de.greyshine.restservices.util.Job;
import de.greyshine.restservices.util.Utils;

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
	
	/**
	 * Simply lists the files¸¸
	 */
	@GET
	@Path("/file")
	public Response list() {
		
		final IBinaryStorageService theBss = application.getBinaryStorageService();
		List<IBinary> theBinaries = theBss.list(null,null);
		
		final JsonArray theJa = new JsonArray();
		
		theBinaries.stream().forEach( aBinary -> {
			
			Job theJob = new Job();
			theJob.put( "id" , aBinary.getId() );
			theJob.put( "created" , aBinary.getCreated().toString() );
			theJob.put( "updated" , aBinary.getUpdated().toString() );
			theJob.put( "size" , Utils.toStringDataSize( aBinary.getLength() ) );
			theJob.put( "sha256" , aBinary.getSha256() );
			
			theJa.add( theJob.build() );
			
		} );
		
		return HtmlUtils.respond200Ok( theJa );
		
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
