package no.met.wdb.netcdf;

import java.util.HashMap;

//import ucar.nc2.NetcdfFile;

class Wdb2NetcdfNameTranslator {

	private HashMap<String, String> wdb2netcdf = new HashMap<String, String>();
	private HashMap<String, String> netcdf2wdb = new HashMap<String, String>();
	
	private void addTranslation(String wdbName, String netcdfName) {
		wdb2netcdf.put(wdbName, netcdfName);
		netcdf2wdb.put(netcdfName, wdbName);
	}
	
	public Wdb2NetcdfNameTranslator() {
		//addTranslation("depth", "depth_below_sea");
	}
	
	public String fromNetcdf(String netcdfName) {

		String specialTranslation = netcdf2wdb.get(netcdfName);
		if ( specialTranslation == null )
			return netcdfName.replace('_', ' ');
		return specialTranslation;
	}

	
	public String fromWdb(String wdbName) {

		String specialTranslation = wdb2netcdf.get(wdbName);
		if ( specialTranslation == null )
			return wdbName.replace(' ', '_');
		return specialTranslation;
	}
}
