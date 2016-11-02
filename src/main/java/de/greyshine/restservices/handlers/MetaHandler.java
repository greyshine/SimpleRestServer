package de.greyshine.restservices.handlers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import de.greyshine.restservices.META;
import de.greyshine.restservices.util.Job;

@Path("/")
public class MetaHandler extends AbstractHandler {

	static final Log LOG = LogFactory.getLog(MetaHandler.class);
	
	public MetaHandler(@Context HttpServletRequest inRequest) {
		super(inRequest);
	}
	
	@META
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/ping")
	public String ping(@Context UriInfo ui) {

		return Job.buildAsString("pong", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), false);
	}

	@META
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/status")
	public Response status() {

		final JsonObject jo = application.createStatusReport();
		return createResponse(jo);
	}

}
