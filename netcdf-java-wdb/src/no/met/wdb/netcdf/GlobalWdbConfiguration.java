package no.met.wdb.netcdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class GlobalWdbConfiguration {

	private HashMap<String, String> wdb2cf = new HashMap<String, String>();
	private HashMap<String, String> cf2wdb = new HashMap<String, String>();
	private HashMap<String, Vector<ucar.nc2.Attribute>> attributes = new HashMap<String, Vector<ucar.nc2.Attribute>>();
	private List<ucar.nc2.Attribute> globalAttributes; 
	

	public GlobalWdbConfiguration(String configFile) throws FileNotFoundException, IOException, JDOMException {
		this(new File(configFile));
	}
	
	public GlobalWdbConfiguration(File configFile) throws FileNotFoundException, IOException, JDOMException {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(configFile);
		
		Element wdb_netcdf_config = document.getRootElement();
		
		Element global_attributes = wdb_netcdf_config.getChild("global_attributes");
		globalAttributes = parseAttributes(global_attributes);
		
		Element wdb_parameters = wdb_netcdf_config.getChild("wdb_parameters");
		if ( wdb_parameters != null ) {
			for ( Element e : (List<Element>) wdb_parameters.getChildren("level_parameter") )
				addParameterConfiguration(e);
			for ( Element e : (List<Element>) wdb_parameters.getChildren("value_parameter") )
				addParameterConfiguration(e);
		}
	}
	
	private void addParameterConfiguration(Element e) {
		String wdbName = e.getAttributeValue("wdbname");
		if ( wdbName == null )
			return; // wtf?

		String cfName = e.getAttributeValue("cfname");
		if ( cfName != null ) {
			wdb2cf.put(wdbName, cfName);
			cf2wdb.put(cfName, wdbName);
		}
		
		attributes.put(wdbName, parseAttributes(e));
	}
	
	private Vector<ucar.nc2.Attribute> parseAttributes(Element parent) {

		Vector<ucar.nc2.Attribute> ret = new Vector<ucar.nc2.Attribute>();
		
		if ( parent != null ) {
			List<Element> attributes = parent.getChildren("attribute");
			for ( Element e : attributes ) {
				String name = e.getAttribute("name").getValue();
				String value = e.getAttribute("value").getValue();
				ret.add(new ucar.nc2.Attribute(name, value));
			}
		}
		return ret;
	}
	
	
	/**
	 * Translate a wdb parameter name (value- or level-) into a cf standard
	 * name. If no explicit translations are given in configuration - make
	 * a guess.
	 */
	public String cfName(String wdbName) {
		String specialTranslation = wdb2cf.get(wdbName);
		if ( specialTranslation == null )
			return defaultCfName(wdbName);
		return specialTranslation;
	}
	
	private static String defaultCfName(String wdbName) {
		return wdbName.replace(' ', '_');
	}

	/**
	 * Translate a cf standard name (value- or level-) into a wdb parameter
	 * name. If no explicit translations are given in configuration - make
	 * a guess.
	 */
	public String wdbName(String cfName) {
		String specialTranslation = cf2wdb.get(cfName);
		if ( specialTranslation == null )
			return cfName.replace('_', ' ');
		return specialTranslation;
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
	public List<ucar.nc2.Attribute> getAttributes(String wdbParameter, String defaultUnit) {
		
		Vector<ucar.nc2.Attribute> ret;
		Vector<ucar.nc2.Attribute> config = attributes.get(wdbParameter);
		if ( config != null )
			ret = (Vector<ucar.nc2.Attribute>) config.clone();
		else
			ret = new Vector<ucar.nc2.Attribute>();

		setAttribute(ret, "units", defaultUnit);
		setAttribute(ret, "standard_name", cfName(wdbParameter));
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
