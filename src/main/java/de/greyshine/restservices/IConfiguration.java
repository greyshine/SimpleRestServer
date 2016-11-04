package de.greyshine.restservices;

import java.io.File;

import de.greyshine.restservices.util.MainUtils.MainArg;
import de.greyshine.restservices.util.Utils;

/**
 * Configuration for an application implementation. 
 *
 */
public interface IConfiguration {
	
	String SERVLETCONTEXT_KEY = IConfiguration.class.getCanonicalName();

	/**
	 * 100MB
	 */
	long DEFAULT_MAX_UPLOAD_SIZE = 100 * Utils.MB_IN_BYTES;

	String[] getArgs();
	
	@MainArg( value="-port.http [portnumber]", isMandatory=false, description="Port on which HTTP will listen.\nIf no http and no https port are set server will listen for http requests on port 7777." )
	Integer getHttpPort();
	@MainArg( value="-port.https [portnumber]", isMandatory=false, description="Port on which HTTPS will listen.\nIf none set server will not listen for https requests." )
	Integer getHttpsPort();
	
	int getServerSecondsToLive();
	
	@MainArg( value="-path [file]", isMandatory=false, description="relative path to the home dir of the application.\nIf none set the calling path will be used as homepath." )
	File getBasepath();

	@MainArg( value="-keystore [file]", isMandatory=false, description="Keystorefile. See http://www.eclipse.org/jetty/documentation/current/configuring-ssl.html" )
	File getKeystoreFile();
	
	@MainArg( value="-pwd.keystore [password]", isMandatory=false, description="Keystorefile. See http://www.eclipse.org/jetty/documentation/current/configuring-ssl.html" )
	String getKeystorePassword();
	@MainArg( value="pwd.keymanager [password]", isMandatory=false, description="defaults to the -pwd.keystore" )
	String getKeymanagerPassword();
	
	boolean isWebAdministrationEnabled();

	@MainArg( value="[fully qualified class]", isMandatory=false, description="As last argument provide the fully qulified class of the Application you want to run.\nThe class must extend de.greyshine.restservices.Application." )
	Class<? extends Application> getApplicationClass();
	
	/**
	 * the maximal file upload bytes allowed
	 * @return
	 */
	long maxUploadSize();
	
	Utils.Kvp<String,String> getUserCredentials();
	
}
