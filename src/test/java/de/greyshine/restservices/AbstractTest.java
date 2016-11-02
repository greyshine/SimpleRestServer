package de.greyshine.restservices;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.greyshine.restservices.client.Client;
import de.greyshine.restservices.client.Client.Response;
import de.greyshine.restservices.client.ClientFactory;
import de.greyshine.restservices.client.JsonConsumer;

public abstract class AbstractTest {
	
	static final Log LOG = LogFactory.getLog( AbstractTest.class );
	
	static String texts = "";
	
	static int PORT = 7778;
	static int TTL = 40;
	
	static final String theHomePath = "target/home-test-application";
	static ServerMain server;
	
	public final ClientFactory clientFactory = new ClientFactory( "http://127.0.0.1:"+ PORT ).verbose();
	
	{
		texts += "\n"+Thread.currentThread() +"; "+ getClass().getCanonicalName();
		System.out.println( texts );
	}
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		
		FileUtils.copyDirectory(new File("src/test/home-test-application.template"),
				new File( theHomePath ));
		
		server = new ServerMain( "-path", theHomePath, /*"-ttl", ""+ TTL,*/ "-webaddress","greyshine.de", "-port.http", ""+ PORT, DefaultApplication.class.getCanonicalName() );
		
		server.start();
		
		LOG.debug( "ready for tests: "+ server );
		
	}
	
	@AfterClass
	public static void afterClass() throws InterruptedException {
		server.stop();
	}
	
	public String callPost(String inValue) throws IOException {
		
		final String time = LocalDateTime.now().format( DateTimeFormatter.ISO_LOCAL_DATE_TIME );
		final String uid = UUID.randomUUID().toString();
		
		final JsonObject theJo = new JsonObject();
		theJo.addProperty( "uid" , uid);
		theJo.addProperty( "time" , time);
		theJo.addProperty( "value" , inValue);
		
		Response r = clientFactory.createPost( "tests" )
				.headerContentTypeJson()
				.setEntityParameter( theJo )//
				.send();
		
		Assert.assertTrue( String.valueOf( r.exception ), r.exception == null );
		Assert.assertEquals( 201 , r.statusCode);
		
		return r.consumeJsonObject().get( "id" ).getAsString();
	}
	
	public Response callGetAndResponse(String inId) {
		
		Response r = clientFactory.createGet( "tests/"+ inId)
				.addGetParameter( "_verbose" , "true")//
				.send();
		
		Assert.assertTrue( r.statusText, r.isStatusOk() );
		
		return r;
	}

	public JsonElement callGetAndJson(String inId) throws IOException {
		
		Response r = clientFactory.createGet( "tests/"+ inId)
				.addGetParameter( "_verbose" , "true")//
				.send();
		
		Assert.assertTrue( r.statusText, r.isStatusOk() );
		
		return r.consume( JsonConsumer.getInstance() );
	}
	
}
