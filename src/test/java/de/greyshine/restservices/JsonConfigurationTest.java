package de.greyshine.restservices;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;

import de.greyshine.restservices.util.Utils;

public class JsonConfigurationTest {
	
	final static File basebath = new File("target/home-test-"+ JsonConfigurationTest.class.getSimpleName() ); 
	
	@Before
	public void before() {
		
		basebath.mkdirs();
		
		Assert.assertTrue( basebath.isDirectory() );
	}
	
	@Test
	public void test() {
		
		final JsonObject theConfigJson = new JsonObject(); 
		theConfigJson.addProperty( "path" , Utils.getCanonicalFileSafe(basebath).getAbsolutePath());
		
		System.out.println( Utils.toString( theConfigJson ) );
		
	}

}
