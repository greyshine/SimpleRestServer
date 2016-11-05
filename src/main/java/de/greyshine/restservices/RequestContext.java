package de.greyshine.restservices;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import de.greyshine.restservices.util.Utils;


public class RequestContext implements IStatusReportable {
	
	public static final Log LOG = LogFactory.getLog( RequestContext.class );

	private static final ThreadLocal<RequestContext> TL = new ThreadLocal<RequestContext>() {
		@Override
		protected RequestContext initialValue() {
			return new RequestContext();
		}
	};
	
	public static final String SESSION_KEY_USER = RequestContext.class.getCanonicalName() +".USER";
	
	private String id;
	private Application application;
	private IConfiguration configuration;
	
	private String user;
	private HttpServletRequest httpServletRequest;
	private HttpServletResponse httpServletResponse;
	
	public ResponseBuilder responseBuilder = Response.ok();
	
	public Response buildResponse() {
		return responseBuilder.build();
	}
	
	public String getUser() {
		
		if ( user == null ) {
			
			user = getSessionAttribute( SESSION_KEY_USER, null);
		}
		
		return user;
	}

	public void setUser(String user) {
		setSessionAttribute( SESSION_KEY_USER , this.user = Utils.trimToNull( user ));
	}

	public String getId() {
		return id;
	}

	public Application getApplication() {
		return application;
	}
	
	public IConfiguration getConfiguration() {
		return configuration;
	}

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public HttpServletResponse getHttpServletResponse() {
		return httpServletResponse;
	}

	public void init(HttpServletRequest inHttpServletRequest, HttpServletResponse inHttpServletResponse) {

		responseBuilder = Response.ok();
		
		try {
		
			application = (Application) inHttpServletRequest.getServletContext().getAttribute( Application.SERVLETCONTEXT_KEY );
			
			if ( application == null ) { throw new IllegalStateException( "ServletContext does not reference Application anymore [key="+ Application.SERVLETCONTEXT_KEY +"]" ); }
			
		} catch (Exception e) {
			
			LOG.fatal(e);
			try {
				inHttpServletResponse.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
			} catch (IOException e1) {
				// swallow
				LOG.debug(e);
			}
			throw new IllegalStateException(e);
		}

		try {
			
			configuration = (IConfiguration) inHttpServletRequest.getServletContext().getAttribute( IConfiguration.SERVLETCONTEXT_KEY );
			
			if ( configuration == null ) { throw new IllegalStateException( "ServletContext does not reference IConfiguration anymore [key="+ IConfiguration.SERVLETCONTEXT_KEY +"]" ); }
			
		} catch (Exception e) {
			
			LOG.fatal(e);
			try {
				inHttpServletResponse.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
			} catch (IOException e1) {
				// swallow
				LOG.debug(e);
			}
			throw new IllegalStateException(e);
		}
		
		httpServletRequest = inHttpServletRequest;
		httpServletResponse = inHttpServletResponse;
		
		user = getSessionAttribute( SESSION_KEY_USER, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getSessionAttribute(String inName, T inDefault) {

		try {

			return Utils.defaultIfNull((T) httpServletRequest.getSession(false).getAttribute(inName), inDefault);

		} catch (Exception e) {

			return inDefault;
		}
	}
	
	public void invalidateSession() {
		
		try {
			
			httpServletRequest.getSession( false ).invalidate();
			
		} catch (Exception e) {
			
			//swallow
		}
	}

	public void setSessionAttribute(String inName, Serializable inValue) {

		if (httpServletRequest == null) {
			throw new IllegalStateException("no HttpServletRequest in scope");
		}

		HttpSession theSession = httpServletRequest.getSession(true);
		if (theSession.isNew()) {

			// TODO add listener
		}

		theSession.setAttribute(inName, inValue);
	}

	public static RequestContext get() {
		return TL.get();
	}

	public String getAddress() {
		return httpServletRequest.isSecure() ? application.getHttpsAddress() : application.getHttpAddress();
	}
	
	public boolean isUserInSession() {
		return getUser() != null;
	}

	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append( getClass().getSimpleName() );
		sb.append( " [" );
		
		try {
			
			sb.append( "uri="+ getHttpServletRequest().getRequestURI() );
			
		} catch (Exception e) {
			// swallow
		}
		
		sb.append( "]" );
		
		return sb.toString();
	}

	@Override
	public JsonObject createStatusReport() {
		
		final JsonObject theJo = new JsonObject();
		
		theJo.addProperty( "url" , httpServletRequest.getRequestURL().toString() );
		theJo.addProperty( "uri" , httpServletRequest.getRequestURI() );
		theJo.addProperty( "remoteHost" , httpServletRequest.getRemoteHost() );
		theJo.addProperty( "method" , httpServletRequest.getMethod() );
		
		return theJo;
	}
	
}
