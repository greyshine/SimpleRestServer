package de.greyshine.restservices.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greyshine.restservices.Application;
import de.greyshine.restservices.HttpHeader;
import de.greyshine.restservices.IConfiguration;
import de.greyshine.restservices.RequestContext;
import de.greyshine.restservices.util.Utils;

public class WebAdminFilter implements Filter {

	static final Log LOG = LogFactory.getLog(WebAdminFilter.class);

	private Application application;
	private IConfiguration configuration;
	private File webcontent;

	public static final String URI_PREFIX = "/web";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		application = (Application) filterConfig.getServletContext().getAttribute(Application.SERVLETCONTEXT_KEY);
		configuration = (IConfiguration) filterConfig.getServletContext()
				.getAttribute(IConfiguration.SERVLETCONTEXT_KEY);

		webcontent = new File(application.getBasepath(), "webcontent");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		final HttpServletRequest inReq = (HttpServletRequest) request;
		final HttpServletResponse inRes = (HttpServletResponse) response;

		if (handleLogin(inReq, inRes) == false) {
			
			return;
		}
		
		String thePath = inReq.getRequestURI();
		thePath = thePath.substring(URI_PREFIX.length());

		if (Utils.isBlank(thePath)) {

			inRes.sendRedirect("web/");
			return;
		}

		thePath = Utils.defaultIfBlank(thePath, "/");
		thePath = "/".equals(thePath) ? "/index.html" : thePath;

		if (!sendResponse(inReq, inRes, thePath)) {

			chain.doFilter(request, response);
		}
	}

	/**
	 * @return <code>true</code> when login was successfully managed. <code>false</code> indicating that request should block
	 */
	private boolean handleLogin(HttpServletRequest inReq, HttpServletResponse inRes) {

		final Utils.Kvp<String,String> theUc = configuration.getUserCredentials();
		
		if ( theUc == null ) {
			
			return true;
			
		} else if (Utils.isEquals( theUc.key, RequestContext.get().getUser())) {
			
			return true;
		}
		
		
		final Utils.Kvp<String,String> theUserAndPassword = Utils.evaluateUserPassword(inReq);
		
		final boolean isCorrectUser = Utils.isEquals( theUc.key, theUserAndPassword.key  ); 
		final boolean isCorrectPassword = Utils.isEquals( theUc.value, theUserAndPassword.value );
		
		if ( isCorrectUser && isCorrectPassword ) {
			
			RequestContext.get().setUser( theUserAndPassword.key );
			return true;
		}
		
		inRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		inRes.setHeader(HttpHeader.WWW_AUTHENTICATE, "BASIC realm=\"\"");
		return false;
	}

	@Override
	public void destroy() {

	}
	
	private boolean sendResponse(HttpServletRequest inReq, HttpServletResponse inRes, String inResource)
			throws IOException {

		InputStream theIs = null;

		File theContentFile = new File(webcontent, inResource);

		if (theContentFile.isFile()) {

			try {

				theIs = new FileInputStream(theContentFile);

			} catch (Exception e) {

				LOG.warn(e);
			}
		}

		if (theIs == null) {

			theIs = Utils.defaultIfNull(theIs,
					Thread.currentThread().getContextClassLoader().getResourceAsStream("webcontent/" + inResource));
		}

		if (theIs != null) {

			inRes.setStatus(HttpServletResponse.SC_OK);
			Utils.copy(theIs, inRes.getOutputStream(), true, false);

			return true;
		}

		return false;

	}



}
