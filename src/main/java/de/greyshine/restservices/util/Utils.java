package de.greyshine.restservices.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class Utils {

	private final static Log LOG = LogFactory.getLog(Utils.class);
	
	public static final String HEADER_X_Total_Count = "X-Total-Count";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";

	public static final int EOF_STREAM = -1;

	public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

	public static final ZoneId ZONEID_GMT = ZoneId.of("GMT");

	public static final JsonParser JSONPARSER = new JsonParser();

	
	public static final int KB_IN_BYTES = 1024;
	public static final int MB_IN_BYTES = 1024 * KB_IN_BYTES;
	public static final int GB_IN_BYTES = 1024 * MB_IN_BYTES;
	public static final int TB_IN_BYTES = 1024 * GB_IN_BYTES;
	
	public static final long MILLIS_1_SECOND = 1000L;
	public static final long MILLIS_1_MINUTE = MILLIS_1_SECOND * 60L;
	public static final long MILLIS_1_HOUR = MILLIS_1_MINUTE * 60L;
	public static final long MILLIS_1_DAY = MILLIS_1_HOUR * 24L;

	public static final DateTimeFormatter DF_LEX = new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmmssSSS")
			.toFormatter();

	private static final BigDecimal BD_1024 = new BigDecimal("1024");

	private static final File[] EMPTY_FILES = new File[0];

	private static final ThreadLocal<byte[]> TL_BUFFERS = new ThreadLocal<byte[]>() {
		@Override
		protected byte[] initialValue() {
			return new byte[4 * 1024];
		}
	};
	
	public static boolean isDebugLogEnabled() {
		return LOG.isDebugEnabled();
	}

	private static final DateTimeFormatter DTF_HTTPDATE = DateTimeFormatter
			.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'");

	public static RuntimeException toRuntimeException(Exception e) {
		return e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
	}
	
	public static String createUniqueId() {
		return ZonedDateTime.now().format( DF_LEX )+"-"+ UUID.randomUUID().toString();
	}

	public static <T> T defaultIfNull(T inObject, T inDefault) {

		return inObject == null ? inDefault : inObject;
	}

	@SuppressWarnings("unchecked")
	public static <T> T castSafe(Object inValue) {

		try {

			return (T) inValue;

		} catch (Exception e) {

			return null;
		}

	}

	public static String defaultIfBlank(String inValue, String inDefault) {

		return isNotBlank(inValue) ? inValue : inDefault;
	}

	public static boolean isNotBlank(String inValue) {

		return !isBlank(inValue);
	}

	public static boolean isBlank(String inValue) {

		if (inValue == null) {
			return true;
		}

		for (char c : inValue.toCharArray()) {
			if (!Character.isWhitespace(c)) {
				return false;
			}
		}

		return true;
	}

	public static void close(Closeable... inCloseables) {

		if (inCloseables != null) {

			for (Closeable closeable : inCloseables) {

				try {

					LOG.debug( "closing: "+ closeable );
					closeable.close();

				} catch (Exception e) {
					// swallow
				}
			}
		}
	}

	public static byte[] getBytes(InputStream inIs) throws IOException {

		if (inIs == null) {
			return null;
		}

		final ByteArrayOutputStream theBaos = new ByteArrayOutputStream();

		copy(inIs, theBaos);

		return theBaos.toByteArray();

	}

	public static long copy(InputStream inInputStream, OutputStream inOutputStream) throws IOException {
		return copy(inInputStream, inOutputStream, false, false);
	}
	
	public static void threadSleep(Long inTime) {
		
		if ( inTime == null || inTime < 1 ) { return; }
		
		final long theEndtime = System.currentTimeMillis() + inTime;
		
		while( theEndtime > System.currentTimeMillis() ) {
		
			final long ttw = theEndtime - System.currentTimeMillis(); 
			
			if ( ttw > 0 ) {
				try {
					Thread.sleep( ttw );
				} catch (Exception e) {
					LOG.info( e );
				}	
			}		
		}
	}

	public static long copy(InputStream inInputStream, OutputStream inOutputStream, boolean inCloseIs,
			boolean inCloseOs) throws IOException {

		final byte[] buffer = TL_BUFFERS.get();

		long count = 0;
		int n = 0;
		
		try {

			while (EOF_STREAM != (n = inInputStream.read(buffer))) {
				inOutputStream.write(buffer, 0, n);
				count += n;
			}

			inOutputStream.flush();
			return count;

		} catch (IOException e) {

			throw e;
			
		} finally {

			if (inCloseIs) {
				close(inInputStream);
			}
			if (inCloseOs) {
				close(inOutputStream);
			}
		}
	}

	public static String getSha256(File inData) {

		InputStream is = null;

		try {
			
			return getSha256( is = new FileInputStream( inData ) );
			
		} catch (Exception e) {
			return null;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	public static String getSha256(InputStream is) {
		
		try {
			
			final byte[] hash = DigestUtils.sha256(is);
			
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				if ((0xff & hash[i]) < 0x10) {
					hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
				} else {
					hexString.append(Integer.toHexString(0xFF & hash[i]));
				}
			}
			
			return hexString.toString();
			
		} catch (Exception e) {
			return null;
		}
	}

	public static Long parseLongSafe(String inValue, Long inDefault) {

		try {

			return Long.parseLong(inValue);

		} catch (Exception e) {

		}

		return inDefault;
	}

	public static String trimToEmpty(String inValue) {
		return inValue == null ? "" : inValue.trim();
	}

	public static String trimToNull(String inValue) {
		inValue = inValue == null ? null : inValue.trim();
		return inValue == null || inValue.isEmpty() ? null : inValue;
	}

	public static String toStringDataSize(Long inSize) {

		if (inSize == null) {

			return null;

		} else if (inSize < 1) {

			return inSize.toString();
		}

		if (inSize < 1024) {

			return inSize + " bytes";
		}

		BigDecimal theSize = new BigDecimal(inSize).divide(Utils.BD_1024, 1, RoundingMode.HALF_UP);

		if (BD_1024.compareTo(theSize) > 0) {

			return theSize.setScale(2, RoundingMode.HALF_UP).toPlainString() + " KB";
		}

		theSize = theSize.divide(Utils.BD_1024, 10, RoundingMode.HALF_UP);

		if (BD_1024.compareTo(theSize) > 0) {

			return theSize.setScale(2, RoundingMode.HALF_UP).toPlainString() + " MB";
		}

		theSize = theSize.divide(Utils.BD_1024, 10, RoundingMode.HALF_UP);

		if (BD_1024.compareTo(theSize) > 0) {

			return theSize.setScale(2, RoundingMode.HALF_UP).toPlainString() + " GB";
		}

		theSize = theSize.divide(Utils.BD_1024, 10, RoundingMode.HALF_UP);

		if (BD_1024.compareTo(theSize) > 0) {

			return theSize.setScale(2, RoundingMode.HALF_UP).toPlainString() + " TB";
		}

		theSize = theSize.divide(Utils.BD_1024, 10, RoundingMode.HALF_UP);

		return theSize.setScale(2, RoundingMode.HALF_UP).toPlainString() + " PB";
	}

	public static List<File> listFiles(File inDir, boolean inCollectDirs, boolean inCollectFiles,
			boolean inWalkDirectories) {

		if (inDir == null || !inDir.isDirectory()) {
			return new ArrayList<>(0);
		}

		final List<File> fs = new ArrayList<>();
		final List<File> qs = new ArrayList<>(1);
		qs.add(inDir);

		while (!qs.isEmpty()) {

			inDir = qs.remove(0);

			for (File f : defaultIfNull(inDir.listFiles(), EMPTY_FILES)) {

				if (f.isDirectory() && inCollectDirs) {
					fs.add(f);
				} else if (f.isFile() && inCollectFiles) {
					fs.add(f);
				}

				if (inWalkDirectories && f.isDirectory()) {
					qs.add(f);
				}
			}
		}

		return fs;
	}

	public static String getReadableTime(Long inMillis) {

		if (inMillis == null) {
			return null;
		}

		final boolean isNegative = inMillis < 0;

		inMillis = !isNegative ? inMillis : inMillis * -1;

		final StringBuilder s = new StringBuilder(isNegative ? "-" : "");

		final long theDays = inMillis / MILLIS_1_DAY;

		if (theDays != 0) {

			s.append(theDays).append("d");
			inMillis -= (theDays * MILLIS_1_DAY);
		}

		final long theHours = inMillis / MILLIS_1_HOUR;

		if (theDays != 0 || theHours != 0) {

			s.append(theHours).append("h");
			inMillis -= (theHours * MILLIS_1_HOUR);
		}

		final long theMins = inMillis / MILLIS_1_MINUTE;

		if (theDays != 0 || theHours != 0 || theMins != 0) {

			s.append(theMins).append("m");
			inMillis -= (theMins * MILLIS_1_MINUTE);
		}

		final long theSecs = inMillis / MILLIS_1_SECOND;

		if (theDays != 0 || theHours != 0 || theMins != 0 || theSecs != 0) {

			s.append(theSecs).append("s");
			inMillis -= (theSecs * MILLIS_1_SECOND);
		}

		s.append(inMillis).append("ms");

		return s.toString().trim();
	}

	public static File defaultIfNotFile(File inFile, File inDefault) {
		return inFile == null || !inFile.isFile() ? inDefault : inFile;
	}

	public static String dateToHttpDate(ZonedDateTime inDate) {

		inDate = inDate != null ? inDate : ZonedDateTime.now();

		inDate = inDate.minus((long) inDate.get(ChronoField.OFFSET_SECONDS), ChronoUnit.SECONDS);

		return inDate.format(DTF_HTTPDATE);
	}

	public static void main(String[] args) {
		
		System.out.println( getGmtTime() );
		System.out.println( ZonedDateTime.now(ZoneOffset.UTC) );
		System.out.println( getZonedDateTime( System.currentTimeMillis() ) );
		
		System.out.println( "xxx" );

		System.out.println(ZonedDateTime.now());
		System.out.println(getGmtTime());
		System.out.println("xx");

		System.out.println(toMilliSeconds(null));
		System.out.println(toMilliSeconds(ZonedDateTime.now()));
		System.out.println(toMilliSeconds(LocalDateTime.now()));
		System.out.println(dateToHttpDate(ZonedDateTime.now()));

	}

	public static long toMilliSeconds(Object inValue) {

		if (inValue == null) {
			return System.currentTimeMillis();
		} else if (inValue instanceof Long) {
			return (long) inValue;
		} else if (inValue instanceof ZonedDateTime) {

			final ZonedDateTime dt = (ZonedDateTime) inValue;

			return (dt.toEpochSecond() * 1000) + dt.get(ChronoField.MILLI_OF_SECOND);
		} else if (inValue instanceof LocalDateTime) {

			final LocalDateTime dt = (LocalDateTime) inValue;
			return (dt.toEpochSecond(ZoneOffset.ofHours(1)) * 1000) + dt.get(ChronoField.MILLI_OF_SECOND);
		}

		throw new UnsupportedOperationException(
				"cannot fetch milliseconds for " + inValue.getClass().getCanonicalName() + ": " + inValue);
	}

	public static ZonedDateTime getGmtTime() {
		return ZonedDateTime.now(ZONEID_GMT);
	}

	public static File getCanonicalFileSafe(File inFile, boolean inReturnAbsoluteInstead) {

		try {

			return inFile == null ? null : inFile.getCanonicalFile();

		} catch (Exception e) {
			return !inReturnAbsoluteInstead || inFile == null ? inFile : inFile.getAbsoluteFile();
		}
	}

	public static void initLog4j(File inLog4jXmlFile) {

		if (inLog4jXmlFile == null) {
			return;
		} else if (!inLog4jXmlFile.isFile()) {
			LOG.debug("log4j.xml not found: " + getCanonicalFileSafe(inLog4jXmlFile, true));
			return;
		}

		try {

			Class.forName("org.apache.commons.logging.LogFactory");
			Class.forName("org.apache.log4j.xml.DOMConfigurator");

			DOMConfigurator.configure(inLog4jXmlFile.toURI().toURL());
			LOG.debug("Reconfigured log4j.xml: " + inLog4jXmlFile);

		} catch (final Exception e) {

			LOG.error("unable to load reconfigure log4j: " + inLog4jXmlFile);

			return;
		}
	}

	public static JsonObject toJsonObject(JsonElement inJsonElement) {
		
		return inJsonElement == null || !inJsonElement.isJsonObject() ? null : inJsonElement.getAsJsonObject();
		
	}
	
	public static ZonedDateTime getZonedDateTime(long inValue) {
		
		return new Date(inValue).toInstant().atZone( ZoneOffset.UTC );
	}

	public static void deleteQuietly(File... inFiles) {
		
		inFiles = defaultIfNull(inFiles, new File[0]);
		
		for (File f : inFiles) {
			
			try {
				FileUtils.forceDelete(f);
			} catch (IOException e) {
				//swallow
			}
			
		}
		
	}

	public static JsonObject toJsonObject(String inString) {
		return inString == null ? null : JSONPARSER.parse( inString ).getAsJsonObject();
	}

	public static boolean isFirstChar(String inString, char c) {
		
		try {
			
			return inString.charAt(0) == c;
			
		} catch(Exception e) {
			
			return false;
			
		}
	} 
	
	public static class LoggingInputStream extends InputStream  {
		
		final Log log  = LogFactory.getLog( LoggingInputStream.class );
		
		final InputStream is;
		private long cnt = 0;

		public LoggingInputStream(InputStream is) {
			this.is = is;
		}
		
		@Override
		public void close() throws IOException {
			LOG.debug( " < CLOSE<< after "+ cnt +" bytes" );
			is.close();
		}

		@Override
		public int read() throws IOException {
			
			int r = is.read();
			
			String txt = " < "+(++cnt)+" < "+ r +" < ";
			
			try {
				
				txt +=  ( r != -1 ? "'"+ (char)r +"'" : "<EOF>");
				
			} catch (Exception e) {
				
				txt += "?";
			}
			
			LOG.debug( txt );
			return r;
		}
	}

	public static class LoggingOutputStream extends OutputStream  {
		
		final Log log  = LogFactory.getLog( LoggingOutputStream.class );
		
		final OutputStream os;
		private long cnt = 0;
		
		public LoggingOutputStream(OutputStream os) {
			this.os = os;
		}
		
		@Override
		public void flush() throws IOException {
			LOG.debug( " > FLUSH > after "+ cnt +" bytes" );
			os.flush();
		}



		@Override
		public void close() throws IOException {
			LOG.debug( " > CLOSE >> after "+ cnt +" bytes" );
			os.close();
		}

		@Override
		public void write(int r) throws IOException {
			
			String txt = " > "+(++cnt)+" > "+ r +" ";
			
			try {
				
				txt += "'"+(char)r +"'";
				
			} catch (Exception e) {
				
				txt += "?";
			}
			
			LOG.debug( txt );
			os.write(r);
		}
	}

	public static boolean isEquals(Object o1, Object o2) {
		
		if ( o1 == o2 ) { return true; }
		else if ( o1 == null || o2 == null  ) { return false; }
		return o1.equals( o2 );
	}

	public static String toString(Object o) {
		
		if ( o instanceof Throwable ) {
			
			o = o.getClass().getCanonicalName()+" [message="+ ((Throwable)o).getMessage() +"]";			
		}
		
		
		return String.valueOf( o );
	}
	
	public static Kvp<String,String> evaluateUserPassword(HttpServletRequest inReq) {
	
		String theAuthorisationHeader = inReq.getHeader("Authorization");
		
		if (theAuthorisationHeader == null || !theAuthorisationHeader.toUpperCase().startsWith("BASIC ")) {
			return new Kvp<String,String>();
		}
		
		String theUserAndPassword = inReq.getHeader("Authorization").substring(5).trim();
		theUserAndPassword = new String(java.util.Base64.getDecoder().decode(theUserAndPassword));
		final int idxColon = theUserAndPassword.indexOf(':');

		final String theUserCandidate = idxColon < 1 ? null
				: trimToNull(theUserAndPassword.substring(0, idxColon));
		final String thePassword = theUserCandidate == null ? null
				: trimToEmpty(theUserAndPassword.substring(idxColon+1));
		
		return new Kvp<String,String>( theUserCandidate, thePassword );
	}
	
	public static class Kvp<T,S> {
		
		public T key;
		public S value;
		public Kvp() {}
		public Kvp(T key, S value) {
			this.key = key;
			this.value = value;
		}
		@Override
		public String toString() {
			return "Kvp [key=" + key + ", value=" + value + "]";
		}
	}
	
	
	


}
