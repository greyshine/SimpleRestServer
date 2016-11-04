package de.greyshine.restservices;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import de.greyshine.restservices.client.Client.Response;
import de.greyshine.restservices.client.ClientFactory;
import de.greyshine.restservices.util.Job;
import de.greyshine.restservices.util.Utils;

public class PostSomeData {
	
	final int count = 100;
	final List<String> collectionNames = Arrays.asList( new String[]{"AlberEinsteins","NapoleonHills","GeorgeMichaels","MutterTheresas"} );
	final ClientFactory cfHttp = new ClientFactory( "http://localhost:7777" );
	final ClientFactory cfHttps = new ClientFactory( "https://localhost:7778", "admin", "admin" );
	final ClientFactory cf = cfHttps;
	
	@Test
	public void run() {
		
		for (int i = 0; i < count; i++) {

			final String theCollection = getCollectionName(); 
			
			Job theJob = new Job().put("id", UUID.randomUUID().toString() ).put("time", Utils.getGmtTime().toString()).put( "<html>" , "<html/>");
			
			final Response r = cf.sendPost( theCollection, theJob.build() );
			System.out.println( r );
			
			//
			
		}
		
		
	}

	private String getCollectionName() {
		
		return collectionNames.get( Utils.getRandomInt( collectionNames.size()-1 ) );
	}

}
