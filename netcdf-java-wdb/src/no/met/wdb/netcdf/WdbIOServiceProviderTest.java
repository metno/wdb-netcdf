package no.met.wdb.netcdf;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import ucar.nc2.iosp.IOServiceProvider;
import ucar.unidata.io.RandomAccessFile;

public class WdbIOServiceProviderTest {

	@Test
	public void testIsValidFile() {
		try {
			IOServiceProvider iosp = new WdbIOServiceProvider();
			assertTrue(iosp.isValidFile(new RandomAccessFile("share/test/valid.conf", "r")));
			assertFalse(iosp.isValidFile(new RandomAccessFile("share/test/invalid.conf", "r")));
		}
		catch (IOException e) {
			fail(e.getMessage());
		}
	}
		
}
