package de.greyshine.restservices.util;

import org.junit.Test;

import junit.framework.Assert;

public class UtilsTest {
	
	@Test
	public void testExecuteSafe() {
		
		String fukkel = "fakkel";
		
		String fakkelUp = Utils.executeSafe(fukkel, x -> { 
			
			return fukkel.toUpperCase(); }
		);
		
		Assert.assertEquals( "FAKKEL" , fakkelUp);
	}

}
