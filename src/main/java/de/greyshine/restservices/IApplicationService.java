package de.greyshine.restservices;

import java.io.File;

/**
 * Interface for a module of the core application.
 */
public interface IApplicationService {
	
	void init(File inBasepath, String[] inArgs);
	void destroy();
	
}
