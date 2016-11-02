package de.greyshine.restservices.interceptors;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import de.greyshine.restservices.filters.GzipFilter;

public class GzipInterceptor implements WriterInterceptor {

	public static final String PROPERTY_KEY_GZIP = GzipInterceptor.class.getCanonicalName();

	@Override
	public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
		
		if ( Boolean.TRUE == context.getProperty( PROPERTY_KEY_GZIP ) && GzipFilter.didYouSolveTheBug ) {
		
			final GZIPOutputStream theGos = new GZIPOutputStream( context.getOutputStream() );
			context.setOutputStream( theGos );
			
//			context.setOutputStream( new OutputStream() {
//				
//				@Override
//				public void write(int b) throws IOException {
//					theGos.write( b );
//					
//				}
//
//				@Override
//				public void flush() throws IOException {
//					theGos.flush();
//				}
//
//				@Override
//				public void close() throws IOException {
//					theGos.flush();
//					theGos.finish();
//					theGos.close();
//				}
//			} );
			
		}
		
		context.proceed();
	}

}
