package de.greyshine.restservices;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import de.greyshine.restservices.filebased.BinaryFileStorage;
import de.greyshine.restservices.filebased.JsonFileStorage;
import de.greyshine.restservices.filters.AuthentificationFilter;
import de.greyshine.restservices.filters.GzipFilter;
import de.greyshine.restservices.handlers.DeleteHandler;
import de.greyshine.restservices.handlers.FileHandler;
import de.greyshine.restservices.handlers.GetHandler;
import de.greyshine.restservices.handlers.MetaHandler;
import de.greyshine.restservices.handlers.PatchHandler;
import de.greyshine.restservices.handlers.PostHandler;
import de.greyshine.restservices.handlers.PutHandler;
import de.greyshine.restservices.interceptors.GzipInterceptor;
import de.greyshine.restservices.util.Job;
import de.greyshine.restservices.util.JsonUtils;
import de.greyshine.restservices.util.MainUtils;
import de.greyshine.restservices.util.MavenInfo;
import de.greyshine.restservices.util.Utils;

public abstract class Application implements IStatusReportable {

	static final Log LOG = LogFactory.getLog(Application.class);

	public static final String SERVLETCONTEXT_KEY = Application.class.getCanonicalName();

	private File basepath = Utils.getCanonicalFileSafe(new File("."), true);

	private final long starttime = System.currentTimeMillis();

	private IBinaryStorageService binaryStorageService;
	private IJsonStorageService jsonStorageService;
	
	private String httpAddress;
	private Integer httpPort;
	private String httpsAddress;
	private Integer httpsPort;
	
	public File getBasepath() {
		return basepath;
	}
	
	public IBinaryStorageService getBinaryStorageService() {
		return binaryStorageService;
	}
	
	public IJsonStorageService getDocumentStorageService() {
		return jsonStorageService;
	}
	
	public String createUniqueId() {
		return ZonedDateTime.now().format( Utils.DF_LEX )+"-"+ UUID.randomUUID().toString();
	}

	public final Collection<Class<?>> getHandlerClasses() {

		final List<Class<?>> theClasses = new ArrayList<>();

		theClasses.add(GetHandler.class);
		theClasses.add(PostHandler.class);
		theClasses.add(PutHandler.class);
		theClasses.add(PatchHandler.class);
		theClasses.add(DeleteHandler.class);
		theClasses.add(FileHandler.class);
		theClasses.add(MetaHandler.class);

		theClasses.addAll(getClasses());

		theClasses.add(GzipInterceptor.class);
		theClasses.add(GzipFilter.class);
		theClasses.add(AuthentificationFilter.class);

		return theClasses;
	}

	public abstract Collection<? extends Class<?>> getClasses();

	public String getVersion() {
		return MavenInfo.getVersion();
	}
	
	public final String getHttpAddress() {
		return httpAddress;
	}
	public final Integer getHttpPort() {
		return httpPort;
	}
	public final String getHttpsAddress() {
		return httpsAddress;
	}
	public final Integer getHttpsPort() {
		return httpsPort;
	}
	
	@Override
	public JsonObject createStatusReport() {
		
		final Job theJob = new Job();
		
		theJob.put("application", getClass().getCanonicalName() );
		theJob.put("version", getVersion());
		theJob.put("application-info", toString() );
		theJob.put("started", LocalDateTime.ofInstant(Instant.ofEpochMilli(starttime), ZoneId.systemDefault())
				.format(DateTimeFormatter.ISO_DATE_TIME));
		theJob.put("running", Utils.getReadableTime(System.currentTimeMillis() - starttime));
		theJob.put("time", ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
		theJob.put( "http" , getHttpAddress() );
		theJob.put( "https" , getHttpsAddress() );
		
		try {
			theJob.put( IBinaryStorageService.class.getCanonicalName(), binaryStorageService instanceof IStatusReportable ? ( (IStatusReportable)binaryStorageService ).createStatusReport() : new JsonPrimitive( binaryStorageService.toString() ) );	
		} catch (Exception e) {
			theJob.put( IBinaryStorageService.class.getCanonicalName(), JsonUtils.toJsonObject(e) );
		}

		try {
			theJob.put( IJsonStorageService.class.getCanonicalName(), jsonStorageService instanceof IStatusReportable ? ( (IStatusReportable)jsonStorageService ).createStatusReport() : new JsonPrimitive( jsonStorageService.toString() ) );	
		} catch (Exception e) {
			theJob.put( IJsonStorageService.class.getCanonicalName(), JsonUtils.toJsonObject(e) );
		}
		
		return theJob.build();
	}

	public final void init(File inBasepath, Integer inHttpPort, Integer inHttpsPort, String... inArgs) throws Exception {

		inBasepath = Utils.defaultIfNull(inBasepath, new File("."));
		inBasepath = inBasepath.isDirectory() ? inBasepath : new File(".");
		inBasepath = Utils.getCanonicalFileSafe(inBasepath, true);

		basepath = inBasepath;
		LOG.debug("basepath: " + basepath);
		
		String theWebAddress = MainUtils.getAfter(inArgs, "-webaddress", "127.0.0.1");
		
		httpPort = inHttpPort;
		httpAddress = httpPort == null ? null : "http://"+ theWebAddress +":"+ httpPort;
		httpsPort = inHttpsPort;
		httpsAddress = httpsPort == null ? null : "https://"+ theWebAddress +":"+ httpsPort;

		initServices(inBasepath, inArgs);
		
		init(basepath, inArgs);
	}

	private void initServices(File inBasepath, String... inArgs) throws Exception {
		
		final Class<? extends IBinaryStorageService> theBinaryStorageClass = Utils.defaultIfNull( getBinaryStorageServiceClass(), BinaryFileStorage.class);
		binaryStorageService = theBinaryStorageClass.newInstance();
		
		final Class<? extends IJsonStorageService> theStorageClass = Utils.defaultIfNull( getJsonStorageServiceClass(), JsonFileStorage.class);
		jsonStorageService = theStorageClass.newInstance(); 
	}

	private Class<? extends IJsonStorageService> getJsonStorageServiceClass() {
		return JsonFileStorage.class;
	}

	private Class<? extends IBinaryStorageService> getBinaryStorageServiceClass() {
		return BinaryFileStorage.class;
	}

	public void init(File inBasepath, String[] inArgs) {
		
		binaryStorageService.init(inBasepath, inArgs);
		jsonStorageService.init(inBasepath, inArgs);
	}

	public void destroy() {

		LOG.debug( "destroying: "+ this );
	}

	public JsonObject createResponseJson(int inStatus, String inErrorCode, String inErrorMessage, JsonElement inData) {

		final JsonObject j = new JsonObject();
		j.addProperty("status", inStatus);

		if (inErrorCode != null) {

			JsonObject e = new JsonObject();
			j.add("error", e);
			e.addProperty("code", inErrorCode);
			e.addProperty("message", inErrorMessage);
		}

		if (inData != null) {
			j.add("data", inData);
		}

		return j;

	}

	public JsonElement createResponseJsonError(int inStatus, String inErrorCode, String inErrorMessage) {

		return createResponseJson(inStatus, inErrorCode, inErrorMessage, null);
	}

	public Set<IServletFilterInfo> getServletFilters() {
		return Collections.emptySet();
	}

	public static interface IServletFilterInfo {
		
		String getPath();

		Filter getFilter();

		EnumSet<DispatcherType> getDispatcherTypes();
	}
	
	public void shutdown() {}
	
	@Override
	public String toString() {
		return getClass().getCanonicalName() + " [path="+ basepath +"]";
	}
}
