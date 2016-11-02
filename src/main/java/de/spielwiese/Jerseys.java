package de.spielwiese;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

@ApplicationPath( "/ctx" )
@Path("/")
public class Jerseys extends Application {
	
	public Jerseys() {
		System.out.println( "started" );
	}

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path( "id" )
    public String handleGet() {
        return "{\"hallo\": " + System.currentTimeMillis() +", thread:\""+ Thread.currentThread() +"\" }";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path( "{id}" )
    public String handleGet2(@PathParam("id") String inId ) {
        return "{\"hallo2\": " + System.currentTimeMillis() + ", \"id\": "+ ( inId == null ? null : "\""+inId +"\"" ) +" }";
    }

    public static void main(String[] args) throws Exception {
        final Server theServer = new Server(7778);
        try {
            // https://nikolaygrozev.wordpress.com/2014/10/16/rest-with-embedded-jetty-and-jersey-in-a-single-jar-step-by-step/
            final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            theServer.setHandler(context);
            final ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
            jerseyServlet.setInitOrder(0);
            jerseyServlet.setInitParameter("jersey.config.server.provider.classnames",Jerseys.class.getCanonicalName());
            theServer.setStopAtShutdown(true);
            theServer.start();
            theServer.join();
        } finally {
            theServer.stop();
            theServer.destroy();
        }
    }
}
