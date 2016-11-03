package de.greyshine.restservices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
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

import com.google.gson.JsonObject;

import de.greyshine.restservices.Application.IServletFilterInfo;
import de.greyshine.restservices.filters.RequestContextFilter;
import de.greyshine.restservices.util.JsonUtils;
import de.greyshine.restservices.util.MainUtils;
import de.greyshine.restservices.util.MavenInfo;
import de.greyshine.restservices.util.Utils;
import de.greyshine.restservices.util.Utils.Kvp;
import de.greyshine.restservices.web.WebAdminFilter;

/**
 * 
 * @author Dirk Schumacher
 */
public class ServerMain {

	static final Log LOG = LogFactory.getLog(ServerMain.class);
	
	static final int DEFAULT_PORT = 7777;

	private final Server server;
	private Integer httpPort;
	private Integer httpsPort;

	private final Application application;
	private final File basepath;
	private final IConfiguration configuration;

	{
		LOG.debug("Hello !-)");
	}

	public ServerMain(final String... inArgs) throws Exception {
		
		this( buildConfiguration(inArgs) );
	}

	public ServerMain(IConfiguration inCfg) throws Exception {
		
		if ( inCfg == null ) {
			throw new IllegalArgumentException("configuration is null");
		}
		
		configuration = inCfg;

		basepath = Utils.getCanonicalFileSafe(configuration.getBasepath(), true);
		
		if ( !basepath.exists() ) {
			basepath.mkdirs();
		}
		
		LOG.warn( "basepath: "+ basepath );

		if (!basepath.isDirectory()) {
			throw new IllegalStateException("basepath is no directory: " + basepath);
		}

		// copy default log4j.xml if not exists
		initLog4J();
		
		Integer theHttpPort = inCfg.getHttpPort();
		Integer theHttpsPort = inCfg.getHttpsPort();
		
		theHttpPort = theHttpPort == null || theHttpPort < 1 ? null : theHttpPort; 
		theHttpsPort = theHttpsPort == null || theHttpsPort < 1 ? null : theHttpsPort; 

		if (theHttpPort == null && theHttpsPort == null) {
			theHttpPort = DEFAULT_PORT;
		}

		application = initApplication( inCfg );
		
		server = new Server();

		final StringBuilder theClassNames = new StringBuilder();
		for (Class<?> aClass : application.getHandlerClasses()) {

			theClassNames.append(theClassNames.length() == 0 ? "" : ",");
			theClassNames.append(aClass.getCanonicalName());

			LOG.debug("announced class: " + aClass.getCanonicalName());
		}
		
		// https://nikolaygrozev.wordpress.com/2014/10/16/rest-with-embedded-jetty-and-jersey-in-a-single-jar-step-by-step/
		final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		server.setHandler(context);
		context.setAttribute(Application.SERVLETCONTEXT_KEY, application);
		context.setAttribute(IConfiguration.SERVLETCONTEXT_KEY, configuration);
		context.addFilter(new FilterHolder(new RequestContextFilter()), "/*", EnumSet.allOf(DispatcherType.class));
		context.addEventListener(new ShutdownListener());

		final ServletHolder theJerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class,
				"/*");
		theJerseyServlet.setInitOrder(0);
		theJerseyServlet.setInitParameter("jersey.config.server.provider.classnames", theClassNames.toString());

		final Set<IServletFilterInfo> theServletFilterInfos = Utils.defaultIfNull(application.getServletFilters(),
				Collections.emptySet());

		for (IServletFilterInfo aServletFilterInfo : theServletFilterInfos) {
			final EnumSet<DispatcherType> theDts = Utils.defaultIfNull(aServletFilterInfo.getDispatcherTypes(),
					EnumSet.allOf(DispatcherType.class));
			final String thePath = Utils.defaultIfBlank(aServletFilterInfo.getPath(), "/*");
			context.addFilter(new FilterHolder(aServletFilterInfo.getFilter()), thePath, theDts);
		}

		if (inCfg.isWebAdminEnabled()) {

			final FilterHolder theWebGuiHolder = context.addFilter(WebAdminFilter.class,
					WebAdminFilter.URI_PREFIX + "/*", EnumSet.allOf(DispatcherType.class));
			theWebGuiHolder.setInitParameters(new HashMap<>());
			LOG.info("enabling webadmin gui");
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

		if (theHttpPort != null) {
			theHttpConnector = new ServerConnector(server, new HttpConnectionFactory(http_config));
			theHttpConnector.setPort(httpPort = theHttpPort);
			theHttpConnector.setIdleTimeout(30000);
		}

		final ServerConnector theHttpsConnector = theHttpsPort == null ? null
				: getHttpsConnector(httpsPort = theHttpsPort, http_config, inCfg);

		// Here you see the server having multiple connectors registered with
		// it, now requests can flow into the server from both http and https
		// urls to their respective ports and be processed accordingly by jetty.
		// A simple handler is also registered with the server so the example
		// has something to pass requests off to.

		// Set the connectors
		Connector[] theConnectors = new Connector[0];

		if (theHttpConnector != null && theHttpsConnector != null) {
			theConnectors = new Connector[] { theHttpConnector, theHttpsConnector };
		} else if (theHttpConnector != null) {
			theConnectors = new Connector[] { theHttpConnector };
		} else if (theHttpsConnector != null) {
			theConnectors = new Connector[] { theHttpsConnector };
		}

		server.setConnectors(theConnectors);
	}

	private Application initApplication(IConfiguration inCfg) throws InstantiationException, IllegalAccessException {
		
		return inCfg.getApplicationClass().newInstance();
	}

	private void initLog4J() {

		final File theFileLog4j = new File(basepath, "log4j.xml");

		if (!theFileLog4j.exists()) {

			try {

				Utils.copySafe(Utils.getResource("log4j.xml"), new FileOutputStream(theFileLog4j), true, true);

			} catch (Exception e) {
				// swallow
			}
		}

		if (theFileLog4j.isFile()) {

			Utils.initLog4j(theFileLog4j);
		}
	}

	private ServerConnector getHttpsConnector(Integer inHttpsPort, HttpConfiguration http_config,
			IConfiguration inCfg) {

		if (inHttpsPort == null) {
			return null;
		}

		final File theKeystore = Utils.toCanonicalFileSafe( inCfg.getKeystoreFile() );
		final String theKeystorePassword = inCfg.getKeystorePassword();
		final String theKeymanagerPassword = inCfg.getKeymanagerPassword();

		if ( !Utils.isFile( theKeystore ) ) {
			
			return null;
		} 
		
		LOG.debug( "keystore: "+ theKeystore );

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
		final ServerConnector theHttpsConnector = new ServerConnector(server,
				new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
				new HttpConnectionFactory(https_config));
		theHttpsConnector.setPort(inHttpsPort);
		theHttpsConnector.setIdleTimeout(500000);

		return theHttpsConnector;
	}

	public void start() throws Exception {

		Utils.initLog4j(new File(basepath, "log4j.xml"));

		LOG.debug("starting server...");

		application.init(basepath, httpPort, httpsPort, configuration.getArgs());

		LOG.debug("Application: " + application);

		server.setStopAtShutdown(true);
		server.start();

		if (configuration.getUserCredentials() != null) {
			LOG.debug("authentification enforced");
		}

		if (configuration.getServerSecondsToLive() > 0) {

			new Thread() {

				@Override
				public void run() {

					LOG.warn("will terminate at "
							+ LocalDateTime.now().plusSeconds(configuration.getServerSecondsToLive()));

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

		if (application.getHttpAddress() != null) {
			LOG.info("http.webaddress=" + application.getHttpAddress());
		}
		
		if (application.getHttpsAddress() != null) {
			LOG.info("https.webaddress=" + application.getHttpsAddress());
		}
	}

	public void stop() {

		LOG.debug("Stopping server...");
		
		try {

			if (!server.isStopped() && !server.isStopping()) {

				server.stop();
				server.destroy();
			}

		} catch (Exception e) {
			LOG.error("Stopping server: "+ e, e);
		}

		try {
			
			application.destroy();

		} catch (Exception e) {
			
			LOG.error("application.destroy: "+ e, e);
		}
		
		LOG.error("Server stopped.");
	}

	public static void main(String[] args) throws Exception {

		if (MainUtils.isArg(args, "-h")) {

			System.out.println("SimpleRestServer " + MavenInfo.getVersion());
			System.out.println(MainUtils.buildMainArgText(IConfiguration.class));

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
		server.join();
	}

	private class ShutdownListener implements ServletContextListener {

		@Override
		public void contextInitialized(ServletContextEvent sce) {
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {

			LOG.debug("received shutdown request ...");

			try {

				application.destroy();

				LOG.debug("... application shutdown done.");

			} catch (Exception e) {
				LOG.error("Application shutdown failure, will be ignored: " + e, e);
			}
		}
	}
	
	private static IConfiguration buildConfiguration(String[] inArgs) {
	
		// check 
		String theConfigJson = MainUtils.getAfter(inArgs, "-config.json", (String)null);
		File theJsonFile = Utils.isBlank( theConfigJson ) ? null : new File( theConfigJson );
		theJsonFile = theJsonFile == null || !theJsonFile.isFile() ? null : theJsonFile;
		
		if ( theJsonFile != null ) {
			JsonObject theJson = null;
			try {
				theJson = JsonUtils.readJsonObject(theJsonFile);
			} catch (IOException e) {
				
				throw Utils.toRuntimeException(e);
			}
			
			LOG.debug( "using config.json: "+ theJsonFile );
			return new JsonConfiguration( theJson );
		}
		
		// TODO: load default config.json in current home folder
		return new ArgsConfiguration(inArgs);
	}
	
	private static class JsonConfiguration implements IConfiguration {
		
		final JsonObject json;
		//final Utils.Kvp<String, String> userCredentials;
		
		public JsonConfiguration(JsonObject inJo) {
			json = inJo;
		}

		@Override
		public File getBasepath() {
			
			File theBasepath = new File(".").getAbsoluteFile();
			
			try {
				
				theBasepath = new File( json.get( "path" ).getAsString() );
				
			} catch (Exception e) {
				// swallow
			}
			
			return theBasepath != null && !theBasepath.isFile() ? theBasepath : Utils.getCanonicalFileSafe( new File(".") ) ;
		}

		@Override
		public String[] getArgs() {
			
			final List<String> theArgs = new ArrayList<>(); 
			
			theArgs.add( "-path" );
			theArgs.add( getBasepath().getAbsolutePath() );
			
			Integer n = getHttpPort();
			if ( n != null ) {
				
				theArgs.add( "-path" );
			}
			
			return theArgs.toArray( new String[theArgs.size()] );
		}

		@Override
		public Integer getHttpPort() {
			try {
				return json.get( "port.http" ).getAsInt();
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public Integer getHttpsPort() {
			try {
				return json.get( "port.https" ).getAsInt();
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public int getServerSecondsToLive() {
			try {
				final int ttl = json.get( "ttl" ).getAsInt();
				return ttl < 1 ? -1 : ttl;
			} catch (Exception e) {
				return -1;
			}
		}

		@Override
		public File getKeystoreFile() {
			String theFile = null;
			try {
				theFile = json.get( "keystore" ).getAsString();
			} catch (Exception e) {
				// swallow
			}
			return theFile == null ? null : new File( theFile );
		}

		@Override
		public String getKeystorePassword() {
			try {
				return json.get( "pwd.keystore" ).getAsString();
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public String getKeymanagerPassword() {
			try {
				return json.get( "pwd.keymanager" ).getAsString();
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public boolean isWebAdminEnabled() {
			try {
				return Boolean.TRUE.equals( json.get( "webadmin" ).getAsBoolean() );
			} catch (Exception e) {
				return false;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends Application> getApplicationClass() {
			
			String theClass = null;
			
			try {
				
				theClass = json.get( "application.class" ).getAsString();
				
			} catch (Exception e) {
				// swallow
			}
			
			theClass = Utils.defaultIfBlank(theClass, DefaultApplication.class.getCanonicalName());
			
			try {
			
				return (Class<? extends Application>) Thread.currentThread().getContextClassLoader().loadClass( theClass );
			
			} catch (ClassNotFoundException e) {
				
				throw Utils.toRuntimeException(e);
			}
		}

		@Override
		public long maxUploadSize() {
			
			long maxUploadSize = DEFAULT_MAX_UPLOAD_SIZE;
			
			try {
				
				maxUploadSize = Long.parseLong( json.get( "" ).getAsString() );
				
			} catch (Exception e) {
				// swallow
			}
			
			return maxUploadSize < 1 ? DEFAULT_MAX_UPLOAD_SIZE : maxUploadSize;
		}

		@Override
		public Kvp<String, String> getUserCredentials() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private static class ArgsConfiguration implements IConfiguration {

		final String[] inArgs;
		final Utils.Kvp<String, String> userCredentials;
		
		public ArgsConfiguration(String[] inArgs) {

			this.inArgs = inArgs == null ? new String[0] : Arrays.copyOf(inArgs, inArgs.length);

			Utils.Kvp<String, String> theUserCredentials = null;

			final String theCredentials = MainUtils.getAfter(inArgs, "-user", (String) null);

			if (Utils.isNotBlank(theCredentials)) {

				try {

					final int idxColon = theCredentials.indexOf(":");

					final String theUser = theCredentials.substring(0, idxColon);
					final String thePassword = theCredentials.substring(idxColon + 1);

					theUserCredentials = new Kvp<>(theUser, thePassword);

				} catch (Exception e) {

					throw new IllegalArgumentException(
							"bad parameter -user: " + MainUtils.getAfter(inArgs, "-user", ""));
				}
			}

			userCredentials = theUserCredentials;

		}

		@Override
		public String[] getArgs() {
			return inArgs;
		}

		@Override
		public Integer getHttpPort() {
			return MainUtils.getAfter(inArgs, "-port.http", (Integer) null);
		}

		@Override
		public Integer getHttpsPort() {
			return MainUtils.getAfter(inArgs, "-port.https", (Integer) null);
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
			
			String theClass = MainUtils.getAfter(inArgs, "-application.class", (String)null);
			theClass = Utils.defaultIfBlank(theClass, DefaultApplication.class.getCanonicalName());
			
			try {
			
				return (Class<? extends Application>) Thread.currentThread().getContextClassLoader().loadClass( theClass );
			
			} catch (ClassNotFoundException e) {
				
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

	}
}
