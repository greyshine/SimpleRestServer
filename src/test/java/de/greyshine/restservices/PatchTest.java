package de.greyshine.restservices;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;

import de.greyshine.restservices.client.Client;
import de.greyshine.restservices.client.Client.Response;
import de.greyshine.restservices.util.JsonUtils;
import de.greyshine.restservices.util.Utils;

public class PatchTest extends AbstractTest {
	
	@Test
	public void test() throws IOException {
		
		final String theId = callPost( "patch" );
		Assert.assertTrue( theId.matches( "[0-9]+(-[a-f0-9]+){5}" ) );

		JsonObject jo = callGetAndJson( theId ).getAsJsonObject();
		
		Assert.assertEquals( "kaputt" , "patch", jo.get( "value" ).getAsString() );
		
		
		Response r = clientFactory.createPatch( "/tests/"+theId +"/test" )
				.headerContentTypeJson()
				.setEntityParameter( "PATCHED".getBytes( Utils.CHARSET_UTF8 ) )//
				.send();
		
		Assert.assertTrue( String.valueOf( r.exception ), r.exception == null );
		Assert.assertEquals( 200 , r.statusCode);
		
		r = clientFactory.createGet("/tests/"+ theId )//
				.headerContentTypeJson()
				.send();
		
		Assert.assertEquals( "patched to PATCHED failed" , "PATCHED", r.consumeJsonObject().get( "test" ).getAsString() );
		
		r = new Client("http://127.0.0.1:" + PORT + "/tests/"+theId +"/test")//
				.method("PATCH")//
				.headerContentTypeJson()
				.send();
		
		Assert.assertTrue( String.valueOf( r.exception ), r.exception == null );
		Assert.assertEquals( 200 , r.statusCode);
		
		System.out.println( r.consumeText() );
		
		r = new Client("http://127.0.0.1:" + PORT + "/tests/"+ theId )//
				.method("GET")//
				.addGetParameter( "_verbose" , "true")//
				.send();
		
		String theText = r.consumeText();
		JsonObject theJsonObject = Utils.toJsonObject( theText );
		
		Assert.assertEquals( "patched to NULL failed: "+ theText, null, theJsonObject.get( "test" ) );
		
		
		r = new Client("http://127.0.0.1:" + PORT + "/tests/"+theId +"/newproperty")//
				.method("PATCH")//
				.headerContentTypeJson()
				.setEntityParameter( "\"newValue\"" )//
				.send();
		
		Assert.assertTrue( String.valueOf( r.exception ), r.exception == null );
		Assert.assertEquals( 200 , r.statusCode);
		
		theText = r.consumeText();
		theJsonObject = Utils.toJsonObject( theText );
		
		System.out.println( theText );
		
		
		r = new Client("http://127.0.0.1:" + PORT + "/tests/"+ theId )//
				.method("GET")//
				.addGetParameter( "_verbose" , "true")//
				.send();
		
		Assert.assertEquals( "patched newproperty failed: "+ theText, "newValue", theJsonObject.get( "newproperty" ).getAsString() );
		
	}
	
	@Test
	public void testPatchNotJsonFormatFail() throws Exception {
		
		String theId = callPost("value");
		
		final File uploadFile = new File("src/test/home-test-application.template/log4j.xml");
		Assert.assertTrue( uploadFile.isFile() && uploadFile.length() > 0 );
		
		Response r = new Client("http://127.0.0.1:" + PORT + "/tests/"+theId +"/test")//
				.method("PATCH")//
				.headerContentTypeJson()
				.setEntityParameter( uploadFile )//
				.send();
		
		Assert.assertNotSame( 200 , r.statusCode);
	}
	
	@Test
	public void testFileUpload() throws IOException {
		
		final String theId = callPost("value");
		
		System.out.println( callGetAndJson(theId) );
		
		
		final File uploadFile = new File("src/test/home-test-application.template/log4j.xml");
		Assert.assertTrue( uploadFile.isFile() && uploadFile.length() > 0 );
		final String theSha256Checksum = Utils.getSha256( uploadFile ); 
		
		final Response r = clientFactory.createPatch( "tests/"+ theId +"/file" )
		.setEntityParameter( uploadFile )//
		.send();
		
		System.out.println( r.statusCode );
		
		Assert.assertEquals( 200 , r.statusCode);
			
		
		
		final Response r2 = clientFactory.createGet( "tests/"+ theId )//
				.send();
		
		String theText = r2.consumeText();
		JsonObject theJsonObject = Utils.toJsonObject( theText );
		
		System.out.println( theText );
		
		final JsonObject theFileJo = (JsonObject) theJsonObject.get("file");
		
		System.out.println( JsonUtils.jsonToString(theFileJo, true) );
		
		Assert.assertEquals( "binary" , theFileJo.get( "_type" ).getAsString());
		
		
		
		final Response r3 = clientFactory.createGet("tests/"+ theId +"/file" )//
				.send();
		
		theText = r3.consumeText();
		
		System.out.println( theText );
		
		// TODO implement method properly, not yet done
		Assert.assertEquals( 500 , r3.statusCode);
		
		String theFileId = theFileJo.get( "id" ).getAsString();
		
		Client c = clientFactory.createGet("file/"+ theFileId);
		Response r4 = c.send();
		
		System.out.println( r4 );
		Assert.assertEquals( 200 , r4.statusCode);
		
		final InputStream theIs = r4.getInputStream();
		final String theSha256Checksum2 = Utils.getSha256( theIs );
		
		Assert.assertEquals( theSha256Checksum , theSha256Checksum2);
		
	}

}
