package de.greyshine.restservices.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import de.greyshine.restservices.Method;
import de.greyshine.restservices.util.HtmlUtils;
import de.greyshine.restservices.util.JsonUtils;
import de.greyshine.restservices.util.Utils;

public class Client {

	private final static Log LOG = LogFactory.getLog(Client.class);
	
	private Set<Closeable> closeables = new HashSet<>();

	private long timeToWaitBeforeConsume = 200L; 
	
	private String address;
	private String login, password;
	private String method;
	private Response response;
	
	private MultipartEntityBuilder multipartEntityBuilder = null;
	private HttpEntity entityParameter;
	
	private final List<BasicNameValuePair> headers = new ArrayList<>();
	private final List<BasicNameValuePair> entityParameters = new ArrayList<>();
	private final List<BasicNameValuePair> getParameters = new ArrayList<>();
	
	public Client() {}
	
	public Client(String inAddress) {
		address( inAddress );
	}

	Client address(String inAdress) {
		address = inAdress;
		return this;
	}
	
	public Client method(String inMethod) {

		method = inMethod == null ? method : inMethod.toUpperCase();
		return this;
	}
	
	public Client method(Method inMethod) {
		return method( inMethod == null ? null : inMethod.name() );
	}
	
	public Client credentials(String inLogin, String inPassword) {
		
		if ( inLogin == null ) {
			
			login = null;
			password = null;
		
		} else {
			
			login = inLogin;
			password = Utils.defaultIfNull(inPassword, "");
			
		}
		
		return this;
	}
	
	public Client timeToWaitBeforeConsume(Long inTimeToWaitBeforeConsume) {
		
		timeToWaitBeforeConsume = inTimeToWaitBeforeConsume == null || inTimeToWaitBeforeConsume < 1 ? -1 : inTimeToWaitBeforeConsume; 
		return this;
	}

	public Response send() {

		if (response != null) {

			response.close();
			response = null;
		}

		method = Utils.defaultIfNull( method ,  "GET");

		switch (method) {
		case "GET":

			return response = sendGet();
		
		case "POST":
			
			return response = sendPost();

		case "PUT":
			
			return response = sendPut();

		case "PATCH":
			
			return response = sendPatch();
			
		case "META":
			
			return response = sendMeta();

		default:

			throw new UnsupportedOperationException("method unsupported: " + method);
		}

	}

	private Response sendGet() {

		final HttpGet httpGet = new HttpGet( createUrl() );
		initCredentials( httpGet );

		try {

			return new Response(createHttpClient().execute(httpGet));

		} catch (Exception e) {

			return new Response(e);
		}
	}
	
	private Response sendPost() {
		
		final HttpPost httpPost = new HttpPost( createUrl() );
		
		initCredentials( httpPost );
		addHeaders( httpPost );
		addEntityParameters( httpPost );
		
		try {
			final CloseableHttpClient theChc = createHttpClient();
			final CloseableHttpResponse theChr = theChc.execute( httpPost );
			
			return new Response( theChr );
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return new Response(e);
		}
	}

	private Response sendPut() {
		
		final HttpPut httpPost = new HttpPut( createUrl() );
		
		initCredentials( httpPost );
		addHeaders( httpPost );
		addEntityParameters( httpPost );
		
		try {
			final CloseableHttpClient theChc = createHttpClient();
			final CloseableHttpResponse theChr = theChc.execute( httpPost );
			
			return new Response( theChr );
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return new Response(e);
		}
	}

	private Response sendPatch() {
		
		final HttpPatch httpPost = new HttpPatch( createUrl() );
		
		initCredentials( httpPost );
		addHeaders( httpPost );
		addEntityParameters( httpPost );
		
		try {
			final CloseableHttpClient theChc = createHttpClient();
			final CloseableHttpResponse theChr = theChc.execute( httpPost );
			
			return new Response( theChr );
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return new Response(e);
		}
	}

	private void addHeaders(HttpEntityEnclosingRequestBase inHttpRequest) {
		
		for (BasicNameValuePair aNvp : headers) {
			
			inHttpRequest.addHeader( aNvp.getName() , aNvp.getValue());
		}
	}
	private Response sendMeta() {

		final HttpMeta httpStatus = new HttpMeta( createUrl() );
		
		initCredentials( httpStatus );
		addEntityParameters( httpStatus );
		
		try {

			return new Response(createHttpClient().execute(httpStatus));

		} catch (Exception e) {

			return new Response(e);
		}
	}

	private void initCredentials(HttpRequestBase inHttpMethod) {
		
		String theAuthorisation = "";
		
		if ( login != null ) {
			
			final String theAuth = login +":"+ ( password==null?"":password );
			theAuthorisation = "BASIC "+ ( new String(Base64.getEncoder().encode( theAuth.getBytes() )));
		}
		
		inHttpMethod.setHeader("Authorization", theAuthorisation);
	}
	
	private void addEntityParameters(HttpEntityEnclosingRequestBase inHttpRequest) {
		
		int c = 0;
		c += !entityParameters.isEmpty() ? 1 : 0;
		c += multipartEntityBuilder != null ? 1 : 0; 
		c += entityParameter != null ? 1 : 0; 
		
		if ( c > 1 ) {
			
			throw new IllegalStateException("only one parameter allowed to be set");
		}
		
		if ( entityParameter != null ) {
		
			inHttpRequest.setEntity( entityParameter );
			
		} else if ( entityParameters != null ) {
			
			 try {
				
				 inHttpRequest.setEntity(new UrlEncodedFormEntity( entityParameters ));
			
			 } catch (UnsupportedEncodingException e) {
				
				 throw new RuntimeException(e);
				 
			}
		
		} else if ( multipartEntityBuilder != null ) {
		
			final HttpEntity theHttpEntity = multipartEntityBuilder.build();
			inHttpRequest.setEntity( theHttpEntity );
		} 
		
	}
	
	private URI createUrl() {
	
		if ( Utils.isBlank( address ) ) {
			throw new IllegalStateException("no address set");
		} else if ( !address.toLowerCase().startsWith("http://") && !address.toLowerCase().startsWith("https://") ) {
			
			address = "http://"+ address;
			
		}
		
		try {
			
			final URIBuilder theUb = new URIBuilder( address );
			
			for (BasicNameValuePair aNvp : getParameters) {
				
				theUb.addParameter( aNvp.getName() == null ? "" : aNvp.getName() , aNvp.getValue() == null ? "" : aNvp.getValue() );
			}
			
			return theUb.build();

		} catch (Exception e) {
			
			throw new IllegalStateException(e);
		}
	}

	private static CloseableHttpClient createHttpClient() {

		try {

			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, TRUSTMANAGERS_ALL, new java.security.SecureRandom());
			final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			return HttpClients.custom().setSSLSocketFactory(sslsf).build();

		} catch (Exception e) {

			throw Utils.toRuntimeException(e);
		}
	}

	public class Response implements Closeable {

		public final CloseableHttpResponse httpResponse;
		private final Map<String, List<String>> headers = new LinkedHashMap<>();
		public final String statusText;
		public final String statusLine;
		public final int statusCode;
		public final Exception exception;
		public final long contentLength;
		
		private InputStream inputStream;
		private boolean isConsumed = false;
		private String text;
		
		private Response(Exception inException) {

			statusLine = null;
			statusCode = -1;
			statusText = inException == null ? null : inException.toString();
			exception = inException;

			httpResponse = null;

			contentLength = -1;
		}

		private Response(CloseableHttpResponse inCloseableHttpResponse) throws IllegalStateException, IOException {

			httpResponse = inCloseableHttpResponse;

			statusLine = inCloseableHttpResponse.getStatusLine().getReasonPhrase();
			statusCode = inCloseableHttpResponse.getStatusLine().getStatusCode();
			
			statusText = statusCode +"; "+statusLine;

			exception = null;

			for (Header aHeader : inCloseableHttpResponse.getAllHeaders()) {

				List<String> theHeaders = headers.get(aHeader.getName());
				headers.put(aHeader.getName(), theHeaders != null ? theHeaders : (theHeaders = new ArrayList<>()));
				theHeaders.add(Utils.defaultIfNull(aHeader.getValue(), ""));

				LOG.debug("resp.header "+ aHeader.getName() + "="+ aHeader.getValue() );
			}
			
			try {
			
				System.out.println( Arrays.asList( inCloseableHttpResponse.getHeaders( "Content-Encoding" ) ) );
				
			} catch (Exception e) {
				System.err.println( e );
			}
			
						
			
			final HttpEntity theHe = inCloseableHttpResponse.getEntity();

			
			if (theHe != null) {

				contentLength = theHe.getContentLength();
				inputStream = theHe.getContent();
				
				// There is already a org.apache.http.client.entity.LazyDecompressingInputStream involved
				//if ( isHeaderValue( HttpHeader.CONTENT_ENCODING, "gzip" ) ) {
				//	inputStream = new GZIPInputStream( inputStream );
				//}

			} else {

				contentLength = -1;
			}
		}
		
		public boolean isHeaderValue(String inHeaderName, String inValue) {
			
			for( String aValue : getHeaders(inHeaderName) ) {
				
				if ( aValue != null && aValue.equals( inValue ) ) {
					return true;
				}
			}
			
			return false;
		}

		public <T> T consume(IStreamConsumer<T> inConsumer) throws IOException {
			
			synchronized ( this ) {
			
				if ( isConsumed ) {
					throw new IOException("consume was already invoked");
				}
				
				isConsumed = true;
			}
			
			if ( Client.this.timeToWaitBeforeConsume > 0 ) {
				LOG.debug( "wating before consume: "+ Client.this.timeToWaitBeforeConsume +" ms" );
				Utils.threadSleep( Client.this.timeToWaitBeforeConsume );
			}
			
			return inConsumer == null ? null : inConsumer.read( inputStream );
		}

		public String getContentType() {
			return getHeader( HtmlUtils.HEADER_CONTENT_TYPE );
		}
		public String getContentEncoding() {
			return getHeader( HtmlUtils.HEADER_CONTENT_ENCODING );
		}
		
		public List<String> getHeaders(String inHeaderName) {

			final List<String> theHeaderValues = headers.get(inHeaderName);

			return theHeaderValues != null ? theHeaderValues : Collections.emptyList();
		}

		public String getHeader(String inHeaderName) {

			final List<String> theHeaderValues = headers.get(inHeaderName);

			return theHeaderValues == null || theHeaderValues.isEmpty() ? null : theHeaderValues.get(0);
		}

		boolean isExceptional() {

			return exception != null;
		}

		public void close() {

			Utils.close(httpResponse, inputStream);
		}

		@Override
		protected void finalize() throws Throwable {
			close();
		}

		@Override
		public String toString() {
			return "Response [status=" + statusCode+";"+ statusLine + ", exception=" + exception + "]";
		}

		public boolean isStatusOk() {
			return isStatus(200) || isStatus( 304 );
		}

		private boolean isStatus(int inStatus) {
			return statusCode == inStatus;
		}

		public InputStream getInputStream() {
			return inputStream;
		}

		public JsonObject consumeJsonObject() throws IOException {
			final JsonElement theJe = consume( JsonConsumer.getInstance() );
			return theJe.isJsonObject() ? theJe.getAsJsonObject() : null;
		}
		
		public String consumeText() throws IOException {
			return text = consume( TextConsumer.getInstance() );
		}

		public String getText() {
			
			if ( isConsumed ) {
				return text;
			}
			
			try {
				
				return consumeText();
			
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public JsonElement getJson() {
			
			final String theText = getText();
			
			return Utils.isBlank( theText ) ? JsonNull.INSTANCE : Utils.JSONPARSER.parse( theText );
		}
		
		public JsonObject getJsonObject() {
			
			final JsonElement theJe = getJson();
			
			return theJe == null || !theJe.isJsonObject() ? null : theJe.getAsJsonObject();
		}
		

	}

	/**
	 * read:
	 * http://www.rgagnon.com/javadetails/java-fix-certificate-problem-in-HTTPS.
	 * html
	 */
	private static final TrustManager[] TRUSTMANAGERS_ALL = new TrustManager[] { new X509TrustManager() {

		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(X509Certificate[] certs, String authType) {
		}

		public void checkServerTrusted(X509Certificate[] certs, String authType) {
		}

	} };

	public void reset() {

		for (Closeable c : new HashSet<>(closeables)) {
			
			closeables.remove( c );
			Utils.close( c );
		}
		
		Utils.close(response);
		
		response = null;
	}
	
	@Override
	protected void finalize() throws Throwable {
		
		reset();
	}
	public Response getResponse() {

		return response != null ? response : (response = send());
	}

	@NotThreadSafe
	public class HttpMeta extends HttpEntityEnclosingRequestBase {

		public final static String METHOD_NAME = "META";

		/**
		 * @throws IllegalArgumentException
		 *             if the uri is invalid.
		 */
		public HttpMeta(URI inUri) {
			super();
			setURI( inUri );
		}

		@Override
		public String getMethod() {
			return METHOD_NAME;
		}

	}

	public Client addGetParameter(String inName, String inValue) {
		
		if ( inName == null ) { return this; }
		
		getParameters.add( new BasicNameValuePair(inName, inValue==null?"":inValue  ) );
		
		return this;
	}
	
	public Client setEntityParameter(File inFile) {
		
		return setEntityParameter(inFile, null);
	}
	
	public Client setEntityParameter(File inFile, String inContentType) {
		
		if ( entityParameter != null || !entityParameters.isEmpty() || multipartEntityBuilder != null ) {
			throw new IllegalStateException("parameter is already set");
		}
		
		entityParameter = new FileEntity(inFile, parseContentType( inContentType ) );
		
		return this;
	}

	public Client setEntityParameter(byte[] inData) {
		
		return setEntityParameter(inData, null);
	}
	
	public Client setEntityParameter(String inString) {
		
		return setEntityParameter( inString == null ? null : inString.getBytes( Utils.CHARSET_UTF8 ) , null);
	}
	
	public Client setEntityParameter(JsonElement inJe) {
		 
		return setEntityParameter( JsonUtils.jsonToString( inJe == null ? JsonNull.INSTANCE : inJe , false) );
	}
	
	public Client setEntityParameter(byte[] inData, String inContentType) {
		
		if ( entityParameter != null || !entityParameters.isEmpty() || multipartEntityBuilder != null ) {
			throw new IllegalStateException("parameter is already set");
		}
		
		entityParameter = new ByteArrayEntity(inData, parseContentType( inContentType ) );
		
		return this;
	}

	public Client setEntityParameter(InputStream inIs) {
		
		return setEntityParameter(inIs, null);
	}
	
	public Client setEntityParameter(InputStream inIs, String inContentType) {
		
		if ( entityParameter != null || !entityParameters.isEmpty() || multipartEntityBuilder != null ) {
			throw new IllegalStateException("parameter is already set");
		}
		
		entityParameter = new InputStreamEntity(inIs, parseContentType( inContentType ) );
		
		return this;
	}
	
	private ContentType parseContentType(String inContentType) {
		
		if ( inContentType == null || inContentType.trim().isEmpty() ) { return null; }
		return ContentType.parse( inContentType );
	}
	
	public Client addEntityParameter(String inName, String inValue) {
		
		inName = inName == null ? "" : inName;
		inValue = inValue == null ? "" : inValue;
		
		entityParameters.add( new BasicNameValuePair(inName, inValue) );
		
		return this;
	}
	
	public Client addMultipartParameter(String inName, String inValue) {
		
		if ( inName == null ) { return this; }
		else if ( entityParameter != null || !entityParameters.isEmpty() ) { throw new IllegalStateException("another parameter type already set"); }
		
		multipartEntityBuilder = multipartEntityBuilder != null ? multipartEntityBuilder : MultipartEntityBuilder.create();
		
		multipartEntityBuilder.addTextBody( inName, inValue == null ? "" : inValue, ContentType.DEFAULT_TEXT);
		
		return this;
	}

	public Client addMultipartParameter(String inName, InputStream inIs, String inContentType, String inFilename) {
		
		if ( inName == null ) { return this; }
		else if ( entityParameter != null || !entityParameters.isEmpty() ) { throw new IllegalStateException("another parameter type already set"); }
		
		inIs = inIs != null ? inIs : new ByteArrayInputStream( new byte[0] );
		
		multipartEntityBuilder = multipartEntityBuilder != null ? multipartEntityBuilder : MultipartEntityBuilder.create();
		multipartEntityBuilder.addBinaryBody( inName, inIs, parseContentType(inContentType), inFilename );
		
		return this;
	}
	
	public Client headerContentTypeJson() {
		
		return header("Content-Type", "application/json; charset=UTF-8");
	}
	
	public Client header(String inName, String inValue) {
		
		if ( inName == null ) { return this; }
		inValue = Utils.trimToEmpty(inValue);
		
		headers.add( new BasicNameValuePair(inName, inValue) );
		
		return this;
	}
	
	public static interface IStreamConsumer<T> {
		
		T read(InputStream inIs) throws IOException;
	}
	
	public static class BytesConsumer implements IStreamConsumer<byte[]> {
		
		@Override
		public byte[] read(final InputStream inIs) throws IOException {
			
			LOG.debug( this +" consuming from "+ inIs );
			
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			
			final InputStream theIs = Utils.isDebugLogEnabled() ? inIs : new Utils.LoggingInputStream( inIs ); 
			final OutputStream theOs = Utils.isDebugLogEnabled() ? baos : new Utils.LoggingOutputStream( baos ); 
			
			try {

				Utils.copy( theIs , theOs, false, true);
				
			} catch (Exception e) {
				
				LOG.error( e );
			}
			
			return baos.toByteArray();
		}
	}
}
