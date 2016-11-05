package de.greyshine.restservices.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.greyshine.restservices.Constants;
import de.greyshine.restservices.IJsonStorageService;
import de.greyshine.restservices.IJsonStorageService.EHandleResult;
import de.greyshine.restservices.IJsonStorageService.FindResult;
import de.greyshine.restservices.IJsonStorageService.IDocument;
import de.greyshine.restservices.IJsonStorageService.IResultHandler;
import de.greyshine.restservices.RequestContext;
import de.greyshine.restservices.util.HtmlUtils;
import de.greyshine.restservices.util.Utils;

@Path("/")
public class GetHandler extends AbstractHandler {

	static final Log LOG = LogFactory.getLog(GetHandler.class);

	public GetHandler(@Context HttpServletRequest inRequest) {
		super(inRequest);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{collection:" + Constants.REGEX_COLLECTION_NAME + "}")
	public Response doGetCollection(@PathParam("collection") String inCollectionName) throws IOException {
		
		final JsonArray theJa = new JsonArray();
		final boolean isEmbed = requestInfo.isEmbed();
		final boolean isEnvelope = requestInfo.isEnvelope();

		final IJsonStorageService theJss = application.getDocumentStorageService(); 

		theJss.find(inCollectionName,
			
			requestInfo.getSortings(Constants.REGEX_PROPERTYNAME),
			requestInfo.getSelects(Constants.REGEX_PROPERTYNAME),
			requestInfo.getOffset(),
			requestInfo.getLength(),
			new IResultHandler<IJsonStorageService.FindResult>() {
				
				@Override
				public EHandleResult handle(FindResult aFr) {
					
					final JsonObject theJo = new JsonObject();
					theJa.add(theJo);

					theJo.addProperty("id", aFr.id);
					theJo.addProperty("index", aFr.index);
					theJo.addProperty("location", RequestContext.get().getAddress() + "/" + aFr.collection + "/" + aFr.id);
					
					//final JsonObject theEnvelopeData = aFr.jsonElement;
					//final JsonElement theData = theEnvelopeData.get( "data" );
					
					if ( isEnvelope && isEmbed) {
					
						theJo.add( "data" ,  aFr.jsonDataEnvelope );
					
					} else if ( !isEnvelope && isEmbed ) {
						
						theJo.add( "data" ,  aFr.jsonData );
					
					} else if ( isEnvelope && !isEmbed ) {
						
						aFr.jsonDataEnvelope.remove( "data" );
						theJo.add( "data", aFr.jsonDataEnvelope );
					}
					
					return EHandleResult.CONTINUE;
				}
				
				@Override
				public EHandleResult handleException(FindResult inResult, Exception inException) {
					return EHandleResult.CONTINUE;
				}
			}
		);

		return createResponse(theJa);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{collection:" + Constants.REGEX_COLLECTION_NAME + "}/{id:"
			+ Constants.REGEX_COLLECTION_ITEM_ID + "}")
	public Response doGetCollectionItem(@PathParam("collection") String inCollectionName,
			@PathParam("id") String inId) {

		final String theIfNoneMatch = Utils.trimToEmpty(request.getHeader("If-None-Match"));
		
		final IDocument theInfo = application.getDocumentStorageService().read(inCollectionName, inId);
		
		if (theInfo.isExceptional()) {

			LOG.error(theInfo.getException());
			return HtmlUtils.respond500ServerError();

		} else if (theInfo.isNotFound()) {

			return HtmlUtils.respond404NotFound();
		}

		boolean isMatchEtag = Utils.isNotBlank( theInfo.getEtag() ) && requestInfo.isHeaderValue( "If-None-Match", theInfo.getEtag() );
		
		boolean isMatchIfNoneMatch = Utils.isNotBlank( theIfNoneMatch ) && theInfo.getUpdated() != null && theIfNoneMatch.equalsIgnoreCase( Utils.dateToHttpDate( theInfo.getUpdated() ) );

		if (theInfo.isFound() && (isMatchEtag || isMatchIfNoneMatch)) {

			return HtmlUtils.respond304NotModified(theInfo);
		}

		final ResponseBuilder theRb = Response.ok();
		HtmlUtils.decorate(theRb, theInfo, true);

		return theRb.build();
	}
	

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{collection:" + Constants.REGEX_COLLECTION_NAME + "}/{id:"
			+ Constants.REGEX_COLLECTION_ITEM_ID + "}"
			+ "/{property:"+Constants.REGEX_PROPERTYNAME +"}")
	public Response doGetCollectionItem(@PathParam("collection") String inCollectionName, @PathParam("id") String inId, @PathParam("property") String inProperty) {
		
		final String theIfNoneMatch = Utils.trimToEmpty(request.getHeader("If-None-Match"));
		
		final IDocument theInfo = application.getDocumentStorageService().read(inCollectionName, inId);
		
		if (theInfo.isExceptional()) {

			LOG.error(theInfo.getException());
			return HtmlUtils.respond500ServerError();

		} else if (theInfo.isNotFound()) {

			return HtmlUtils.respond404NotFound();
		}
		
		JsonElement theJsonElement = null;
		
		try {
			theJsonElement = theInfo.getDataJson();
		} catch (IOException e) {
			
			LOG.error(e);
			return HtmlUtils.respond500ServerError();
		}
		
		throw new UnsupportedOperationException("not yet implemented");
	}

}
