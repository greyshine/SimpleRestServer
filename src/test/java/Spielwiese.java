import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Spielwiese {

	public static void main(String[] args) throws IOException {

		URL u = new URL("https://www.google.de");
		u = new URL("http://127.0.0.1:8080/status");
		
		
		 HttpURLConnection connection = (HttpURLConnection) u
                 .openConnection();
		
		 connection.setRequestProperty( "user-agent" , "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
		 
		 connection.connect();
		 
		 System.out.println( connection.getResponseCode() );
		 System.out.println( connection.getResponseMessage() );
		 
		 System.out.println( connection.getContentLength() );
		 System.out.println( connection.getContentEncoding() );
		 System.out.println( connection.getContentType() );
		 
		 
		 
		try (InputStream is =  connection.getInputStream() ) {
			
			while( is.available()>0 ) {
				
				System.out.print((char)is.read());
			}
			
		} 
		
	}
	
	static class B {
		
		String s = "BWelt";
		
		public B() {
			
			System.out.println( "A "+ getS() );
			System.out.println( "B "+ getS() );
		}

		public String getS() {
			return s;
		}
	} 
	
	public  static void daysLeft() {
		
		double sum = 20000 * 0.8;
		
		LocalDate d = LocalDate.now();
		
		int daysLeft = 0;
		//daysLeft -= (30*0.8);
		
		while( d.getYear() < 2017 ) {
			
			boolean isWorkday = true;
			
			switch ( d.getDayOfWeek() ) {
			case FRIDAY:
			case SATURDAY:
			case SUNDAY:
				isWorkday = false;
				break;
			}
			
			final int x = (d.getMonth().getValue()*100)+d.getDayOfMonth();
			
			switch (x) {
			case 325:
			case 328:
			case 505:
			case 516:
			case 526:
			case 1003:
			case 1101:
			case 1226:
			case 1231:
				isWorkday = false;
				break;
			}
			
			if ( isUrlaub( d ) ) {
				isWorkday = false;
			}

			if ( isWorkday ) {
				daysLeft++;
			}
			
			d = d.plus( 1, ChronoUnit.DAYS );
		}
		
		System.out.println( "days: "+ daysLeft );
		
		d = LocalDate.now().minus(1, ChronoUnit.DAYS);
		int daycount = 0;
		
		while( d.getYear() < 2017 ) {
			
			d = d.plus(1,ChronoUnit.DAYS);
			
			switch ( d.getDayOfWeek() ) {
			case FRIDAY:
			case SATURDAY:
			case SUNDAY:
				continue;
			}
			
			
			
			
			
			daycount++;
			
			System.out.println( d +" "+ daycount +"; left="+ (daysLeft-daycount) );
		}
		
		
		
		
		System.out.println( sum +" / "+ daysLeft +"="+(sum / daysLeft) );
	}
	
	private static boolean isUrlaub(LocalDate d) {
		
		final int x = (d.getMonth().getValue()*100)+d.getDayOfMonth();
		
		if ( 517 <= x && x <= 529 ) {
		
			return true;
		
		} else if ( 919 <= x && x <= 923 ) {
			
			return true;
		} 
		
		
		return false;
	}

	static boolean isWorkDay(LocalDate d) {
		
		switch ( d.getDayOfWeek() ) {
		case FRIDAY:
		case SATURDAY:
		case SUNDAY:
			return false;
		}
		
		final int x = (d.getMonth().getValue()*100)+d.getDayOfMonth();
		
		switch (x) {
		case 325:
		case 328:
		case 505:
		case 516:
		case 526:
		case 1003:
		case 1101:
		case 1226:
		case 1231:
			return false;
		}
		
		
		return true;
	}
	
	

}
