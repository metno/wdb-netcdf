package no.met.wdb.netcdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import no.met.wdb.DatabaseConnectionSpecification;
import no.met.wdb.ReadQuery;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import ucar.unidata.io.RandomAccessFile;


class WdbConfiguration {
	
	private DatabaseConnectionSpecification databaseConnectionSpecification;
	String wciUser;
	private ReadQuery readQuery = null;
	

	public WdbConfiguration(File configFile) throws FileNotFoundException, IOException, JDOMException {
		this(new FileInputStream(configFile));
	}
	
	WdbConfiguration(RandomAccessFile raf) throws IOException, JDOMException {
		this(new UcarRandomAccessFileInputStream(raf, true));
	}
	
	public WdbConfiguration(InputStream config) throws IOException, JDOMException {

		SAXBuilder builder = new SAXBuilder(false);
		Document document = builder.build(config);
		
		Element wdb_query = document.getRootElement();
		databaseConnectionSpecification = parseConnection(wdb_query);
		
		Element wci = wdb_query.getChild("wci");
		wciUser = parseWciUser(wci);
		if ( wciUser == null )
			wciUser = databaseConnectionSpecification.getUser();
		readQuery = parseReadQuery(wci);
	}

	public DatabaseConnectionSpecification getDatabaseConnectionSpecification() {
		return databaseConnectionSpecification;
	}

	public String getWciUser() {
		return wciUser;
	}
	
	public ReadQuery getReadQuery() {
		return readQuery;
	}
	
	private int parsePort(String from) {
		if ( from == null || from.isEmpty() )
			return DatabaseConnectionSpecification.PORT_NOT_SPECIFIED;
		return Integer.parseInt(from);
	}
	
	private DatabaseConnectionSpecification parseConnection(Element wdb_query) {
		
		Element connection = wdb_query.getChild("connection");
		
		String database = connection.getChildTextNormalize("database");
		String host = connection.getChildTextNormalize("host");
		int port = parsePort(connection.getChildTextNormalize("port"));
		String user = connection.getChildTextNormalize("user");
		
		return new DatabaseConnectionSpecification(database, host, port, user);
	}
	
	private String parseWciUser(Element wci) {
		Element begin = wci.getChild("begin");
		if ( begin == null )
			return null;
		return begin.getChildTextNormalize("user");
	}
	
	private List<String> parseElementList(Element parent, String name) {
		
		List<String> ret = null;
		
		List<Element> children = parent.getChildren(name);
		if ( ! children.isEmpty() ) {
			ret = new Vector<String>(children.size());
			for (Element e : children ) 
				ret.add(e.getTextNormalize());
		}
		return ret;
	}

	private List<Integer> parseElementListAsInt(Element parent, String name) {
		
		List<Integer> ret = null;
		
		List<Element> children = parent.getChildren(name);
		if ( ! children.isEmpty() ) {
			ret = new Vector<Integer>(children.size());
			for (Element e : children ) 
				ret.add(Integer.parseInt(e.getTextTrim()));
		}
		return ret;
	}

	
	private ReadQuery parseReadQuery(Element wci) {
		
		Element read = wci.getChild("read");
		
		List<String> dataProvider = parseElementList(read, "dataprovider");
		String location = read.getChildTextNormalize("location");
		String referenceTime = read.getChildTextNormalize("referencetime"); 
		String validTime = read.getChildTextNormalize("validtime");
		List<String> parameter = parseElementList(read, "valueparameter");
		String level = read.getChildTextNormalize("levelparameter");
		List<Integer> dataVersion = parseElementListAsInt(read, "dataversion");
		
		return new ReadQuery(dataProvider, location, referenceTime, validTime, parameter, level, dataVersion);
	}
}
