package de.greyshine.restservices;

public abstract class HttpHeader {

	private HttpHeader() {
		
	}
	
	public static final String CONTENT_TYPE = "Content-Type"; 
	public static final String CONTENT_ENCODING = "Content-Encoding";
	public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
	public static final Object ACCEPT_ENCODING = "Accept-Encoding"; 
	
}
