package no.met.wdb.netcdf;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import static junit.framework.TestCase.*;

import no.met.wdb.DatabaseConnectionSpecification;
import no.met.wdb.ReadQuery;

import org.jdom.JDOMException;
import org.junit.Test;

class WdbConfigurationTest {

	@Test
	public void testConnectionSpecification() throws IOException, JDOMException {
		
		WdbConfiguration config = new WdbConfiguration(new File("test/local.wdb.xml"));
		
		DatabaseConnectionSpecification connectionSpec = config.getDatabaseConnectionSpecification();

		DatabaseConnectionSpecification expected = new DatabaseConnectionSpecification("wdb", "localhost", 5432, "vegardb");
		assertEquals(expected, connectionSpec);
	}
	
	@Test
	public void testImplicitWciUser() throws IOException, JDOMException {
	
		WdbConfiguration config = new WdbConfiguration(new File("test/local.wdb.xml"));
		assertEquals("vegardb", config.getWciUser());
	}
	
	@Test
	public void testReadQuery() throws IOException, JDOMException {
	
		WdbConfiguration config = new WdbConfiguration(new File("test/local.wdb.xml"));
		
		Vector<String> dataProvider = new Vector<String>();
		dataProvider.add("met.no eceps modification");
		String referenceTime = "2011-05-12 02:00:00+02";
		Vector<Integer> dataVersion = new Vector<Integer>();
		dataVersion.add(0);
		
		ReadQuery expected = new ReadQuery(dataProvider, null, referenceTime, null, null, null, dataVersion);
		assertEquals(expected.toString(), config.getReadQuery().toString());
	}
	
}
