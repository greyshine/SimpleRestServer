package de.greyshine.restservices.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonElement;

import de.greyshine.restservices.ApplicationException;
import de.greyshine.restservices.Constants;
import de.greyshine.restservices.IJsonStorageService;
import de.greyshine.restservices.IJsonStorageService.IDocument;
import de.greyshine.restservices.util.HtmlUtils;
import de.greyshine.restservices.util.Job;
import de.greyshine.restservices.util.JsonUtils;

@Path("/")
public class PostHandler extends AbstractHandler {

	static final Log LOG = LogFactory.getLog( PostHandler.class );

	public PostHandler(@Context HttpServletRequest inRequest) {
		super(inRequest);
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{collection:" + Constants.REGEX_COLLECTION_NAME + "}")
	public Response doPostJson(@PathParam("collection") String inCollectionName) throws IOException {

		if (Boolean.FALSE == isJsonContentTypeRequest) {

			return Response.status(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE).build();
		}

		final IJsonStorageService theStorage = getJsonStorageService();
		
		final IDocument theInfo = theStorage.create(inCollectionName, getRequestEntityJson() );

		try {

			throwExceptionOnFailure(theInfo);

		} catch (Exception e) {

			theStorage.delete(inCollectionName, theInfo.getId());

			final JsonElement theStatus = application.createResponseJson(HttpServletResponse.SC_BAD_REQUEST, "400",
					e.getMessage(), Job.create().put("$id", theInfo.getId()).build());

			return Response.status(HttpServletResponse.SC_BAD_REQUEST)//
					.entity(JsonUtils.jsonToString(theStatus, true))//
					.build();
		}

		LOG.debug("POST " + theInfo.getId());

		final boolean isVerbose = requestInfo.isVerbose();

		return HtmlUtils.returnDocumentResponse(theInfo.getId(), inCollectionName);
	}
}
