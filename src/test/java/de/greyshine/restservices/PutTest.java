package de.greyshine.restservices;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import de.greyshine.restservices.client.Client;
import de.greyshine.restservices.client.Client.Response;
import de.greyshine.restservices.util.Utils;

public class PutTest extends AbstractTest {
	
	@Test
	public void testPutNotJsonFormatFail() throws Exception {
		
		String theId = callPost("value");
		
		final File uploadFile = new File("src/test/home-test-application.template/log4j.xml");
		Assert.assertTrue( uploadFile.isFile() && uploadFile.length() > 0 );
		
		Response r = new Client("http://127.0.0.1:" + PORT + "/tests/"+theId)//
				.method("PUT")//
				.headerContentTypeJson()
				.setEntityParameter( uploadFile )//
				.send();

		Assert.assertNotSame( 200 , r.statusCode);
	}
	

	@Test
	public void testPut() throws Exception {
		
		String theJson = "{a:\"b\"}";
		Response r = new Client("http://127.0.0.1:" + PORT + "/tests")//
				.method("POST")//
				.headerContentTypeJson()
				.setEntityParameter( theJson.getBytes( Utils.CHARSET_UTF8 ) )//
				.send();
		
		Assert.assertTrue( String.valueOf( r.exception ), r.exception == null );
		Assert.assertEquals( 201 , r.statusCode);
		
		final String theId = r.getJson().getAsJsonObject().get( "id" ).getAsString(); 
		Assert.assertTrue( theId.matches( "[0-9]+(-[a-f0-9]+){5}" ) );
		
		r = new Client("http://127.0.0.1:" + PORT + "/tests/"+ theId )//
				.method("GET")//
				.addGetParameter( "_verbose" , "true")//
				.send();
		
		Assert.assertEquals( "kaputt" , "b", r.getJsonObject().get( "a" ).getAsString() );
		
		theJson = "{c:\"d\"}";
		r = new Client("http://127.0.0.1:" + PORT + "/tests/"+theId)//
				.method("PUT")//
				.headerContentTypeJson()
				.setEntityParameter( theJson.getBytes( Utils.CHARSET_UTF8 ) )//
				.send();
		
		Assert.assertTrue( String.valueOf( r.exception ), r.exception == null );
		Assert.assertEquals( 201 , r.statusCode);
		
		r = new Client("http://127.0.0.1:" + PORT + "/tests/"+ theId )//
				.method("GET")//
				.headerContentTypeJson()
				.addGetParameter( "_verbose" , "true")//
				.send();
		
		Assert.assertEquals( "kaputt" , "d", r.getJsonObject().get( "c" ).getAsString() );
	}

}
