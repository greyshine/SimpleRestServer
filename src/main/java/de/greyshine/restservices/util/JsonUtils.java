package de.greyshine.restservices.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

public abstract class JsonUtils {
	
	public static final Gson GSON_PRETTYPRINT = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	
	public static JsonElement readJson(File aFile) throws IOException {

		InputStream theIs = null;

		try {

			theIs = (aFile == null || !aFile.isFile() ? null : new FileInputStream(aFile));
			return readJson(theIs);

		} finally {

			IOUtils.closeQuietly(theIs);
		}
	}

	public static JsonElement readJson(InputStream inIs) {

		return new JsonParser().parse(new InputStreamReader(inIs, CHARSET_UTF8));
	}
	
	public static long streamJson(InputStream inIs, OutputStream inOutputStream, boolean inPrettyPrint) throws IOException {
		return Utils.copy(jsonToInputStream(readJson(inIs), inPrettyPrint), inOutputStream);

	}

	public static InputStream jsonToInputStream(JsonElement inJson, boolean inPrettyPrint) {
		return new ByteArrayInputStream(jsonToByteArray(inJson, inPrettyPrint));
	}

	public static byte[] jsonToByteArray(JsonElement inJson, boolean inPrettyPrint) {
		return jsonToString(inJson, inPrettyPrint).getBytes(CHARSET_UTF8);
	}
	
	public static long writeJson(JsonElement inJson, File inFile, boolean inPrettyPrint) throws FileNotFoundException, IOException {
		
		try ( OutputStream theOs = new FileOutputStream( inFile ) ) {
			
			return writeJson( inJson, theOs, inPrettyPrint );
		} 
	}
	
	
	public static void streamJson(InputStream inputStream, File inOutputFile, boolean inPrettyPrint)
			throws FileNotFoundException, IOException {

		FileOutputStream theFos = null;

		try {

			streamJson(inputStream, theFos = new FileOutputStream(inOutputFile), inPrettyPrint);

		} finally {

			Utils.close(theFos);
		}
	}
	public static long writeJson(InputStream inJsonStream, File inFile, boolean inPrettyPrint) throws FileNotFoundException, IOException {
		
		return writeJson( readJson( inJsonStream ), inFile, inPrettyPrint);
	}

	public static long writeJson(JsonElement inJson, OutputStream inOutputStream, boolean inPrettyPrint) throws IOException {
		
		inJson = Utils.defaultIfNull(inJson, JsonNull.INSTANCE);
		
		System.out.println( "a: "+ inJson );
		InputStream theIs = jsonToInputStream(inJson, inPrettyPrint); 
		
//		String s = "";
//		while( theIs.available() > 0 ) {
//			
//			s += (char)theIs.read();
//		}
//		System.out.println( "b: "+ s );
		
		theIs = jsonToInputStream(inJson, inPrettyPrint); 
		return Utils.copy( theIs, inOutputStream);
	}

	public static JsonElement readJson(String inJsonText) {
		return Utils.isBlank( inJsonText ) ? JsonNull.INSTANCE : readJson( new ByteArrayInputStream( inJsonText.getBytes( CHARSET_UTF8 ) ) );
	}
	
	public static String jsonToString(JsonElement inJson, boolean inPrettyPrint) {
		
		inJson = Utils.defaultIfNull( inJson , JsonNull.INSTANCE);
		
		String r = inPrettyPrint ? GSON_PRETTYPRINT.toJson(inJson) : GSON.toJson( inJson ) ; 
		
		return r;
	}
	
	
	// path related stuff, todo if needed later...
	

	public static abstract class AbstractPathElement implements IPathElement {

		public final JsonElement je;
		
		public AbstractPathElement(JsonElement inJe) {
			je = inJe == null ? JsonNull.INSTANCE : inJe;
		}

		@Override
		public boolean isArray() {
			return je.isJsonArray();
		}

		@Override
		public Integer getArrayIndex() {
			return null;
		}

		@Override
		public boolean isPrimitve() {
			return je.isJsonPrimitive();
		}

		@Override
		public boolean isNull() {
			return je.isJsonNull();
		}

		@Override
		public JsonElement getValue() {
			return je;
		}
	}

	public static class RootPathElement extends AbstractPathElement {

		public RootPathElement(JsonElement inJe) {
			super( inJe );
		}
		
		@Override
		public String getPath() {
			return "$";
		}

		@Override
		public final IPathElement getParent() {
			return null;
		}

		@Override
		public final boolean isRoot() {
			return true;
		}
	}

	private static final Charset CHARSET_UTF8 = Charset.forName( "UTF-8" );
	
	public enum EPathNotation{
		
		BRACKET(""),
		DOT("(\\$\\.)?[a-zA-Z0-9_-]");
		
		final String regex;
		
		private EPathNotation(String inRegex) {
			regex = inRegex;
		}
		
		public boolean isMatch(String inPath) {
			return inPath != null && inPath.matches( regex );
		}
	}
	
	private JsonUtils() {
	}
	
	public static <T extends JsonElement> T read(File inFile) throws FileNotFoundException, IOException {
		
		try ( FileInputStream theFis = new FileInputStream( inFile ) ) {
			return read( theFis );
		} 
	}

	@SuppressWarnings("unchecked")
	public static <T extends JsonElement> T read(InputStream inIs) {
		
		return (T) new JsonParser().parse( new JsonReader( new InputStreamReader( inIs , CHARSET_UTF8) ) );
	}

	public static JsonElement set(JsonElement inJson, String inPath, String inValue) {
		
		return set( inJson, inPath, inValue == null ? JsonNull.INSTANCE : new JsonPrimitive( inValue ));
	}
	
	public static JsonElement set(JsonElement inJson, String inPath, JsonElement inJsonValue) {
	
		inPath = convertPathToDotNotation( inPath );
		
		final String[] theParts = inPath.split( "\\.", -1 );
		
		JsonElement c = inJson;
		
		for (int i = 0, l=theParts.length; c != null && i < l; i++) {
			
			System.out.println( i + ":"+ theParts[i]  + " "+ c  );
			
			if ( i==0 && "$".equals(theParts[i]) ) { continue; }
			
			if ( c.isJsonObject() ) {
				
				c = ((JsonObject)c).get( theParts[i] );
			}
			
			final boolean isArrayExpected = theParts[i].matches( ".+[0-9]" );
			
			if ( isArrayExpected ) {
				
				
			} else {
				
				
				
			}
		}
		
		System.out.println( ">> "+ c );
		
		return inJson;
	}
	
	
	
	public static String convertPathToDotNotation(String inPath) {
		return inPath;
	}

	public static String convertPathToBracketNotation(String inPath) {
		return inPath;
	}



	public static class Entry {
		
		final String path;
		final JsonElement jsonElement;
		
		public Entry(String path, JsonElement jsonElement) {
			this.path = path;
			this.jsonElement = jsonElement;
		}
	}



	public static List<Entry> listElements(JsonObject theJo) {
		throw new UnsupportedOperationException("TODO: implement");
	}

	public static JsonElement get(JsonObject theJo, String inPath) {
		
		return null;
	}

	/**
	 * Not yet implemented
	 * @param inJe
	 * @param inVisitor
	 */
	@Deprecated
	public static void walkJson(JsonElement inJe, IElementVisitor inVisitor) {
		
		if ( inVisitor == null ) { return; }
		else if ( inJe == null || inJe.isJsonNull() ) {
			inVisitor.visitNull("$");
			return;
		}
		
		final List<IPathElement> q = new ArrayList<>();
		q.add( new RootPathElement( inJe ) );
		
		while( !q.isEmpty() ) {
			
			IPathElement theJe = q.remove(0);
			
		}
		
		
	}
	
	public interface IElementVisitor {
	
		EPathNotation getPathNotation();
		void visitNull(String inPath);
		void visitPrimitive(String inPath, JsonPrimitive inJp);
		void visitObjectBefore(String inPath, JsonObject inJo);
		void visitObjectAfter(String inPath, JsonObject inJo);
		void visitArrayBefore(String inPath, JsonArray inJa);
		void visitArrayAfter(String inPath, JsonArray inJa);
	}
	
	public interface IPathElement {
		
		String getPath();
		boolean isArray();
		Integer getArrayIndex();
		boolean isPrimitve();
		boolean isNull();
		JsonElement getValue();
		IPathElement getParent();
		boolean isRoot();
	}



	public static JsonElement toJsonArray(List<String> inValues) {
		
		final JsonArray theJa = new JsonArray();
		
		inValues = inValues != null ? inValues : Collections.emptyList();
		
		for( String s : inValues ) {
			
			theJa.add( s );
		}
		
		return theJa;
	}

	public static JsonObject toJsonObject(Exception e) {
		
		if ( e == null ) { return null; }
		
		Job j = new Job();
		j.put("exception", e.getClass().getCanonicalName());
		j.put("message", e.getMessage());
		
		return j.build();
	}
	
	public static JsonObject createBinaryReferenceJson(String inId) {
		
		return new Job()//
				.put("_type", "binary")//
				.put("id", inId)//
				.build();
	}
	
}
