package de.greyshine.restservices.client;

import de.greyshine.restservices.Method;
import de.greyshine.restservices.RequestInfo;
import de.greyshine.restservices.util.Utils;

public class ClientFactory {
	
	private String address;
	private boolean isVerbose = true;
	
	public ClientFactory(String inAddress) {
		this.address = inAddress;
	}
	
	public Client create(String inUri) {
		
		inUri = inUri == null || inUri.startsWith("/") ? inUri : "/"+inUri;
		
		if ( inUri != null && !Utils.isFirstChar( inUri , '/'  ) ) {
			inUri = "/"+ inUri;
		}
		
		final String theAddress = inUri == null ? address : address+ inUri;
		
		final Client c = new Client(theAddress);
		
		if ( isVerbose ) {
			
			c.addGetParameter( RequestInfo.VERBOSE, "true");
		}
		
		return c;
	}
	
	public Client createGet(String inUri) {
		return create(inUri).method( Method.GET );
	}
	
	public Client createPost(String inUri) {
		return create(inUri).method( Method.POST );
	}

	public Client createPut(String inUri) {
		return create(inUri).method( Method.PUT );
	}
	
	public Client createPatch(String inUri) {
		return create( inUri ).method( Method.PATCH );
	}

	public ClientFactory verbose() {
		
		isVerbose = true;
		return this;
	}

	public Client create(Method inMethod, String inUri) {
		return create(inUri).method( inMethod );
	}
}
