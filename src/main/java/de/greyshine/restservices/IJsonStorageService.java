package de.greyshine.restservices;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import de.greyshine.restservices.filebased.IServiceProvider;
import de.greyshine.restservices.util.Utils;

/**
 * bridge for accessing the backend and its documents and files 
 */
public interface IJsonStorageService extends IStatusReportable {
	
	enum EDataType {
		
		NULL,
		JSON_OBJECT,
		JSON_ARRAY,
		JSON_NUMBER,
		JSON_TEXT
	}
	
	void init(IServiceProvider inServiceProvider, File inBasepath, String[] inArgs);
	void destroy();
	
	/**
	 * @return List of all so far known collections
	 */
	List<String> getCollectionNames();

	IDocument read(String inCollectionName, String inId);
	IDocument create(String inCollectionName, JsonElement inJson);
	IDocument update(String inCollectionName, String inId, JsonElement inJson);
	IDocument delete(String inCollectionName, String inId);
	
	void find(String inCollectionName, List<String> inSortings, List<String> inSelects, Long inOffset, Long inLength, IResultHandler<FindResult> inResultHandler ) throws IOException;
	
	interface IDocument {
		
		enum EStreamType {
			
			RAW, DATA, ENVELOPED;
		}
		
		String getCollection();
		String getId();
		
		boolean isFound();
		boolean isNotFound();
		/** 
		 * @param inProperty
		 * @return if the underlying data has a property with the given name 
		 */
		boolean isItemProperty(String inProperty);
		
		Exception getException();
		boolean isExceptional();
		
		String getSha256();
		long getLength();
		
		boolean isStreamable();
		InputStream getStream(EStreamType inStreamType);
		
		JsonElement getDataJson() throws IOException;
		void writeDataJson(JsonElement inJson) throws IOException;
		
		ZonedDateTime getCreated();
		ZonedDateTime getUpdated();

		String getEtag();
		EDataType getDataType();
	}
	
	public static class FindResult {
		
		public final String id;
		public final String collection;
		public JsonObject jsonDataEnvelope;
		public JsonElement jsonData;
		public long index;
		
		public final boolean isDataJsonObject;
		
		public FindResult(long index, String collection, String id, JsonObject jsonObject) {
			this.index = index;
			this.id = id;
			this.collection = collection;
			this.jsonDataEnvelope = jsonObject;
			this.jsonData = this.jsonDataEnvelope.get( "data" );
			this.isDataJsonObject = jsonData.isJsonObject();
		}
		
		public JsonPrimitive getDataMember(String inMember) {
			
			try {
				
				return jsonData.getAsJsonObject().get( inMember ).getAsJsonPrimitive();
				
			} catch (Exception e) {
				// swallow
				return null;
			}
			
		}
		
	}

	interface IResultHandler<T> {
		EHandleResult handle(T inResult);
		EHandleResult handleException(T inResult, Exception inException);
	}
	
	enum EHandleResult {
		
		STOP, CONTINUE;
	}


}
