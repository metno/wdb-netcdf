package no.met.wdb.netcdf;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.projection.LatLonProjection;
import ucar.unidata.geoloc.projection.RotatedLatLon;
import ucar.unidata.geoloc.projection.Stereographic;
import ucar.unidata.geoloc.projection.UtmProjection;


class ProjectionSpecification {

	static private Pattern p = Pattern.compile("\\+([0-9a-z_]+)=?(.*)");
	
	
	private ProjectionImpl projection;
	private String projDefinition;
	private Map<String, String> definition = new HashMap<String, String>();
	private String suggestedXDimensionName = "";
	private String suggestedYDimensionName = "";
	private String unitsX;
	private String unitsY;

	public ProjectionSpecification(String projDefinition) {
		
		this.projDefinition = projDefinition;
		
		for ( String s : projDefinition.split("\\s+") ) {
			Matcher m = p.matcher(s);
			
			if ( m.matches() )
				definition.put(m.group(1), m.group(2));
		}

		String projectionType = definition.get("proj");
		if ( projectionType == null )
			throw new IllegalArgumentException("Unable to parse proj string: " + projDefinition);
		else if ( projectionType.equals("stere") )
			projection = getStereographicProjection(definition);
		else if ( projectionType.equals("utm") )
			projection = getUtmProjection(definition);
		else if ( projectionType.equals("ob_tran") )
			projection = getObliqueProjection(definition);
		else if ( projectionType.equals("longlat") )
			projection = getLongLatProjection(definition);
		else throw new IllegalArgumentException("Unhandled projection type: " + projectionType);
	}
	
	static ProjectionImpl getProjection(String projDefinition) {
		return new ProjectionSpecification(projDefinition).getProjection();
	}
	

	public ProjectionImpl getProjection() {
		return projection;
	}

	
	public String getProjDefinition() {
		return projDefinition;
	}


	public Map<String, String> getDefinition() {
		return definition;
	}

	public String getSuggestedXDimensionName() {
		return suggestedXDimensionName;
	}

	public String getSuggestedYDimensionName() {
		return suggestedYDimensionName;
	}

	public String getUnitsX() {
		return unitsX;
	}

	public String getUnitsY() {
		return unitsY;
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
	
	
	private ProjectionImpl getStereographicProjection(Map<String, String> definition) {

		//+proj=stere +lat_0=90 +lon_0=58 +lat_ts=60 +a=6371000 +units=m +no_defs
	
		double lat_0 = get("lat_0", definition);
		double lat_ts = get("lat_ts", definition);
		
		double lon_0 = get("lon_0", definition);
		
		Stereographic ret = new Stereographic(lat_ts, lat_0, lon_0, true);
		ret.setName("projection_stereographic");

		suggestedXDimensionName = "xc";
		suggestedYDimensionName = "yc";
		String units = definition.get("units");
		if ( units != null ) {
			unitsX = units;
			unitsY = units;
		}
		else { // guess a default
			unitsX = "m";
			unitsY = "m";
		}
		
		
		return ret;
	}
	
	private ProjectionImpl getUtmProjection(Map<String, String> definition) {
		
		// atm we do not support +lon_0
		
		int zone = (int) get("zone", definition);
		
		boolean north = definition.get("south") == null;
		
		String ellps = definition.get("ellps");
		if ( ellps == null )
			ellps = "WGS84"; // this is default
		if ( ellps.equals("WGS84") ) {
			double axis = 6378137.0;
			double inverseFlattening = 298.257223563;
			ProjectionImpl ret =  new UtmProjection(axis, inverseFlattening, zone, north);
			ret.setName("projection_utm" + zone);
			
			suggestedXDimensionName = "xc";
			suggestedYDimensionName = "yc";
			String units = definition.get("units");
			if ( units != null ) {
				unitsX = units;
				unitsY = units;
			}
			else { // guess a default
				unitsX = "m";
				unitsY = "m";
			}
			
			return ret;
		}
		// TODO: add others as needed

		throw new IllegalArgumentException("Unsupported ellipsis: " + ellps);
	}

	private ProjectionImpl getObliqueProjection(Map<String, String> definition) {

		for ( Map.Entry<String,String> entry : definition.entrySet() )
			System.out.println("definition[" + entry.getKey() + "] = " + entry.getValue());
		
		String realProjection = definition.get("o_proj");
		if ( realProjection == null )
			throw new IllegalArgumentException("Missing projection parameter");
		else if ( ! realProjection.equals("longlat") )
			throw new IllegalArgumentException("oblique transformations currently only supports longlat grids");

		// o_lat o_lon = pole position
		
		double latitudeOfSouthernPoleInDegrees = - get("o_lat_p", definition, 0); // 23.5
		double longitudeOfSouthernPoleInDegrees = get("lon_0", definition, 0); // -24
		double something = get("o_lon_b", definition, 0); // 0
		if ( something != 0 )
			throw new IllegalArgumentException("o_lon_b is not supported");
		
		ProjectionImpl ret = new RotatedLatLon(latitudeOfSouthernPoleInDegrees, longitudeOfSouthernPoleInDegrees, 0);
		ret.setName("projection_rotated_latitude_longitude");
		
		suggestedXDimensionName = "rlon";
		suggestedYDimensionName = "rlat";
		unitsX = "degree_east";
		unitsY = "degree_north";
		
		return ret;
	}

	private ProjectionImpl getLongLatProjection(Map<String, String> definition) {
		suggestedXDimensionName = "longitude";
		suggestedYDimensionName = "latitude";
		unitsX = "degree_east";
		unitsY = "degree_north";
	
		return new LatLonProjection("projection_longlat");
	}
}
