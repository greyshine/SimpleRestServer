package de.greyshine.restservices.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.greyshine.restservices.RequestContext;
import de.greyshine.restservices.IJsonStorageService.IDocument;
import de.greyshine.restservices.IJsonStorageService.IDocument.EStreamType;
import de.greyshine.restservices.util.Utils.Kvp;

public abstract class HtmlUtils {

	private HtmlUtils() {
		
	}
	
	public static final String HEADER_X_Total_Count = "X-Total-Count";
	public static final String HEADER_CONTENT_TYPE = "Content-Type"; 
	public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
	public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
	public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	
	
	public static Response createResponse(File inFile) {

		try {

			return createResponse(new FileInputStream(inFile));

		} catch (FileNotFoundException e) {
			return Response.noContent().build();
		}
	}

	public static Response createResponse(InputStream inIs) {

		return Response.ok().entity(inIs).header(HtmlUtils.HEADER_X_Total_Count, 0).build();
	}

	public static Response streamResponse(IDocument inItem) {
		return createResponse(inItem.getStream(EStreamType.DATA));
	}
	
	public static  Response returnDocumentResponse(String inId, String inCollectionName) {

		final JsonObject jo = new JsonObject();
		jo.addProperty("id", inId);

		jo.addProperty("location", RequestContext.get().getAddress() + "/" + inCollectionName + "/" + inId);

		return Response.status( HttpServletResponse.SC_CREATED )//
				// https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.30
				.header( "Location" , RequestContext.get().getAddress() + "/" + inCollectionName + "/" + inId)//
				.entity(JsonUtils.jsonToString(jo, true))//
				.build();
	}
	
	public static Response respond200Ok(JsonElement inJe) {
		
		ResponseBuilder r = Response.ok();
		r.header( HEADER_CONTENT_TYPE, "application/json");
		
		if ( inJe != null ) {
			
			r.entity( JsonUtils.jsonToString(inJe, true) );
		}
		
		return r.build();
	}

	public static  Response respond200Ok(IDocument inItem, boolean inIncludeData) {
	
		ResponseBuilder r = Response.ok();
		
		if ( inItem instanceof IDocument ) {
			r = decorate( r, inItem, inIncludeData );
		}
		
		return r.build();
	}
	
	public static ResponseBuilder decorate( ResponseBuilder inRb, IDocument inDocument, boolean inIncludeData ) {
		
		if ( inRb == null || inDocument == null ) {
			return inRb;
		}
		
		inRb.header( "ETag", inDocument.getEtag());
		inRb.header( "Last-Modified" ,  Utils.dateToHttpDate( inDocument.getUpdated() ) );
		
		if ( inIncludeData ) {
			
			inRb.entity( inDocument.getStream( EStreamType.DATA ) );
		}
		
		
		return inRb;
		
	}
	
	public static Response respond200Ok() {
		return respond200Ok(null, false);
	}
	
	public static Response respond204NoContent() {
		return Response.status( HttpServletResponse.SC_NO_CONTENT ).build();
	}
	
	public static Response respond400BadRequest() {
		return respond400BadRequest(null);
	}
	
	public static Response respond400BadRequest(String inMessage) {

		final ResponseBuilder r = Response.status( HttpServletResponse.SC_BAD_REQUEST ); 
		
		if ( inMessage != null ) {
			
			r.entity(JsonUtils.jsonToString( createJsonResponseEntity( inMessage , null ) , true) );
		}
		
		return r.build();
		
	}
	
	public static Response respond404NotFound() {
		return Response.status( HttpServletResponse.SC_NOT_FOUND ).build();
	}

	public static Response respond500ServerError() {
		return respond500ServerError(null);
	}
	
	public static Response respond500ServerError(Exception inException) {
		
		final ResponseBuilder r = Response.status( HttpServletResponse.SC_BAD_REQUEST ); 
		
		if ( inException != null ) {
			r.entity( JsonUtils.jsonToString( createJsonResponseEntity( inException.getMessage(), inException ) , true) );
		}
		
		return r.build();
	}
	
	public static Response respond304NotModified() {
		return Response.notModified().build();
	}
	
	public static JsonObject createJsonResponseEntity(String inMessage, Exception inException) {
		
		final JsonObject theJo = new JsonObject();
		
		if ( inMessage != null ) {
			theJo.addProperty( "message" , Utils.trimToEmpty( inMessage ) );	
		}
		
		if ( inException != null ) {
			
			theJo.addProperty( "error" , Utils.trimToEmpty( inException.getMessage() ) );
		}
		
		return theJo;
	}
	
	public static Response respond304NotModified(IDocument inDocument) {
		
		return decorate( Response.notModified() , inDocument, false).build();
	}
	
	public static Kvp<String,String> evaluateUserPassword(HttpServletRequest inReq) {
		
		String theAuthorisationHeader = inReq.getHeader("Authorization");
		
		if (theAuthorisationHeader == null || !theAuthorisationHeader.toUpperCase().startsWith("BASIC ")) {
			return new Kvp<String,String>();
		}
		
		String theUserAndPassword = inReq.getHeader("Authorization").substring(5).trim();
		theUserAndPassword = new String(java.util.Base64.getDecoder().decode(theUserAndPassword));
		final int idxColon = theUserAndPassword.indexOf(':');

		final String theUserCandidate = idxColon < 1 ? null
				: Utils.trimToNull(theUserAndPassword.substring(0, idxColon));
		final String thePassword = theUserCandidate == null ? null
				: Utils.trimToEmpty(theUserAndPassword.substring(idxColon+1));
		
		return new Kvp<String,String>( theUserCandidate, thePassword );
	}
	
	
}
