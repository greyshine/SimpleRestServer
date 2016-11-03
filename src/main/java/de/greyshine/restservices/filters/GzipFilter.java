package de.greyshine.restservices.filters;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greyshine.restservices.interceptors.GzipInterceptor;
import de.greyshine.restservices.util.HtmlUtils;

@Provider
public class GzipFilter implements ContainerRequestFilter, ContainerResponseFilter {
	
	static Log LOG = LogFactory.getLog( GzipFilter.class );
	
	/**
	 * It seems that the response eliminates the response header Content-Encoding=gzip
	 * What so ever. The client reports an error when reading the gzip stream: Unexpected end of ZLIB input stream
	 * 
	 * Some not too helpful answers for solvings:
	 * http://stackoverflow.com/questions/25542450/gzip-format-decompress-jersey
	 * http://stackoverflow.com/questions/17541223/setting-content-type-encoding-in-jersey-rest-client
	 * 
	 * 
	 */
	public static final boolean didYouSolveTheBug = System.currentTimeMillis() < 0 || false;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		
		if ( isAcceptEncodingGzip( requestContext.getHeaders() ) ) {
			requestContext.setProperty( GzipInterceptor.PROPERTY_KEY_GZIP , Boolean.TRUE);
		}
	}

	private boolean isAcceptEncodingGzip(MultivaluedMap<String, String> headers) {
		
		final List<String> theHeaders = headers.get( HtmlUtils.HEADER_ACCEPT_ENCODING ); 
		
		if ( theHeaders == null ) { return false; }
		
		for( String v : theHeaders ) {
			
			if ( v!=null && v.toLowerCase().contains( "gzip" ) ) {
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		
		// TODO: only gzip response text, json, xml, ...
		// images and movies are often compressed by nature
		final boolean isGzipEncoding = Boolean.TRUE.equals( requestContext.getProperty( GzipInterceptor.PROPERTY_KEY_GZIP ) );
		
		if ( isGzipEncoding && didYouSolveTheBug ) {
		
			responseContext.getHeaders().add( HtmlUtils.HEADER_CONTENT_ENCODING , "gzip");
		} 
		
		if ( isGzipEncoding && !didYouSolveTheBug) {
			
			LOG.debug("skipping response 'Content-Encoding: gzip' due to unresolved bug.");
		}
		
		
		
	}
}
