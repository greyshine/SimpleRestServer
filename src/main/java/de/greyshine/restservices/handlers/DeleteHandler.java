package de.greyshine.restservices.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
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
import de.greyshine.restservices.IJsonStorageService.IDocument;
import de.greyshine.restservices.util.HtmlUtils;
import de.greyshine.restservices.util.Job;
import de.greyshine.restservices.util.JsonUtils;

@Path("/")
public class DeleteHandler extends AbstractHandler {

	static final Log LOG = LogFactory.getLog( DeleteHandler.class );
	
	public DeleteHandler(@Context HttpServletRequest inRequest) {
		super(inRequest);
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{collection:" + Constants.REGEX_COLLECTION_NAME + "}/{id:"
			+ Constants.REGEX_COLLECTION_ITEM_ID + "}")
	public Response doDelete(@PathParam("collection") String inCollectionName, @PathParam("id") String inId) {

		try {

			final IDocument theInfo = application.getDocumentStorageService().delete(inCollectionName, inId);
			
			if ( !theInfo.isExceptional() && theInfo.isNotFound() ) {
			
			} else if ( theInfo.isExceptional() && theInfo.isFound() ) {
				
				return HtmlUtils.respond500ServerError();
			}
			
			return HtmlUtils.respond204NoContent();

		} catch (Exception e) {

			final JsonElement theStatus = application.createResponseJson(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"500", e.getMessage(), Job.create().put("$id", inId).build());

			return Response.status(HttpServletResponse.SC_BAD_REQUEST)//
					.entity(JsonUtils.jsonToString(theStatus, true))//
					.build();
		}
	}
	
}
