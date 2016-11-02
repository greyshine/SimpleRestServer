package de.greyshine.restservices;

import java.io.File;

import de.greyshine.restservices.filebased.IServiceProvider;

/**
 * Interface for a module of the core application.
 */
public interface IApplicationService {
	
	void init(IServiceProvider inServiceProvider, File inBasepath, String[] inArgs);
	void destroy();
	
}
