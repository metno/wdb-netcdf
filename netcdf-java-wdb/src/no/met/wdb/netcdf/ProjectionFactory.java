package no.met.wdb.netcdf;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.projection.LatLonProjection;
import ucar.unidata.geoloc.projection.Stereographic;
import ucar.unidata.geoloc.projection.UtmProjection;


class ProjectionFactory {

	static private Pattern p = Pattern.compile("\\+([0-9a-z_]+)=?(.*)");
	
	static public ProjectionImpl getProjection(String projDefinition) {

		HashMap<String, String> definition = new HashMap<String, String>();		
		for ( String s : projDefinition.split("\\s+") ) {
			Matcher m = p.matcher(s);
			
			if ( m.matches() )
				definition.put(m.group(1), m.group(2));
		}

		String projectionType = definition.get("proj");
		if ( projectionType == null )
			throw new IllegalArgumentException("Unable to parse proj string: " + projDefinition);
		else if ( projectionType.equals("stere") )
			return getStereographicProjection(definition);
		else if ( projectionType.equals("utm") )
			return getUtmProjection(definition);
		else if ( projectionType.equals("ob_tran") )
			return getObliqueProjection(definition);
		else if ( projectionType.equals("longlat") )
			return getLongLatProjection(definition);
		else throw new IllegalArgumentException("Unhandled projection type: " + projectionType);
	}

	static private double get(String what, Map<String, String> definition) {
		String ret = definition.get(what);
		if ( ret == null )
			throw new IllegalArgumentException(what);
		return Double.parseDouble(ret);
	}

	static private double get(String what, Map<String, String> definition, double defaultValue) {
		String ret = definition.get(what);
		if ( ret == null )
			return defaultValue;
		return Double.parseDouble(ret);
	}
	
	
	static private ProjectionImpl getStereographicProjection(Map<String, String> definition) {

		//+proj=stere +lat_0=90 +lon_0=58 +lat_ts=60 +a=6371000 +units=m +no_defs
	
		double lat_0 = get("lat_0", definition);
		double lat_ts = get("lat_ts", definition);
		
		double lon_0 = get("lon_0", definition);

		System.out.println("lat_0=" + lat_0 + " lat_ts=" + lat_ts + " lon_0=" + lon_0);
		
		
		Stereographic ret = new Stereographic(lat_ts, lat_0, lon_0, true);
		System.out.println(ret.getProjectionParameters());
		
		return ret;
	}
	
	static private ProjectionImpl getUtmProjection(Map<String, String> definition) {
		
		// atm we do not support +lon_0
		
		int zone = (int) get("zone", definition);
		
		boolean north = definition.get("south") == null;
		
		String ellps = definition.get("ellps");
		if ( ellps == null )
			ellps = "WGS84"; // this is default
		if ( ellps.equals("WGS84") ) {
			double axis = 6378137.0;
			double inverseFlattening = 298.257223563;
			return new UtmProjection(axis, inverseFlattening, zone, north);
		}
		// TODO: add others as needed

		throw new IllegalArgumentException("Unsupported ellipsis: " + ellps);
	}

	static private ProjectionImpl getObliqueProjection(Map<String, String> definition) {
		
		String realProjection = definition.get("o_proj");
		if ( realProjection == null )
			throw new IllegalArgumentException("Missing projection parameter");
		else if ( realProjection != "longlat" )
			throw new IllegalArgumentException("oblique transformations currently only supports longlat grids");

		// o_lat o_lon = pole position
		
		double northPoleLat = get("o_lat_p", definition, 0);
		double northPoleLon = get("o_lon_b", definition, 0);
		double angle = get("lon_0", definition, 0);
		
		//new RotatedLatLon(southPoleLat, soutPoleLon, southPoleAngle);
		
		throw new IllegalArgumentException("Unhandled projection type: ob_tran");
	}

	static private ProjectionImpl getLongLatProjection(Map<String, String> definition) {
		return new LatLonProjection("longlat");
	}
}
