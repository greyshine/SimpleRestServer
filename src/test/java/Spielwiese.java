import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Spielwiese {

	public static void main(String[] args) {

		
		System.out.println( new B() {

			{
				System.out.println( "1 "+ getS() );
				System.out.println( "2 "+super.getS() );
			}
			
			@Override
			public String getS() {
				return "OVER";
			}
			
		} );
		
		
		
		
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
