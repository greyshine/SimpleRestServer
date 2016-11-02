package de.greyshine.restservices.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonElement;

import de.greyshine.restservices.Constants;
import de.greyshine.restservices.IJsonStorageService;
import de.greyshine.restservices.IJsonStorageService.IDocument;
import de.greyshine.restservices.util.Job;
import de.greyshine.restservices.util.JsonUtils;
import de.greyshine.restservices.util.ResponseUtils;

@Path("/")
public class PutHandler extends AbstractHandler {
	
	static final Log LOG = LogFactory.getLog( PutHandler.class );

	public PutHandler(@Context HttpServletRequest inRequest) {
		super(inRequest);
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{collection:" + Constants.REGEX_COLLECTION_NAME + "}/{id:"
			+ Constants.REGEX_COLLECTION_ITEM_ID + "}")
	public Response doPut(@PathParam("collection") String inCollectionName, @PathParam("id") String inId)
			throws IOException {

		if (Boolean.FALSE == isJsonContentTypeRequest) {

			return Response.status(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE).build();
		}

		final IJsonStorageService theStorage = getJsonStorageService();
		final IDocument theItem = theStorage.read(inCollectionName, inId);

		if ( theItem.isNotFound() ) {

			return ResponseUtils.respond404NotFound();
		}

		try {
			
			theItem.writeDataJson( getRequestEntityJson() );

		} catch (Exception e) {

			theStorage.delete(inCollectionName, inId);

			final JsonElement theStatus = application.createResponseJson(HttpServletResponse.SC_BAD_REQUEST, "400",
					e.getMessage(), Job.create().put("$id", inId).build());

			return Response.status(HttpServletResponse.SC_BAD_REQUEST)//
					.entity(JsonUtils.jsonToString(theStatus, true))//
					.build();
		}

		LOG.debug("PUT " + inId);

		return ResponseUtils.returnDocumentResponse(inId, inCollectionName);
	}


	
}
