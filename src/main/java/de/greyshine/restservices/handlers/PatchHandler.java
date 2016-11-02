package de.greyshine.restservices.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import de.greyshine.restservices.Constants;
import de.greyshine.restservices.IBinaryStorageService.IBinary;
import de.greyshine.restservices.IJsonStorageService.IDocument;
import de.greyshine.restservices.util.JsonUtils;
import de.greyshine.restservices.util.ResponseUtils;
import de.greyshine.restservices.util.Utils;

@Path("/")
public class PatchHandler extends AbstractHandler {
	
	static final Log LOG = LogFactory.getLog( PatchHandler.class );

	public PatchHandler(@Context HttpServletRequest inRequest) {
		super(inRequest);
	}
	
	@PATCH
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{collection:" + Constants.REGEX_COLLECTION_NAME + "}/{id:"
			+ Constants.REGEX_COLLECTION_ITEM_ID + "}/{property:" + Constants.REGEX_PROPERTYNAME
			+ "}")
	public Response doPatch(@PathParam("collection") String inCollectionName, @PathParam("id") String inId,
			@PathParam("property") String inProperty) throws IOException {

		if ( Utils.isBlank( inProperty ) ) {
			return ResponseUtils.respond400BadRequest();
		}
		
		final IDocument theItem = getJsonStorageService().read(inCollectionName, inId);

		if ( theItem.getException() != null ) {
			
			return ResponseUtils.respond500ServerError(  );
		
		} else if (theItem.isNotFound()) {

			return Response.status(404).build();
		}

		if (Boolean.FALSE == isJsonContentTypeRequest) {

			return doUploadFile(theItem, inProperty);

		} else {

			return doPatchJsonValue(theItem, inCollectionName, inId, inProperty);
		}
	}
	
	private Response doUploadFile(IDocument inItem, String inProperty)
			throws IOException {
		
		JsonElement theDataJsonElement = Utils.defaultIfNull( inItem.getDataJson() , JsonNull.INSTANCE);

		if ( theDataJsonElement.isJsonNull() ) {
			
			final JsonObject theJo = new JsonObject();
			theJo.add( inProperty , JsonNull.INSTANCE);
			theDataJsonElement = theJo;
		
		} else if ( !theDataJsonElement.isJsonObject() ) {
			
			return ResponseUtils.respond400BadRequest( "document is no json-object" );
		}
		
		final JsonObject theDataJsonObject = theDataJsonElement.getAsJsonObject();
		
		
		// TODO: fetch existing binary content and delete it
		//final BinaryReference theOldBinaryReference = BinaryReference.fromJson( !theDataJsonElement.isJsonObject() ? null : theDataJsonElement.getAsJsonObject().get( inProperty ) );
		
		final IBinary theNewBinary = getBinaryStorageService().create( getRequestEntityStream() );
		
		if ( !theNewBinary.isExceptional() ) {
			
			theDataJsonObject.add(inProperty, JsonUtils.createBinaryReferenceJson( theNewBinary.getId() ) );
		}
		
		inItem = getJsonStorageService().update( inItem.getCollection() , inItem.getId(), theDataJsonObject);
		
		return !inItem.isExceptional() ? ResponseUtils.respond200Ok( inItem, requestInfo.isVerbose() ) : ResponseUtils.respond500ServerError( inItem.getException() );
	}

	private Response doPatchJsonValue(IDocument inDocument, String inCollectionName, String inId, String inProperty)
			throws IOException {

		final boolean isPropertyArrayIndex = inProperty.matches("\\[[0-9+]\\]");

		final JsonElement theDataJsonElement = Utils.defaultIfNull( inDocument.getDataJson() , JsonNull.INSTANCE);
		final JsonElement theNewPropertyJson = getRequestEntityJson();

		boolean changed = false;

		if ( theDataJsonElement.isJsonArray() && isPropertyArrayIndex) {

			final int theIndex = Integer.parseInt(inProperty.substring(1, inProperty.length() - 1));
			final JsonArray theJa = (JsonArray) inDocument.getDataJson();
			while (theIndex >= theJa.size()) {
				theJa.add(JsonNull.INSTANCE);
			}

			theJa.set(theIndex, theNewPropertyJson);
			changed = true;

		} else if (theDataJsonElement.isJsonObject()) {
			
			final JsonObject theJo = (JsonObject) inDocument.getDataJson();
			theJo.add(inProperty, theNewPropertyJson);
			changed = true;
		}

		if (changed) {

			inDocument.writeDataJson( theDataJsonElement );
		}

		LOG.debug("PATCH " + (changed ? "" : "(unchanged)") + "" + inId + " :: " + inProperty + " : "
				+ theNewPropertyJson);

		if (!changed) {

			return ResponseUtils.respond304NotModified();

		} else {
			
			return ResponseUtils.streamResponse( inDocument );
		}

	}

}
