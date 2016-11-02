package de.greyshine.restservices;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;

import de.greyshine.restservices.client.Client.Response;

public class MetaTest extends AbstractTest {

	@Test
	public void testPing() throws Exception {
		
		final Response r = clientFactory.create( Method.META, "ping" )
				.send();
		
		Assert.assertTrue( String.valueOf( r.exception ), r.exception == null );
		Assert.assertEquals( "bad status: "+ r.statusLine, 200, r.statusCode );
		
		System.out.println( r.consumeText() );
		
	}

	@Test
	public void testStatus() throws Exception {
		
		final Response r = clientFactory.create( Method.META, "status" )
				.send();
		
		Assert.assertTrue( String.valueOf( r.exception ), r.exception == null );
		Assert.assertEquals( "bad status: "+ r.statusLine, 200, r.statusCode );
		
		System.out.println( r.consumeText() );
		
		final JsonObject jo = r.getJsonObject();
		
		Assert.assertEquals(String.valueOf( jo.get( "application" ) ), DefaultApplication.class.getCanonicalName(), jo.get( "application" ).getAsString() );
		
	}
	
}
