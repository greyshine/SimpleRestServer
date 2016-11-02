package de.greyshine.restservices.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class MavenInfo {
	
	final Log LOG = LogFactory.getLog( MavenInfo.class );

	public static final Properties properties = new Properties();
	
	static {
		
		InputStream theIs = Thread.currentThread().getContextClassLoader().getResourceAsStream( "maven-info.properties" );
		
		try {
			properties.load( theIs );
		} catch (IOException e) {
			// swallow
		} finally {
			Utils.close( theIs );
		}
	}
	
	public static String getVersion() {
		
		return properties.getProperty( "version" )+" ("+ properties.getProperty( "build.date" ) +")";
	} 

}
