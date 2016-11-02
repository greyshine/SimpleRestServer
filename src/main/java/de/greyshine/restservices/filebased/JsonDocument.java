package de.greyshine.restservices.filebased;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import de.greyshine.restservices.IJsonStorageService.IDocument;
import de.greyshine.restservices.IJsonStorageService.EDataType;
import de.greyshine.restservices.util.JsonUtils;
import de.greyshine.restservices.util.Utils;


public class JsonDocument implements IDocument {
	
	static final Log LOG = LogFactory.getLog( JsonDocument.class );
	
	private static final DateTimeFormatter DF_STOREFORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy'T'HH:mm:ss.SSS'Z'");
	public static final ZoneId ZONEID_GMT = ZoneId.of("GMT");
	private static final byte[] EMPTY_BYTES = {};
	
	private Exception exception;
	private File file;
	private JsonObject jsonObject;
	private ZonedDateTime createdOn = ZonedDateTime.now();
	private ZonedDateTime updatedOn;

	public JsonDocument(File inFile) {
		file = inFile;
	}
	
	@Override
	public String getCollection() {
		return file.getParentFile().getName();
	}

	@Override
	public String getId() {
		return file == null ? null : file.getName();
	}

	@Override
	public boolean isFound() {
		return exception == null && file != null && file.isFile();
	}

	@Override
	public boolean isNotFound() {
		return !isFound();
	}

	@Override
	public boolean isExceptional() {
		return exception != null;
	}
	
	@Override
	public Exception getException() {
		return exception;
	}

	@Override
	public String getSha256() {
		return Utils.getSha256(file);
	}

	@Override
	public long getLength() {
		return !isStreamable() ? -1 : file.length();
	}

	@Override
	public boolean isItemProperty(String inProperty) {
		
		if ( inProperty == null || isNotFound() ) { return false; }
		
		try {
			loadJson();
		} catch (IOException e) {
			
			LOG.error( e );
			
			return false;
		}
		
		final JsonElement theMember = jsonObject == null ? null : jsonObject.get( inProperty ); 
		return theMember != null;
	}

	@Override
	public boolean isStreamable() {
		return getException() == null && file != null && file.isFile();
	}

	@Override
	public InputStream getStream(EStreamType inStreamType) {

		if (!isStreamable()) {
			return new ByteArrayInputStream(EMPTY_BYTES);
		}

		inStreamType = inStreamType == null ? EStreamType.DATA : inStreamType;

		try {

			if (EStreamType.RAW == inStreamType) {

				return new FileInputStream(file);

			}

			final JsonElement theJson = EStreamType.ENVELOPED == inStreamType ? loadJson() : loadJson().get("data");

			final InputStream theIs = JsonUtils.jsonToInputStream(theJson, true);

			return theIs;

		} catch (IOException e) {
			throw new RuntimeException("resource not loaded, internal server error");
		}
	}

	private JsonObject loadJson() throws IOException {

		if (jsonObject != null) {
		
			return jsonObject;
		
		} else if ( !file.isFile() ) {
			
			return null;
		}

		jsonObject = (JsonObject) JsonUtils.readJson(file);
		
		
		createdOn = ZonedDateTime.from( ZonedDateTime.of( LocalDateTime.from( DF_STOREFORMAT.parse( jsonObject.get( "created" ).getAsString() ) ), ZONEID_GMT ) );
		updatedOn = ZonedDateTime.from( ZonedDateTime.of( LocalDateTime.from( DF_STOREFORMAT.parse( jsonObject.get( "updated" ).getAsString() ) ), ZONEID_GMT ) );

		return jsonObject;
	}

	@Override
	public ZonedDateTime getCreated() {
		return createdOn;
	}

	@Override
	public ZonedDateTime getUpdated() {
		return updatedOn;
	}

	@Override
	public String getEtag() {
		
		try {
		
			loadJson();
		
			return updatedOn == null ? "" : Long.toString(Utils.toMilliSeconds(updatedOn), Character.MAX_RADIX);
			
		} catch (Exception e) {
			
			return "";
		}
	}
	
	@Override
	public JsonElement getDataJson() throws IOException {

		try {
			
			return Utils.defaultIfNull( loadJson().get( "data" ) , JsonNull.INSTANCE);
			
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public void writeDataJson(JsonElement inJson) throws IOException {
		
		if ( isNotFound() ) {
			
			createdOn = updatedOn = getGmtTime();
			jsonObject = new JsonObject();
			jsonObject.addProperty( "id" , getId() );
			jsonObject.addProperty( "created" , DF_STOREFORMAT.format(createdOn) );
			jsonObject.addProperty( "updated" , DF_STOREFORMAT.format(updatedOn) );
			jsonObject.addProperty( "v" ,  0 );
		
		} else {
		
			loadJson();
			updatedOn = getGmtTime();
			
			jsonObject.addProperty("updated", DF_STOREFORMAT.format(updatedOn));
			jsonObject.addProperty( "v" ,  jsonObject.get( "v" ).getAsInt()+1 );
		}
		
		inJson = inJson == null ? JsonNull.INSTANCE : inJson;
		jsonObject.add( "data" , inJson);
		
		LOG.debug("storing " + this.file.getName() + " > " + file + " > " + inJson);
					
		JsonUtils.writeJson(jsonObject, file, true);
	}

	@Override
	public EDataType getDataType() {
		
		try {
			
			loadJson();
			
		} catch (IOException e) {
			
			throw new IllegalStateException("unable to determine underlying type: "+ file);
		}
		
		final JsonElement theDataJsonElement = jsonObject.get( "data" ); 
		
		
		if ( theDataJsonElement == null || theDataJsonElement.isJsonNull() ) { return EDataType.NULL; }
		else if ( theDataJsonElement.isJsonArray() ) { return EDataType.JSON_ARRAY; }
		else if ( theDataJsonElement.isJsonPrimitive() ) { return EDataType.JSON_TEXT; }
		// TODO inspect internal objects, like file-reference / geocoords
		else if ( theDataJsonElement.isJsonObject() ) { return EDataType.JSON_OBJECT; }
		
		throw new IllegalStateException();
	}
	
	public static ZonedDateTime getGmtTime() {
		return ZonedDateTime.now(ZONEID_GMT);
	}

	public void setException(Exception inException) {
		this.exception = inException;
	}

	@Override
	public String toString() {
		return "JsonDocument [collection="+ getCollection() +", id="+ getId() +", createdOn=" + createdOn + ", updatedOn=" + updatedOn + "]";
	}

	public File getFile() {
		return file;
	}
	
	

	
	
}
