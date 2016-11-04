package de.greyshine.restservices.client;

import com.google.gson.JsonElement;

import de.greyshine.restservices.Method;
import de.greyshine.restservices.RequestInfo;
import de.greyshine.restservices.client.Client.Response;
import de.greyshine.restservices.util.Utils;

public class ClientFactory {
	
	private String address;
	private boolean isVerbose = false;
	private String user, password;
	
	public ClientFactory(String inAddress) {
		this(inAddress, null,null);
	}
	
	public ClientFactory(String inAddress, String inUser, String inPassword) {
		this.address = inAddress;
		user = inUser;
		password = Utils.defaultIfNull( inPassword , "");
	}
	
	public Client create(String inUri) {
		
		inUri = inUri == null || inUri.startsWith("/") ? inUri : "/"+inUri;
		
		if ( inUri != null && !Utils.isFirstChar( inUri , '/'  ) ) {
			inUri = "/"+ inUri;
		}
		
		final String theAddress = inUri == null ? address : address+ inUri;
		
		final Client c = new Client(theAddress);
		
		if ( Utils.isNotBlank(user) ) {
			
			c.credentials(user, password);
		}
		
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

	public Response sendPost(String inCollection, JsonElement inJe) {
		return createPost( inCollection ).headerContentTypeJson().setEntityParameter( inJe ).send();
	}
}
