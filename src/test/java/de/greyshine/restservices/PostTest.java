package de.greyshine.restservices;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import de.greyshine.restservices.client.Client;
import de.greyshine.restservices.client.Client.Response;
import de.greyshine.restservices.util.Utils;

public class PostTest extends AbstractTest {

	@Test
	public void testPostAndGet() throws Exception {
		
		final String time = LocalDateTime.now().format( DateTimeFormatter.ISO_LOCAL_DATE_TIME );
		final String uid = UUID.randomUUID().toString();
		
		final String theJson = "{test:\""+ this +"\", time:\""+ time +"\", uid:\""+ uid +"\" }";

		Response r = clientFactory.createPost("tests" )
				.headerContentTypeJson()
				.setEntityParameter( theJson.getBytes( Utils.CHARSET_UTF8 ) )//
				.send();
		
		Assert.assertTrue( String.valueOf( r.exception ), r.exception == null );
		
		System.out.println( "\n----------------" );
		System.out.println( "send > "+ theJson );
		System.out.println( "----------------" );
		System.out.println( r.getText() );
		
		final String theId = r.getJson().getAsJsonObject().get( "id" ).getAsString(); 
		
		Assert.assertTrue( theId.matches( "[0-9]+(-[a-f0-9]+){5}" ) );
		
		r = callGetAndResponse(theId);
		
		System.out.println( "-check---------------" );
		System.out.println("GET.text > "+ r.getText() );
		System.out.println("GET.json > "+ r.getJson() );
		
		Assert.assertEquals( "kaputt" , time, r.getJsonObject().get( "time" ).getAsString() );
		Assert.assertEquals( "kaputt" , uid, r.getJsonObject().get( "uid" ).getAsString() );
	}
	
	@Test
	public void testPostNotJsonFormatFail() {
		
		final File uploadFile = new File("src/test/home-test-application.template/log4j.xml");
		Assert.assertTrue( uploadFile.isFile() && uploadFile.length() > 0 );
		
		final Response r = new Client("http://127.0.0.1:" + PORT + "/tests")//
		.method("POST")//
		.headerContentTypeJson()
		.setEntityParameter( "{broken:jsonfromat; \"must\": break " )//
		.send();
		
		Assert.assertNotSame( 200 , r.statusCode);
	}
	
}
