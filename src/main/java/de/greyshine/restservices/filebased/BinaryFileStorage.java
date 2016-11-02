package de.greyshine.restservices.filebased;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

import de.greyshine.restservices.IBinaryStorageService;
import de.greyshine.restservices.IStatusReportable;
import de.greyshine.restservices.util.Job;
import de.greyshine.restservices.util.Utils;
import de.greyshine.restservices.util.Wrapper;

public class BinaryFileStorage implements IBinaryStorageService, IStatusReportable {
	
	static final Log LOG = LogFactory.getLog( BinaryFileStorage.class );
	
	protected File basepath;
	private File binaryDir;
	protected IServiceProvider serviceProvider;

	@Override
	public void init(IServiceProvider inServiceProvider, File inBasepath, String[] inArgs) {
		
		this.basepath = inBasepath;
		this.serviceProvider = inServiceProvider;
		
		binaryDir = new File(basepath, "data/files");
		binaryDir.mkdirs();
	}
	
	@Override
	public void destroy() {
	}
	
	@Override
	public IBinary create(InputStream inIs) {
		
		if ( inIs == null ) {
			
			return new Binary( new IllegalArgumentException( "stream is null" ) );
		}
		
		final String theId = Utils.createUniqueId();
		final File theFile =  getFile( theId , true );
		
		try {
			
			copyInputStreamToFile(inIs, theFile);
			
		} catch (IOException e) {
			
			return new Binary(e);
		}
		
		return new Binary( theId , theFile);
	}

	@Override
	public IBinary read(String inId) {
		
		final File theFile = getFile(inId, false);
		
		if ( theFile == null ) {
			return new Binary( new FileNotFoundException( "no such file with id="+ inId ) );
		}
		
		return new Binary(theFile.getName(), theFile);
	}
	
	@Override
	public IBinary update(String inId, InputStream inIs) {

		if ( inIs == null ) {
			return new Binary( new IllegalArgumentException( "stream is null" ) );
		}
		
		final File theFile = getFile(inId, false);
		
		if ( theFile == null ) {
			return new Binary( new FileNotFoundException( "no such file with id="+ inId ) );
		}
		
		try {
			
			copyInputStreamToFile(inIs, theFile);
		
		} catch (IOException e) {
			
			return new Binary(e);
		}
		
		return new Binary( inId, theFile );
	}

	@Override
	public IBinary delete(String inId) {

		final File theFile = getFile(inId, false);
		
		if ( theFile == null ) {
			return new Binary( inId, null );
		}
		
		Utils.deleteQuietly( theFile );
		
		return theFile.exists() ? new Binary( new IllegalStateException("file was not deleted; id="+ inId) ) : new Binary(theFile.getName(), theFile) {
			@Override
			public boolean isFound() {
				return true;
			}
			@Override
			public boolean isNotFound() {
				return true;
			}
		};
	}

	private File getFile( String inId , boolean inCreateDirs ) {
		
		if ( Utils.isBlank( inId ) ) { return null; }
		
		// TODO create hash for folders
		
		final File theFile = new File( binaryDir, inId );
		
		if ( inCreateDirs && !theFile.getParentFile().exists() ) {
			
			theFile.getParentFile().mkdirs();
		}
	
		return !theFile.isDirectory() ? theFile : null;
	}
	
	private void copyInputStreamToFile( InputStream inIs, File inFile ) throws IOException {
		
		FileOutputStream theFos = null;
		
		try {
			
			Utils.copy(inIs, theFos=new FileOutputStream(inFile), false, true );
			
		} catch (IOException e) {
			
			Utils.close( theFos );
			Utils.deleteQuietly( inFile );
			
			throw e;
		}
	}
	

	private class Binary implements IBinary {
		
		final String id;
		final Exception exception;
		final File file;
		
		private Binary(String id, File file) {
			this.id=id;
			this.exception = null;
			this.file = file;
		}
		
		private Binary(Exception exception) {
			
			this.id = null;
			this.exception = exception;
			this.file = null;
		}
		
		
		@Override
		public String getId() {
			return id;
		}

		@Override
		public InputStream getStream() throws IOException {
			
			final InputStream is = new InputStream() {
				
				final FileInputStream fis = new FileInputStream( file );
				
				{
					LOG.debug(  "STREAM>: "+ file );
				}
				
				@Override
				public int read() throws IOException {
					
					final int r = fis.read();
					
					LOG.debug(  "send>> "+ (char)r );
					
					return r;
				}
				
				public void close() {
					LOG.debug(  "DONE>: "+ file );
					Utils.close( fis );
				}

				@Override
				protected void finalize() throws Throwable {
					close();
				}
			};
			
			return isNotFound() ? null : is;
		}

		@Override
		public Exception getException() {
			return exception;
		}

		@Override
		public boolean isExceptional() {
			return getException() != null;
		}

		@Override
		public String getSha256() {
			return Utils.getSha256( file );
		}

		@Override
		public long getLength() {
			return isNotFound() ? -1 : file.length();
		}

		@Override
		public boolean isFound() {
			return file != null && file.isFile() && file.canRead();
		}

		@Override
		public boolean isNotFound() {
			return !isFound();
		}

		@Override
		public ZonedDateTime getCreated() {
			return getUpdated();
		}

		@Override
		public ZonedDateTime getUpdated() {
			return isNotFound() ? null : Utils.getZonedDateTime( file.lastModified() );
		}

		@Override
		public String getEtag() {
			return null;
		}

		@Override
		public String getMime() {
			return null;
		}
	}


	@Override
	public JsonObject createStatusReport() {
		
		final Job theJob = new Job();
		theJob.put( "dir" , Utils.getCanonicalFileSafe(binaryDir, true).getAbsolutePath() );
		
		final Wrapper<Long> fileCount = new Wrapper<Long>(0L);
		final Wrapper<Long> fileSizes = new Wrapper<Long>(0L);
		
		
		
		try {
			Files.walkFileTree( binaryDir.toPath() , new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					
					fileCount.value++;
					fileSizes.value += file.toFile().length();
					
					return FileVisitResult.CONTINUE;
				}
				
			} );
			
			theJob.put( "files" , fileCount.value);
			theJob.put( "size" , fileSizes.value);
			theJob.put( "size-readable" , Utils.toStringDataSize( fileSizes.value ));
			
		} catch (IOException e) {
			theJob.put("error-on-file-traversing", e.getMessage());
		}
		
		
		
		return theJob.build();
	}
}
