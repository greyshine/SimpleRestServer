package de.greyshine.restservices.handlers;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonElement;

import de.greyshine.restservices.Application;
import de.greyshine.restservices.ApplicationException;
import de.greyshine.restservices.IBinaryStorageService;
import de.greyshine.restservices.IConfiguration;
import de.greyshine.restservices.IJsonStorageService;
import de.greyshine.restservices.IJsonStorageService.IDocument;
import de.greyshine.restservices.RequestContext;
import de.greyshine.restservices.RequestInfo;
import de.greyshine.restservices.ApplicationException.EReason;
import de.greyshine.restservices.util.HtmlUtils;
import de.greyshine.restservices.util.JsonUtils;
import de.greyshine.restservices.util.Utils;

public abstract class AbstractHandler {
	
	public enum RequestType {
		
		GET_COLLECTION,
		GET_COLLECTION_ITEM
		;
		
	}
	
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog( AbstractHandler.class );

	protected Application application;
	
	protected IConfiguration configuration;

	protected final HttpServletRequest request;
	protected final RequestInfo requestInfo;

	protected final boolean isJsonContentTypeRequest;
	private JsonElement jsonElement;
	
	public AbstractHandler() {
		this(null);
	}
	
	public AbstractHandler(HttpServletRequest inRequest) {

		inRequest = inRequest != null ? inRequest : RequestContext.get().getHttpServletRequest();
		
		application = (Application) inRequest.getServletContext().getAttribute( Application.SERVLETCONTEXT_KEY );
		
		configuration = (IConfiguration) inRequest.getServletContext().getAttribute( IConfiguration.SERVLETCONTEXT_KEY );
		
		request = inRequest;
		
		requestInfo = new RequestInfo(inRequest);

		final String contentType = request.getHeader(HtmlUtils.HEADER_CONTENT_TYPE);

		isJsonContentTypeRequest = contentType != null && ("application/json".equalsIgnoreCase(contentType)
				|| contentType.toLowerCase().startsWith("application/json;"));
	}
	
	public IJsonStorageService getJsonStorageService() {
		return application.getDocumentStorageService();
	}
	
	public IBinaryStorageService getBinaryStorageService() {
		return application.getBinaryStorageService();
	}
	
	public void throwExceptionOnFailure(IDocument inInfo) {
		
		if ( inInfo == null || inInfo.getException() == null ) { return; }
		
		throw Utils.toRuntimeException( inInfo.getException() );
	}
	
	public static Response createResponse(JsonElement inJson) {
		return Response.ok().entity(JsonUtils.jsonToString(inJson, true)).build();
	}

	protected JsonElement getRequestEntityJson()	 {
		
		if ( jsonElement != null ) { return jsonElement; }
		else if ( !isJsonContentTypeRequest ) {
			
			throw new ApplicationException( EReason.INPUT_IS_NO_JSON, "no content-type application/json declared").technicalError(false);
		}

		try {
		
			jsonElement = JsonUtils.readJson( getRequestEntityStream() );
			
			if ( jsonElement == null ) {
				throw new NullPointerException( "no data read from request entity stream" );
			}
			
		} catch (Exception e) {
			
			throw new ApplicationException( EReason.INPUT_IS_NO_JSON, "reading json entity stream failed", e).technicalError(false);
		}
		
		return jsonElement;
	}
	
	/**
	 * 
	 * @return entity stream of the current request
	 * @throws IOException
	 */
	public InputStream getRequestEntityStream() throws IOException {
		
		final long maxReads = configuration.maxUploadSize();
		
		return new InputStream() {
			
			final InputStream theOriginalIs = request.getInputStream();
			
			long reads = 0;
			
			@Override
			public int read() throws IOException {
				
				final int r = theOriginalIs.read();
				
				if ( ++reads > maxReads && r != -1 ) {
					
					throw new IOException( "too much data to consume exceeding "+ maxReads +" bytes" ); 
				}
				
				return r;
			}

			@Override
			public void close() throws IOException {
				theOriginalIs.close();
			}
		};
	}
	
	public JsonElement beforeHandle(JsonElement inRequestElement) {
		return inRequestElement;
	}
	
	public JsonElement afterHandle(JsonElement inResponseElement) {
		return inResponseElement;
	}
	
}
