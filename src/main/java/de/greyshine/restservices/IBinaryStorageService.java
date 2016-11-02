package de.greyshine.restservices;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;

public interface IBinaryStorageService extends IApplicationService {
	
	IBinary create(InputStream inIs);
	IBinary update(String inId, InputStream inIs);
	IBinary read(String inId);
	IBinary delete(String inId);
	
	interface IBinary {
		
		String getId();
		InputStream getStream() throws IOException;
		
		Exception getException();
		boolean isExceptional();
		
		String getSha256();
		long getLength();
		
		boolean isFound();
		boolean isNotFound();
		
		ZonedDateTime getCreated();
		ZonedDateTime getUpdated();
		
		String getEtag();
		String getMime();
	}

}
