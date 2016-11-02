package de.greyshine.restservices;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.greyshine.restservices.util.JsonUtils;
import de.greyshine.restservices.util.JsonUtils.EPathNotation;
import de.greyshine.restservices.util.JsonUtils.Entry;
import junit.framework.Assert;

public class JsonUtilsTest {

	@Test
	@Ignore
	public void testListPaths() throws FileNotFoundException, IOException {
		
		final JsonObject theJo = JsonUtils.read( new File("src/test/resources/JsonUtilsTests/listpaths.json") );
		
		for (Entry e : JsonUtils.listElements( theJo )) {
			System.out.println( e );
		}
		
	}
	
	@Test
	@Ignore
	public void testDotNotation() {
		
		String thePath = "$.";
		
		Assert.assertTrue( EPathNotation.DOT.isMatch( thePath ) );
		
	}
	
	@Test
	@Ignore
	public void testGetValue() throws FileNotFoundException, IOException {
		
		final JsonObject theJo = JsonUtils.read( new File("src/test/resources/JsonUtilsTests/listpaths.json") );
		
		JsonElement theJe = JsonUtils.get( theJo, "$.world" );
		Assert.assertEquals( "hello", theJe.getAsString() );
		
		theJe = JsonUtils.get( theJo, "$.boolean" );
		Assert.assertEquals( true, theJe.getAsBoolean() );
		
		theJe = JsonUtils.get( theJo, "$.object" );
		Assert.assertEquals( true, theJe.isJsonObject() );
		
		theJe = JsonUtils.get( theJo, "$.null" );
		Assert.assertEquals( true, theJe.isJsonNull() );
		
	}

	@Test
	@Ignore
	public void setValue() throws FileNotFoundException, IOException {
		
		final JsonObject theJo = JsonUtils.read( new File("src/test/resources/JsonUtilsTests/listpaths.json") );
		
		JsonUtils.set( theJo , "$.hallo", "world");
		
	}
	
}
