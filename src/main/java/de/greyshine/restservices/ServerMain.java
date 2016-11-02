package de.greyshine.restservices;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import de.greyshine.restservices.Application.IServletFilterInfo;
import de.greyshine.restservices.filters.RequestContextFilter;
import de.greyshine.restservices.util.MainUtils;
import de.greyshine.restservices.util.MavenInfo;
import de.greyshine.restservices.util.Utils;
import de.greyshine.restservices.util.Utils.Kvp;
import de.greyshine.restservices.web.WebAdminFilter;

public class ServerMain {

	static final Log LOG = LogFactory.getLog(ServerMain.class);

	final Server theServer;
	private int httpPort;
	private Integer httpsPort;
	
	final Application application;
	final File basepath;
	final IConfiguration configuration;

	public ServerMain(final String... inArgs) throws Exception {
		
		this(new IConfiguration() {
			
			{
				LOG.debug( "Hello !-)" );
				LOG.debug( "args: "+ Arrays.asList( inArgs ) );	
			}
			
			final String[] args = inArgs == null ? new String[0] : Arrays.copyOf( inArgs , inArgs.length);
			
			private final Utils.Kvp<String,String> userCredentials;
			
			{
				
				Utils.Kvp<String,String> theUserCredentials = null; 
				
				final String theCredentials = MainUtils.getAfter(inArgs, "-user", (String)null); 
				
				if ( Utils.isNotBlank( theCredentials ) ) {
					
					try {
					
						final int idxColon = theCredentials.indexOf( ":" );

						final String theUser = theCredentials.substring( 0 , idxColon);
						final String thePassword = theCredentials.substring( idxColon+1);
					
						theUserCredentials = new Kvp<>( theUser, thePassword );
						
					} catch (Exception e) {
						
						throw new IllegalArgumentException("bad parameter -user: "+ MainUtils.getAfter(inArgs, "-user", "") );
					}
				}
				
				userCredentials = theUserCredentials;
				
			}
			
			@Override
			public String[] getArgs() {
				return args;
			}

			@Override
			public Integer getHttpPort() {
				return MainUtils.getAfter(inArgs, "-port.http", (Integer)null);
			}

			@Override
			public Integer getHttpsPort() {
				return MainUtils.getAfter(inArgs, "-port.https", (Integer)null);
			}

			@Override
			public int getServerSecondsToLive() {
				return MainUtils.getAfter(inArgs, "-ttl", "[0-9]+", -1);
			}

			@Override
			public File getKeystoreFile() {
				return MainUtils.getAfter(inArgs, "-keystore", null, false, true, false);
			}

			@Override
			public String getKeystorePassword() {
				return MainUtils.getAfter(inArgs, "-pwd.keystore", (String) null);
			}

			@Override
			public String getKeymanagerPassword() {
				return MainUtils.getAfter(inArgs, "-pwd.keymanager", getKeystorePassword());
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends Application> getApplicationClass() {

				if (MainUtils.isMatching(inArgs, -2, "-.+")) {

					return DefaultApplication.class;
				}

				try {
					return (Class<? extends Application>) ClassLoader.getSystemClassLoader()
							.loadClass(MainUtils.get(inArgs, -1));
				} catch (Exception e) {

					try {

						return DefaultApplication.class;

					} catch (Exception e2) {
						// swallow
					}

					throw Utils.toRuntimeException(e);
				}

			}

			@Override
			public File getBasepath() {
				return new File(MainUtils.getAfter(inArgs, "-path", null, "."));
			}

			@Override
			public boolean isWebAdminEnabled() {
				return MainUtils.isArg(inArgs, "-webadmin");
			}

			@Override
			public long maxUploadSize() {
				return DEFAULT_MAX_UPLOAD_SIZE;
			}

			@Override
			public Kvp<String, String> getUserCredentials() {
				return userCredentials;
			}
			
		});
	}

	public ServerMain(IConfiguration inCfg) throws Exception {

		configuration = inCfg;

		basepath = Utils.getCanonicalFileSafe( inCfg.getBasepath(), true);
		
		if ( !basepath.isDirectory() ) {
			throw new IllegalStateException("basepath is no directory: "+ basepath);
		}
		
		Integer theHttpPort = inCfg.getHttpPort();
		final Integer theHttpsPort = inCfg.getHttpsPort();
		
		if ( theHttpPort == null && theHttpsPort == null ) {
			theHttpPort = 7777;
		}

		application = inCfg.getApplicationClass().newInstance();

		final StringBuilder theClassNames = new StringBuilder();
		for (Class<?> aClass : application.getHandlerClasses()) {

			theClassNames.append(theClassNames.length() == 0 ? "" : ",");
			theClassNames.append(aClass.getCanonicalName());
			
			LOG.debug( "announced class: "+ aClass.getCanonicalName() );
		}
		
		theServer = new Server();
		
		// https://nikolaygrozev.wordpress.com/2014/10/16/rest-with-embedded-jetty-and-jersey-in-a-single-jar-step-by-step/
		final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		theServer.setHandler(context);
		context.setAttribute(Application.SERVLETCONTEXT_KEY, application);
		context.setAttribute(IConfiguration.SERVLETCONTEXT_KEY, configuration);
		context.addFilter( new FilterHolder( new RequestContextFilter() ), "/*", EnumSet.allOf( DispatcherType.class ) );
		context.addEventListener( new ShutdownListener() );
		
		final ServletHolder theJerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class,"/*");
		theJerseyServlet.setInitOrder(0);
		theJerseyServlet.setInitParameter("jersey.config.server.provider.classnames", theClassNames.toString());

		
		
		final Set<IServletFilterInfo> theServletFilterInfos = Utils.defaultIfNull(application.getServletFilters(), Collections.emptySet() );
		
		for (IServletFilterInfo aServletFilterInfo : theServletFilterInfos) {
			final EnumSet<DispatcherType> theDts = Utils.defaultIfNull( aServletFilterInfo.getDispatcherTypes(), EnumSet.allOf( DispatcherType.class ));
			final String thePath = Utils.defaultIfBlank( aServletFilterInfo.getPath(), "/*");
			context.addFilter( new FilterHolder( aServletFilterInfo.getFilter() ), thePath, theDts );
		}
		
		
		if ( inCfg.isWebAdminEnabled() ) {
			
			final FilterHolder theWebGuiHolder = context.addFilter(WebAdminFilter.class, WebAdminFilter.URI_PREFIX+"/*" , EnumSet.allOf( DispatcherType.class ));
			theWebGuiHolder.setInitParameters( new HashMap<>() );
			LOG.info( "enabling webadmin gui" );
		}
		
		// HTTP Configuration
		// HttpConfiguration is a collection of configuration information
		// appropriate for http and https. The default scheme for http is
		// <code>http</code> of course, as the default for secured http is
		// <code>https</code> but we show setting the scheme to show it can be
		// done. The port for secured communication is also set here.
		HttpConfiguration http_config = new HttpConfiguration();

		// HTTP connector
		// The first server connector we create is the one for http, passing in
		// the http configuration we configured above so it can get things like
		// the output buffer size, etc. We also set the port (8080) and
		// configure an idle timeout.
		ServerConnector theHttpConnector = null;
		
		if ( theHttpPort != 0 ) {
			theHttpConnector = new ServerConnector(theServer, new HttpConnectionFactory(http_config));
			theHttpConnector.setPort( httpPort = theHttpPort );
			theHttpConnector.setIdleTimeout(30000);	
		}
		

		final ServerConnector theHttpsConnector = theHttpsPort == null ? null : getHttpsConnector(httpsPort = theHttpsPort, http_config, inCfg);

		// Here you see the server having multiple connectors registered with
		// it, now requests can flow into the server from both http and https
		// urls to their respective ports and be processed accordingly by jetty.
		// A simple handler is also registered with the server so the example
		// has something to pass requests off to.

		// Set the connectors
		Connector[] theConnectors = new Connector[0];
		
		if ( theHttpConnector != null && theHttpsConnector != null ) {
			theConnectors = new Connector[] { theHttpConnector, theHttpsConnector };
		} else if ( theHttpConnector != null ) {
			theConnectors = new Connector[] { theHttpConnector };
		} else if ( theHttpsConnector != null ) {
			theConnectors = new Connector[] { theHttpsConnector };
		}
		
		theServer.setConnectors( theConnectors );
	}

	private ServerConnector getHttpsConnector(Integer inHttpsPort, HttpConfiguration http_config, IConfiguration inCfg) {

		if ( inHttpsPort == null ) { return null; }
		
		final File theKeystore = inCfg.getKeystoreFile();
		final String theKeystorePassword = inCfg.getKeystorePassword();
		final String theKeymanagerPassword = inCfg.getKeymanagerPassword();

		if (theKeystore == null) {
			return null;

		} else if (!theKeystore.isFile()) {

			throw new IllegalArgumentException("keystore is no file: " + theKeystore);

		}

		http_config.setSecureScheme("https");
		http_config.setSecurePort(inHttpsPort);
		http_config.setOutputBufferSize(32768);

		// HTTPS Configuration
		// A new HttpConfiguration object is needed for the next connector and
		// you can pass the old one as an argument to effectively clone the
		// contents. On this HttpConfiguration object we add a
		// SecureRequestCustomizer which is how a new connector is able to
		// resolve the https connection before handing control over to the Jetty
		// Server.
		HttpConfiguration https_config = new HttpConfiguration(http_config);
		SecureRequestCustomizer src = new SecureRequestCustomizer();
		// src.setStsMaxAge(2000);
		// src.setStsIncludeSubDomains(true);
		https_config.addCustomizer(src);

		// SSL Context Factory for HTTPS
		// SSL requires a certificate so we configure a factory for ssl contents
		// with information pointing to what keystore the ssl connection needs
		// to know about. Much more configuration is available the ssl context,
		// including things like choosing the particular certificate out of a
		// keystore to be used.
		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setKeyStorePath(theKeystore.getAbsolutePath());
		sslContextFactory.setKeyStorePassword(theKeystorePassword);
		sslContextFactory.setKeyManagerPassword(theKeymanagerPassword);

		// HTTPS connector
		// We create a second ServerConnector, passing in the http configuration
		// we just made along with the previously created ssl context factory.
		// Next we set the port and a longer idle timeout.
		final ServerConnector theHttpsConnector = new ServerConnector(theServer,
				new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
				new HttpConnectionFactory(https_config));
		theHttpsConnector.setPort( inHttpsPort);
		theHttpsConnector.setIdleTimeout(500000);

		return theHttpsConnector;
	}

	public void start() throws Exception {
		
		Utils.initLog4j( new File(basepath, "log4j.xml") );

		LOG.debug("starting server...");

		application.init( basepath, httpPort, httpsPort, configuration.getArgs() );
		
		LOG.debug("Application: " + application);

		theServer.setStopAtShutdown(true);
		theServer.start();

		LOG.info("listening on http.port=" + httpPort );
		if ( httpsPort != null ) {
			LOG.info("listening on https.port=" + httpsPort );
		}
		
		if ( configuration.getUserCredentials() != null ) {
			LOG.info( "authentification enforced" );
		}

		if (configuration.getServerSecondsToLive() > 0) {

			new Thread() {

				@Override
				public void run() {

					LOG.warn("will terminate at " + LocalDateTime.now().plusSeconds(configuration.getServerSecondsToLive()));

					try {
						Thread.sleep(configuration.getServerSecondsToLive() * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					LOG.info("Timed termination now!");

					ServerMain.this.stop();
				}

			}.start();
		}
	}

	public void stop() {

		try {

			if (!theServer.isStopped() && !theServer.isStopping()) {

				theServer.stop();
				theServer.destroy();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		application.destroy();

	}

	public static void main(String[] args) throws Exception {

		if ( MainUtils.isArg(args, "-h") ) {
			
			System.out.println( "SimpleRestServer "+ MavenInfo.getVersion() );
			System.out.println( MainUtils.buildMainArgText( IConfiguration.class ) );
			
			return;
		}
		
		new ServerMain(args).start();
	}

	public static ServerMain start(final String... inArgs) throws Exception {

		final ServerMain theSm = new ServerMain(inArgs);
		theSm.start();
		return theSm;
	}

	public void join() throws InterruptedException {
		theServer.join();
	}
	
	private class ShutdownListener implements ServletContextListener {

		@Override
		public void contextInitialized(ServletContextEvent sce) {}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			
			LOG.debug( "received shutdown request ..." );
			
			try {
				
				application.destroy();
				
				LOG.debug( "... application shutdown done." );
				
			} catch (Exception e) {
				LOG.error( "Application shutdown failure, will be ignored: "+ e, e );
			}
		}
	}
}
