package de.greyshine.restservices;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import de.greyshine.restservices.util.Utils;

public class RequestInfo {
	
	public static String OFFSET = "_offset";
	public static String LENGTH = "_length";
	public static String EMBED = "_embed";
	public static String PRETTYPRINT = "_pretty";
	public static String SORT = "_sort";
	public static String JSONP = "_jsonp";
	public static String TIME_TO_LIVE = "_ttl";
	public static String ENVELOPE = "_envelope";
	public static String PAGE = "_page";
	public static String ITEMS = "_per_page";
	public static String VERBOSE = "_verbose";
	
	private final HttpServletRequest req;
	private List<String> sortings;
	private List<String> selects;

	public RequestInfo(HttpServletRequest inRequest) {
		req = inRequest;
	}
	
	public List<String> getSortings(String inPropertyNameRegex) {
		
		inPropertyNameRegex = Utils.defaultIfBlank( inPropertyNameRegex, Constants.REGEX_PROPERTYNAME_SORTABLE);
		
		if ( sortings == null ) {
			
			sortings = new ArrayList<>();
			
			try {
				
				for (String aSortIndicator : req.getParameter( SORT ).split( ",", -1 ) ) {
					
					aSortIndicator = Utils.trimToEmpty( aSortIndicator );
					
					if ( aSortIndicator.isEmpty() || !aSortIndicator.matches( inPropertyNameRegex ) ) { continue; }
					aSortIndicator = aSortIndicator.charAt( 0 ) != '-' ? '+'+aSortIndicator : aSortIndicator;
					
					sortings.add( aSortIndicator );
				}
				
			} catch (Exception e) {
				// swallow
			}
		}
		
		return sortings;
	}
	
	public List<String> getSelects(String inPropertyNameRegex) {
		
		inPropertyNameRegex = Utils.defaultIfBlank( inPropertyNameRegex, "[a-zA-Z0-9]+");
		
		if (selects == null) {
			
			selects = new ArrayList<>();
			
			// TODO
			try {
				
				for (String aSortIndicator : req.getParameter( SORT ).split( ",", -1 ) ) {
					
					aSortIndicator = Utils.trimToEmpty( aSortIndicator );
					
					if ( aSortIndicator.isEmpty() || !aSortIndicator.matches( inPropertyNameRegex ) ) { continue; }
					
					sortings.add( aSortIndicator );
				}
				
			} catch (Exception e) {
				// swallow
			}
			
		}
		
		
		return selects;
	}
	
	public boolean isEmbed() {
		
		final String embed = req.getParameter( EMBED );
		
		return embed != null && !"false".equalsIgnoreCase( embed );
	}
	public boolean isPrettyPrint() {
		return !"false".equalsIgnoreCase( req.getParameter( PRETTYPRINT ) );
	}
	public boolean isEnvelope() {
		
		final String envelope = req.getParameter( ENVELOPE );
		return envelope != null && !"false".equalsIgnoreCase( envelope );
	}
	
	public Long getTimeToLive() {
		return Utils.parseLongSafe( req.getParameter( TIME_TO_LIVE ) , null );
	}

	public Long getOffset() {
		
		final Long v = Utils.parseLongSafe( req.getParameter( OFFSET ) , null );
		
		return v == null ? null : Math.max( 0 , v );
	}

	public Long getLength() {
		
		final Long v = Utils.parseLongSafe( req.getParameter( LENGTH ) , null );
		
		return v == null ? null : Math.max( 0 , v );
	}

	public boolean isVerbose() {
		return "true".equalsIgnoreCase( req.getParameter( VERBOSE ) );
	}

	public boolean isHeaderValue(String inHeader, String inValue) {

		final Enumeration<String> theEnum = req.getHeaders( inHeader );
		if ( theEnum == null ) { return false; }
		
		while( theEnum.hasMoreElements() ) {
			
			for( String aValue : Utils.defaultIfNull( theEnum.nextElement(), "").split( ",", -1 ) ) {
				
				if ( aValue != null && aValue.trim().equals( inValue ) ) { return true; }
			} 
		}
		
		return false;
	}
	
}
