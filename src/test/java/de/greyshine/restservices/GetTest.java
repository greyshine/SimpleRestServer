package de.greyshine.restservices;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.greyshine.restservices.client.Client.Response;

public class GetTest extends AbstractTest {

	@Test
	public void testGets() throws IOException {
		
		for (int i = 0; i < 2; i++) {
		
			clientFactory.createPost( "tests" )//
				.headerContentTypeJson()//
				.setEntityParameter( "{test:\"gets\"}" )//
				.send();
		}
		
		Response r = clientFactory.createGet("tests")
				.send();
		
		Assert.assertEquals( "bad status: "+ r.statusLine, 200, r.statusCode );
		System.out.println( r.consumeText() );
		
		r = clientFactory.createGet("tests")//
				.addGetParameter("_embed", "true")
				.send();
		
		Assert.assertEquals( "bad status: "+ r.statusLine, 200, r.statusCode );
		System.out.println( r.consumeText() );
		
	}
}
