package de.greyshine.restservices.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Simple Builder for {@link JsonObject}s 
 *
 */
public class Job {

	private static final Gson GSON_PRETTYPRINT = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	
	public final JsonObject j = new JsonObject();
	
	public Job() {}
	
	public static Job create() {
		return new Job();
	}
	
	public Job put(String inProperty, String inValue) {
		j.addProperty(inProperty, inValue);
		return this;
	}
	
	public Job put(String inProperty, Number inValue) {
		j.addProperty(inProperty, inValue);
		return this;
	}
	
	public Job put(String inProperty, Boolean inValue) {
		j.addProperty(inProperty, inValue);
		return this;
	}
	
	public Job put(String inProperty, JsonElement inValue) {
		j.add(inProperty, inValue);
		return this;
	}
	
	public JsonObject build() {
		return j;
	}
	
	public String buildAsString(boolean inPretty) {
		return inPretty ? GSON_PRETTYPRINT.toJson( j ) : GSON.toJson( j );
	}
	public InputStream buildAsInputStream(boolean inPretty) {
		try {
			return new ByteArrayInputStream( buildAsString( inPretty ).getBytes( "UTF-8" ) );
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonObject buildAsObject(String inProperty, String inValue) {
		return new Job().put(inProperty, inValue).build();
	}
	
	public static String buildAsString(String inProperty, String inValue) {
		return buildAsString(inProperty, inValue, true);
	}
	
	public static String buildAsString(String inProperty, String inValue, boolean inPretty) {
		return create().put(inProperty, inValue).buildAsString(inPretty);
	}
	
}
