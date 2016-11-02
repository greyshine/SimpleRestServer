package de.greyshine.restservices;


import java.io.IOException;

import org.junit.Test;

import de.greyshine.restservices.client.Client;
import de.greyshine.restservices.client.Client.Response;
import de.greyshine.restservices.util.Utils;
import junit.framework.Assert;

public class FileUploadTest extends AbstractTest {

	@Test
	public void testUpload() throws IOException {
		
		String data = String.valueOf( Utils.getGmtTime() );
		
		Client c = clientFactory.createPost("file");
		c.setEntityParameter( data );
		Response r = c.send();
		
		Assert.assertEquals( 200 , r.statusCode);
		
		final String theId = r.consumeJsonObject().get( "id" ).getAsString();
		
		Client c2 = clientFactory.createGet("file/"+ theId);
		Response r2 = c2.send();
		
		System.out.println( r2 );
		
		String theResponseText = r2.consumeText(); 
		
		System.out.println( "TEXT1: "+ theResponseText );
		
		Assert.assertEquals( data, theResponseText );
	}
	
	@Test
	public void testPut() throws IOException {
		
		String data = String.valueOf( Utils.getGmtTime() );
		
		Client c = clientFactory.createPost("file");
		c.setEntityParameter( data );
		Response r = c.send();
		
		Assert.assertEquals( 200 , r.statusCode);
		
		final String theId = r.consumeJsonObject().get( "id" ).getAsString();
		
		data += " - updated";
		
		c = clientFactory.createPut("file/"+ theId);
		c.setEntityParameter( data );
		r = c.send();
		
		Assert.assertEquals(r.toString(),  200, r.statusCode );
		
		c = clientFactory.createGet("file/"+ theId);
		
		final String theExpected = data;
		final String theResult = c.send().consumeText();
		
		System.out.println( theId );
		System.out.println( theExpected );
		System.out.println( theResult );
		
		Assert.assertEquals( theExpected, theResult );
		
		
	}

	
}
