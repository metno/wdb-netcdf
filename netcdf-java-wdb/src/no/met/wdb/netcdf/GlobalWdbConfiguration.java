package no.met.wdb.netcdf;

import java.util.HashMap;
import java.util.Vector;

import ucar.nc2.Attribute;

public class GlobalWdbConfiguration {

	private HashMap<String, String> wdb2cf = new HashMap<String, String>();
	private HashMap<String, String> cf2wdb = new HashMap<String, String>();
	private HashMap<String, Vector<Attribute>> attributes = new HashMap<String, Vector<Attribute>>();
	private Vector<Attribute> globalAttributes = new Vector<Attribute>(); 
	

	public GlobalWdbConfiguration() {
	}

	public GlobalWdbConfiguration(String configFile) {
		System.err.println("TODO: Parse confi file");
	}
	
	/**
	 * Translate a wdb parameter name (value- or level-) into a cf standard
	 * name. If no explicit translations are given in configuration - make
	 * a guess.
	 */
	public String cfName(String wdbName) {
		String specialTranslation = wdb2cf.get(wdbName);
		if ( specialTranslation == null )
			return wdbName.replace(' ', '_');
		return specialTranslation;
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

	private void setAttribute(Vector<Attribute> out, String name, String value)
	{
		for ( Attribute a : out )
			if ( a.getName().equals(name))
				return;
		out.add(new Attribute(name, value));
	}

	
	/**
	 * Get all attributes that config says the given parameter should
	 * have. The returned list is not meant to be exhaustive - other
	 * attributes may be added by other means.
	 */
	public Iterable<Attribute> getAttributes(String wdbParameter, String defaultUnit) {
		
		Vector<Attribute> ret;
		Vector<Attribute> config = attributes.get(wdbParameter);
		if ( config != null )
			ret = (Vector<Attribute>) config.clone();
		else
			ret = new Vector<Attribute>();

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
	public Iterable<Attribute> getGlobalAttributes() {
		return globalAttributes;
	}
}
