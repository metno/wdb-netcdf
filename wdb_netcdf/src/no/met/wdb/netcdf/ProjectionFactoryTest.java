package no.met.wdb.netcdf;

import static org.junit.Assert.*;

import org.junit.Test;

import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.ProjectionPoint;
import ucar.unidata.util.Parameter;

// To test:
//
//	+proj=longlat +a=6367470.0 +towgs84=0,0,0 +no_defs
//	+proj=utm +zone=33 +ellps=WGS84 +datum=WGS84 +units=m +no_defs 
//	+proj=utm +zone=32 +ellps=WGS84 +datum=WGS84 +units=m +no_defs 
//	+proj=utm +lon_0=15 +datum=WGS84 +units=m +no_defs
//	+proj=utm +zone=35 +ellps=WGS84 +datum=WGS84 +units=m +no_defs 
//	+proj=stere +lat_0=90 +lon_0=58 +lat_ts=60 +a=6371000 +units=m +no_defs
//	+proj=stere +lat_0=90 +lon_0=24 +lat_ts=60 +a=6371000 +units=m +no_defs
//	+proj=ob_tran +o_proj=longlat +lon_0=-24 +o_lat_p=23.5 +a=6367470.0 +no_defs
//	+proj=ob_tran +o_proj=longlat +lon_0=0 +o_lat_p=25 +a=6367470.0 +no_defs
//	+proj=ob_tran +o_proj=longlat +lon_0=-40 +o_lat_p=22 +a=6367470.0 +no_defs

class ProjectionFactoryTest {

	//private static final double e = 0.00001;
	private static final double e = 0.005;

	@Test
	public void testLongLatProjection() {
		ProjectionImpl impl = ProjectionSpecification.getProjection("+proj=longlat +a=6367470.0 +towgs84=0,0,0 +no_defs");
		LatLonPoint p = impl.projToLatLon(10, 20);
		
		assertEquals(10, p.getLongitude(), e);
		assertEquals(20, p.getLatitude(), e);
	}

	@Test
	public void testUtmProjectionZone33() {
		ProjectionImpl impl = ProjectionSpecification.getProjection("+proj=utm +zone=33 +ellps=WGS84 +datum=WGS84 +units=m +no_defs");
		LatLonPoint p = impl.projToLatLon(10, 20);
		
		System.out.println(p.toString());

		assertEquals(10.511346, p.getLongitude(), e); 
		assertEquals(0.00018, p.getLatitude(), e);
	}

	@Test
	public void testUtmProjectionZone32() {
		ProjectionImpl impl = ProjectionSpecification.getProjection("+proj=utm +zone=32 +ellps=WGS84 +datum=WGS84 +units=m +no_defs");
		LatLonPoint p = impl.projToLatLon(10, 20);
		
		System.out.println(p.toString());
		
		assertEquals(4.511346, p.getLongitude(), e);
		assertEquals(0.00018, p.getLatitude(), e);
	}

@Test
public void testStereoGraphicProjection() {
	
	ProjectionImpl impl = ProjectionSpecification.getProjection("+proj=stere +lat_0=90 +lon_0=58 +lat_ts=60 +a=6371000 +units=m +no_defs");
	LatLonPoint p = impl.projToLatLon(-2452.000, -1952.000);

	assertEquals(6.52276459393507, p.getLongitude(), e);
	assertEquals(60.4627127507448, p.getLatitude(), e);
}

@Test
public void testStereoGraphicProjection2() {
	
	ProjectionImpl impl = ProjectionSpecification.getProjection("+proj=stere +lat_0=90 +lon_0=58 +lat_ts=60 +a=6371000 +units=m +no_defs");
	LatLonPoint p = impl.projToLatLon(-1704.000, -1504.000);

	assertEquals(9.43256110381849, p.getLongitude(), e);
	assertEquals(68.3538736322542, p.getLatitude(), e);
}

@Test
public void testStereoGraphicProjection3() {
	
	// Units is not supported!
	
	ProjectionImpl impl = ProjectionSpecification.getProjection("+proj=stere +lat_0=90 +lon_0=58 +lat_ts=60 +a=6371000 +units=m +no_defs");
	ProjectionPoint p = impl.latLonToProj(68.3538736322542, 9.43256110381849);

	
	for ( Parameter pm : impl.getProjectionParameters() )
		System.out.println("\t" + pm);
	
	assertEquals(-1704.000, p.getX(), e);
	assertEquals(-1504.000, p.getY(), e);
}


}
