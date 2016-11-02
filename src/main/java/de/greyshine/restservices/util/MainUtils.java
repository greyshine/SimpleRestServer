package de.greyshine.restservices.util;

import java.io.File;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;


/**
 * Helper for parsing Main args 
 */
public abstract class MainUtils {

	public static String get(String[] args, int index) {
		
		return get(args, index, null);
	}
	
	public static String get(String[] args, int index, String inDefault) {
		
		try {
		
			return args[ index >= 0 ? index : args.length+index ];
			
		} catch (Exception e) {
			// swallow
		}
		return inDefault;
	}

	public static String getAfter(String[] args, String inBefore, String inDefault) {
	
		return getAfter(args, inBefore, null, inDefault);
	}
	
	public static Boolean getAfter(String[] args, String inBefore, Boolean inDefault) {
		
		try {
			
			return Boolean.parseBoolean( getAfter(args, inBefore, (String)null) );
			
		} catch (Exception e) {
			// swallow
		}
		
		return inDefault;
	}
	
	public static Integer getAfter(String[] args, String inBefore, Integer inDefault) {
		
		try {
			
			return Integer.parseInt( getAfter(args, inBefore, (String)null) );
			
		} catch (Exception e) {
			
			return inDefault;
		}
		
	}

	public static Integer getAfter(String[] args, String inBefore, String inRegex, Integer inDefault) {
		
		try {
			
			return Integer.parseInt( getAfter(args, inBefore, inRegex, (String)null) );
			
		} catch (Exception e) {
			
			return inDefault;
		}
		
	}
	
	public static String getAfter(String[] args, String inBefore, String inRegex, String inDefault) {
		
		try {

			for (int i = 0; i < args.length; i++) {
				if ( inBefore.equals( args[i] ) && ( inRegex == null || args[i+1].matches( inRegex ) ) ) {
					return args[i+1];
				}
			}
			
			
		} catch (Exception e) {
			// swallow
		}
		return inDefault;
	}

	public static boolean isMatching(String[] args, int index, String inRegex) {
		
		try {
			
			return get(args, index).matches( inRegex );
			
		} catch (Exception e) {
			// finally
		}
		
		return false;
	}

	public static File getAfter(String[] args, String inBefore, File inDefault, boolean isAllowNotExisting, boolean isAllowFile, boolean isAllowDir) {
		
		final String thePath = getAfter( args, inBefore, (String)null );
		
		if ( Utils.isBlank( thePath ) ) { return inDefault; }
		
		final File theFile = new File( thePath );
		
		if ( !theFile.exists() && isAllowNotExisting ) {
			return theFile;
		}
		if ( theFile.isFile() && isAllowFile ) {
			return theFile;
		}
		if ( theFile.isDirectory() && !isAllowDir ) {
			return theFile;
		}
		
		return inDefault;
	}

	public static boolean isArg(String[] inArgs, String inArg) {
		
		if ( inArgs == null ) { return false; }
		for (String anArg : inArgs) {
			
			if ( anArg == inArg ) { return true; }
			else if ( inArg != null && inArg.equals( anArg ) ) { return true; }
			
		}
		
		return false;
	}
	
	public static String buildMainArgText(Class<?> inClass) {
		
		if ( inClass == null ) { return ""; }

		final StringBuilder sb = new StringBuilder();

		for( Method aMethod : inClass.getDeclaredMethods() ) {
			
			final MainArg theMa = aMethod.getDeclaredAnnotation( MainArg.class );
			if ( theMa == null ) { continue; }
			
			sb.append( '\n' )//
			.append( theMa.value() )//
			.append( "\n\n\t" )//
			.append( theMa.description().replace( "\n" , "\n\t") )//
			.append( "\n\t" )//
			.append( theMa.isMandatory() ? "(mandatory)" : "(optional)" )//
			.append( "\n" );
		}
		
		return sb.toString();
	}
	
	/**
	 * 
	 * 
	 *
	 */
	@Retention( RetentionPolicy.RUNTIME )
	@Target( ElementType.METHOD )
	public @interface MainArg {

		String value();
		boolean isMandatory();
		String description();
		
	}
	
}
