package de.greyshine.restservices.filters;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greyshine.restservices.HttpHeader;
import de.greyshine.restservices.IConfiguration;
import de.greyshine.restservices.RequestContext;
import de.greyshine.restservices.util.Utils;
import de.greyshine.restservices.util.Utils.Kvp;

@Provider
public class AuthentificationFilter implements ContainerRequestFilter {
	
	static final Log LOG = LogFactory.getLog( AuthentificationFilter.class );
	
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		
		if ( !isPass() ) {
			
			requestContext.abortWith( Response.status( Status.UNAUTHORIZED ).header(HttpHeader.WWW_AUTHENTICATE, "BASIC realm=\"\"").build() );
			return;
		}
	}


	private boolean isPass() {
		
		final RequestContext theRc = RequestContext.get();
		final IConfiguration configuration = theRc.getConfiguration();
		final Kvp<String,String> theUc = configuration.getUserCredentials(); 
		
		if ( theUc == null ) { return true; }
		else if ( theRc.isUserInSession() ) { return true; }
		
		final Utils.Kvp<String,String> theUserAndPassword = Utils.evaluateUserPassword( theRc.getHttpServletRequest() );
		
		final boolean isCorrectUser = Utils.isEquals( theUc.key, theUserAndPassword.key  ); 
		final boolean isCorrectPassword = Utils.isEquals( theUc.value, theUserAndPassword.value );
		
		if ( isCorrectUser && isCorrectPassword ) {
			
			RequestContext.get().setUser( theUserAndPassword.key );
			return true;
		}
		
		// ok i dunno understand that: principal and subject ...
		// i am used to logins, passwords and roles

		LOG.info( "bad login attempt: "+ theUserAndPassword +"; "+ theRc.createStatusReport() );
		
		return false;
	}

}
