package com.itarverne.qrcode;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.*;
import java.io.File;

public class GeneratorTest {

	private static String PATH;
	
	@Before
	public void before() {
		PATH = System.getProperty("path");
	}

	@Test
	public void testWithParamMandatory() {
		try {
			String [] args = { "-p", PATH, "-c", "http://itarverne.com" };
			Generator.main(args);
			assertTrue(true);
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testQRCodeGenerated() {         
		try {
			String [] args = { "-p", PATH, "-c", "http://itarverne.com" };
			Generator.main(args);

			File dir = new File(PATH);
			File[] files = dir.listFiles();
			if(files == null || files.length != 1)
				fail("The QRCode is not generated");
			else {
				String fileName = files[0].getName();
				int lastIndexDot = fileName.lastIndexOf('.');
                
                String name = fileName.substring(0, lastIndexDot);
                String ext = fileName.substring(lastIndexDot);
                
                if(ext.equals(".png") && name.length() == 9) {
                	assertTrue(true);
                }
                assertFalse(false);
			}
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
	}	
}