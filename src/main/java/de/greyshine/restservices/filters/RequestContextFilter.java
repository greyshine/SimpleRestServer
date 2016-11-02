package de.greyshine.restservices.filters;

import java.io.IOException;

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

import de.greyshine.restservices.RequestContext;

public class RequestContextFilter implements Filter {
	
	final Log LOG = LogFactory.getLog( RequestContextFilter.class );
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		RequestContext.get().init( (HttpServletRequest)request, (HttpServletResponse)response);
		
		try {
		
			chain.doFilter(request, response);
			
		} catch (Exception e) {
			
			LOG.error( e, e );
		}
	}

	@Override
	public void destroy() {
		
		
	}

}
