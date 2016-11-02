package de.greyshine.restservices.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;

import de.greyshine.restservices.client.Client.IStreamConsumer;

public class JsonConsumer implements IStreamConsumer<JsonElement> {
	
	public static final JsonConsumer INSTANCE = new JsonConsumer();
	
	/**
	 * Threadsafe: yes
	 * https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/json/JsonParser
	 */
	private static final JsonParser JSONPARSER = new JsonParser();  

	@Override
	public JsonElement read(InputStream inIs) throws IOException {

		if (inIs == null) {
			return JsonNull.INSTANCE;
		}
		
		return JSONPARSER.parse(new InputStreamReader( inIs, "UTF-8" ));
	}

	public static JsonConsumer getInstance() {
		return INSTANCE;
	}
}
