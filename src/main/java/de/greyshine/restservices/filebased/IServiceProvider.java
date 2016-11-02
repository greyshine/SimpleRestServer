package de.greyshine.restservices.filebased;

import de.greyshine.restservices.IBinaryStorageService;
import de.greyshine.restservices.IJsonStorageService;

public interface IServiceProvider {

	IBinaryStorageService getBinaryStorageService();
	IJsonStorageService getDocumentStorageService();
	
}
