package no.met.wdb.netcdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import no.met.wdb.store.NameTranslator;

import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


class GlobalWdbConfiguration implements NameTranslator {

	private HashMap<String, String> wdb2cf = new HashMap<String, String>();
	private HashMap<String, String> cf2wdb = new HashMap<String, String>();
	private HashMap<String, Vector<ucar.nc2.Attribute>> attributes = new HashMap<String, Vector<ucar.nc2.Attribute>>();
	private List<ucar.nc2.Attribute> globalAttributes;
	
	GlobalWdbConfiguration() {
		globalAttributes = new Vector<ucar.nc2.Attribute>();
	}
	
	public GlobalWdbConfiguration(String configFile) throws FileNotFoundException, IOException, JDOMException {
		this(new File(configFile));
	}
	
	public GlobalWdbConfiguration(File configFile) throws FileNotFoundException, IOException, JDOMException {
		this(new FileInputStream(configFile));
	}

	@SuppressWarnings("unchecked")
	public GlobalWdbConfiguration(InputStream configStream) throws IOException, JDOMException {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(configStream);
		
		Element wdb_netcdf_config = document.getRootElement();
		
		Element global_attributes = wdb_netcdf_config.getChild("global_attributes");
		globalAttributes = parseAttributes(global_attributes);
		
		try {
			Element wdb_parameters = wdb_netcdf_config.getChild("wdb_parameters");
			if ( wdb_parameters != null ) {
				for ( Element e : (List<Element>) wdb_parameters.getChildren("level_parameter") )
					addParameterConfiguration(e);
				for ( Element e : (List<Element>) wdb_parameters.getChildren("value_parameter") )
					addParameterConfiguration(e);
			}
			
			Element units = wdb_netcdf_config.getChild("units");
			if ( units != null )
				for ( Element e : (List<Element>) units.getChildren("translation") )
					addTranslation(e);
		}
		catch ( Exception e ) {
			throw new IOException(e);
		}
	}

	private void addTranslation(Element e) throws DataConversionException {
		String wdbName = e.getAttributeValue("wdbname");
		if ( wdbName == null )
			return; // should not happen with well-formed documents

		String cfName = e.getAttributeValue("cfname");
		if ( cfName != null ) {
			wdb2cf.put(wdbName, cfName);
			cf2wdb.put(cfName, wdbName);
		}
	}
	
	private void addParameterConfiguration(Element e) throws DataConversionException {

		addTranslation(e);

		String wdbName = e.getAttributeValue("wdbname");
		if ( wdbName != null )
			attributes.put(wdbName, parseAttributes(e));
	}
	
	private Vector<ucar.nc2.Attribute> parseAttributes(Element parent) throws DataConversionException {

		Vector<ucar.nc2.Attribute> ret = new Vector<ucar.nc2.Attribute>();
		
		if ( parent != null ) {
			@SuppressWarnings("unchecked")
			List<Element> attributes = (List<Element>) parent.getChildren("attribute");
			for ( Element e : attributes ) {

				String name = e.getAttribute("name").getValue();
				
				org.jdom.Attribute typeAttribute = e.getAttribute("type");
				String type = typeAttribute == null ? "String" : typeAttribute.getValue();
				
				org.jdom.Attribute value = e.getAttribute("value");
				if ( type.equals("String") )
					ret.add(new ucar.nc2.Attribute(name, value.getValue()));
				else if ( type.equals("double") )
					ret.add(new ucar.nc2.Attribute(name, value.getDoubleValue()));
				else if ( type.equals("float") )
					ret.add(new ucar.nc2.Attribute(name, value.getFloatValue()));
				else if ( type.equals("int") )
					ret.add(new ucar.nc2.Attribute(name, value.getIntValue()));
				else if ( type.equals("short") )
					ret.add(new ucar.nc2.Attribute(name, (short) value.getIntValue()));
				else if ( type.equals("char") ) {
					String val = value.getValue();
					if ( val.length() > 1 )
						throw new RuntimeException("Invalid format for (single) char attribute: <" + val + ">");
					ret.add(new ucar.nc2.Attribute(name, val));
				}
				
				ret.add(new ucar.nc2.Attribute(name, value.getValue()));
			}
		}
		return ret;
	}
	
	
	/**
	 * Translate a wdb parameter name (value- or level-) into a a cf standard 
	 * name. If no explicit translations are given in configuration - make a 
	 * guess.
	 */
	@Override
	public String translate(String wdbName) {
		String specialTranslation = wdb2cf.get(wdbName);
		if ( specialTranslation == null )
			return defaultCfName(wdbName);
		return specialTranslation;
	}
	
	private static String defaultCfName(String wdbName) {
		return wdbName.replaceAll("[ ()]", "_");
	}


	private void setAttribute(Vector<ucar.nc2.Attribute> out, String name, String value)
	{
		for ( ucar.nc2.Attribute a : out )
			if ( a.getName().equals(name))
				return;
		out.add(new ucar.nc2.Attribute(name, value));
	}

	
	/**
	 * Get all attributes that config says the given parameter should
	 * have. The returned list is not meant to be exhaustive - other
	 * attributes may be added by other means.
	 */
	@SuppressWarnings("unchecked")
	public List<ucar.nc2.Attribute> getAttributes(String wdbParameter, String wdbUnit) {
		
		Vector<ucar.nc2.Attribute> ret;
		Vector<ucar.nc2.Attribute> config = attributes.get(wdbParameter);
		if ( config != null )
			ret = (Vector<ucar.nc2.Attribute>) config.clone();
		else
			ret = new Vector<ucar.nc2.Attribute>();

		setAttribute(ret, "units", translate(wdbUnit));
		setAttribute(ret, "standard_name", translate(wdbParameter));
		setAttribute(ret, "long_name", wdbParameter);
		//setAttribute(ret, "_FillValue", Float.NaN);

		return ret;
	}

	/**
	 * Get all globla attributes mentioned in config. The returned list is not
	 * meant to be exhaustive - other attributes may be added by other means.
	 */
	public List<ucar.nc2.Attribute> getGlobalAttributes() {
		return globalAttributes;
	}
}
