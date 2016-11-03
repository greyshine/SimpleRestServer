package de.greyshine.restservices.filebased;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import de.greyshine.restservices.Constants;
import de.greyshine.restservices.IJsonStorageService;
import de.greyshine.restservices.util.JsonUtils;
import de.greyshine.restservices.util.Utils;
import de.greyshine.restservices.util.Utils.Wrapper;

public class JsonFileStorage implements IJsonStorageService {
	
	private static final Log LOG = LogFactory.getLog(JsonFileStorage.class);

	private File collectionsDir;
	
	@Override
	public void init(File inBasepath, String[] inArgs) {
		
		collectionsDir = new File( inBasepath, "data/collections" );
		collectionsDir.mkdirs();
	}

	@Override
	public void destroy() {
	}
	
	@Override
	public JsonObject createStatusReport() {
		
		final JsonObject inJo = new JsonObject();

		final Wrapper<Long> theSize = new Wrapper<>(0L);
		final Wrapper<Long> theCount = new Wrapper<>(0L);

		try {
			
			Files.walkFileTree(collectionsDir.toPath(), new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					theCount.value++;
					theSize.value += file.toFile().length();
					return FileVisitResult.CONTINUE;
				}
			});

		} catch (IOException e) {

			LOG.error(e, e);
		}

		inJo.addProperty("data.count", theCount.value);
		inJo.addProperty("data.size", theSize.value);
		inJo.addProperty("data.size.readable", Utils.toStringDataSize(theSize.value));
		inJo.add("collections", JsonUtils.toJsonArray( getCollectionNames() ) );

		return inJo;
	}

	@Override
	public IDocument read(String inCollectionName, String inId) {
		
		return new JsonDocument(getDocumentFile(inCollectionName, inId, false));
	}
	
	@Override
	public IDocument create(String inCollectionName, JsonElement inJson) {
		
		final JsonDocument theItem = new JsonDocument( getDocumentFile(inCollectionName, Utils.createUniqueId(), true) );
		try {
		
			theItem.writeDataJson( inJson );
		
		} catch (IOException e) {
			
			theItem.setException( e );
		}
		
		return theItem;
	}

	@Override
	public IDocument update(String inCollectionName, String inId, JsonElement inJson) {
		
		JsonDocument theDocument = new JsonDocument( getDocumentFile(inCollectionName, inId, false) );
		
		if ( theDocument.isExceptional() || theDocument.isNotFound() ) {
			
			return theDocument;
		}
		
		try {
			
			theDocument.writeDataJson( inJson );
		
		} catch (IOException e) {
			
			theDocument.setException( e );
		}
		
		return theDocument;
	}

	@Override
	public IDocument delete(String inCollectionName, String inId) {
		
		final JsonDocument theJsonDocument = new JsonDocument(getDocumentFile(inCollectionName, inId, false));
		
		if ( theJsonDocument.isNotFound() ) {

			theJsonDocument.setException( new FileNotFoundException( inCollectionName+"/"+inId ) );
			
			return theJsonDocument;
		}

		try {
		
			Files.delete( theJsonDocument.getFile().toPath() );
	
			if ( theJsonDocument.getFile().exists() ) {
				
			}
			
		} catch (IOException e) {
			
			theJsonDocument.setException( new IllegalStateException( inCollectionName+"/"+inId +" still exists: "+ theJsonDocument.getFile().getPath(), e ) );
		}

		return theJsonDocument;
	}

	@Override
	public void find(String inCollectionName, List<String> inSortings, List<String> inSelects, Long inOffset,
			Long inLength, IResultHandler<FindResult> inHandler) throws IOException {
		
		if ( inHandler == null ) { return; }
		
		inOffset = inOffset == null ? 0 : inOffset;
		inSelects = Utils.defaultIfNull(inSelects, Collections.emptyList());

		final File theDataDir = getCollectionsDir(inCollectionName, false);
		final List<File> theFiles = Utils.listFiles(theDataDir, false, true, true);

		long theIndex = -1;
		List<FindResult> theFindResults = new ArrayList<>();
		
		for (File aFile : theFiles) {

			theIndex++;

			try {

				final JsonObject theJo = JsonUtils.readJson(aFile).getAsJsonObject();
				
				if ( theJo.get( "data" ) == null ) {
					theJo.add( "data" , JsonNull.INSTANCE);
				}
				
				theFindResults.add(new FindResult(theIndex, inCollectionName, aFile.getName(), theJo));

			} catch (Exception e) {
				// swallow
				LOG.warn("error read: " + aFile + ": " + e.getMessage(), e);
			}
		}
		
		if ( inSortings != null && !inSortings.isEmpty() ) {
			
			Collections.sort( theFindResults, new FindResultComparator(inSortings));
		}
		
		
		final boolean isCheckSelects = inSelects != null && !inSelects.isEmpty();
		
		long theIdx = 0;
		for (FindResult aFindResult : theFindResults) {
			
			if ( inOffset > 0 ) {
				
				inOffset--;
				continue;
			
			} else if ( inLength != null && inLength-- < 1 ) {
				
				break;
			}
			
			aFindResult.index = theIdx++;
			
			if ( isCheckSelects && aFindResult.isDataJsonObject ) {
				
				final JsonObject theJo = aFindResult.jsonData.getAsJsonObject();
				
				final Collection<Entry<String,JsonElement>> theEntries = theJo.entrySet();
				
				for (Entry<String,JsonElement> anEntry : theEntries) {
					
					if ( !inSelects.contains( anEntry.getKey() ) ) {
						
						theJo.remove( anEntry.getKey() );
					}
				}
			}
			
			try {
			
				final EHandleResult theHr = inHandler.handle( aFindResult );
				if ( theHr == EHandleResult.STOP ) {
					break;
				}
				
			} catch (Exception e) {
				
				try {
					
					if ( EHandleResult.STOP == inHandler.handleException(aFindResult, e) ) {
						break;
					}
					
				} catch (Exception e2) {
					
					LOG.error( e2 );
					break;
				}
			}
		}
		
	}

	@Override
	public List<String> getCollectionNames() {
		
		final List<String> theCollections = new ArrayList<>();
		for(File aFile : Utils.listFiles(collectionsDir , true, false, false)) {
			theCollections.add( aFile.getName() );
		}
		Collections.sort( theCollections );
		return theCollections;
	}
	
	private File getDocumentFile(String inCollectionName, String inId, boolean inCreate) {
		return new File(getCollectionsDir(inCollectionName, inCreate), inId);
	}
	
	private File getCollectionsDir(String inCollectionName, boolean inCreate) {
	
		if ( Utils.isBlank( inCollectionName ) ) { return null; }
		final File theDir = new File( collectionsDir, inCollectionName );
		
		
		if (inCreate && !theDir.exists() ) {
			theDir.mkdirs();
		}
		
		return theDir;
	}
	
	private static class FindResultComparator implements Comparator<FindResult> {

		final List<String> sortings;
		
		private FindResultComparator(List<String> sortings) {
			this.sortings = sortings;
		}
		
		@Override
		public int compare(FindResult o1, FindResult o2) {
			
			for (String aSorting : sortings) {
				
				if ( aSorting == null || !aSorting.matches( Constants.REGEX_PROPERTYNAME_SORTABLE ) ) { continue; }
				
				final boolean isDescending= aSorting.charAt(0) == '-';
				
				final JsonPrimitive value1 = o1.getDataMember( aSorting ); 
				final JsonPrimitive value2 = o2.getDataMember( aSorting );

				if ( value1 == null || value2 == null ) {
					
					continue;
					
				} else if ( value1.isString() && value2.isString() ) {
					
					return value1.getAsString().compareTo( value2.getAsString() ) * ( isDescending?-1:1);
				
				} else if ( value1.isNumber() && value2.isNumber() ) {
					
					return value1.getAsBigDecimal().compareTo( value2.getAsBigDecimal() ) * ( isDescending?-1:1);
				
				} else if ( value1.isBoolean() && value2.isBoolean() ) {
					
					return Boolean.compare( value1.getAsBoolean(), value2.getAsBoolean()) * ( isDescending?-1:1);
				
				} else if ( !value1.isJsonNull() && value2.isJsonNull() ) {
					
					return 1 * ( isDescending?-1:1);

				} else if ( value1.isJsonNull() && !value2.isJsonNull() ) {
					
					return -1 * ( isDescending?-1:1);
				}
			}
			
			return 0;
		}
	}

}
