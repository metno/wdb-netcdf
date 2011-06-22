package no.met.wdb.netcdf;

import static org.junit.Assert.*;

import org.junit.Test;

import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.ProjectionImpl;

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

public class ProjectionFactoryTest {

	private static final double e = 0.00001;

	@Test
	public void testLongLatProjection() {
		ProjectionImpl impl = ProjectionFactory.getProjection("+proj=longlat +a=6367470.0 +towgs84=0,0,0 +no_defs");
		LatLonPoint p = impl.projToLatLon(10, 20);
		
		assertEquals(10, p.getLongitude(), e);
		assertEquals(20, p.getLatitude(), e);
	}

	@Test
	public void testUtmProjectionZone33() {
		ProjectionImpl impl = ProjectionFactory.getProjection("+proj=utm +zone=33 +ellps=WGS84 +datum=WGS84 +units=m +no_defs");
		LatLonPoint p = impl.projToLatLon(10, 20);
		
		System.out.println(p.toString());

		assertEquals(10.511346, p.getLongitude(), e); 
		assertEquals(0.00018, p.getLatitude(), e);
	}

	@Test
	public void testUtmProjectionZone32() {
		ProjectionImpl impl = ProjectionFactory.getProjection("+proj=utm +zone=32 +ellps=WGS84 +datum=WGS84 +units=m +no_defs");
		LatLonPoint p = impl.projToLatLon(10, 20);
		
		System.out.println(p.toString());
		
		assertEquals(4.511346, p.getLongitude(), e);
		assertEquals(0.00018, p.getLatitude(), e);
	}

@Test
public void testStereoGraphicProjection() {
	
	ProjectionImpl impl = ProjectionFactory.getProjection("+proj=stere +lat_0=90 +lon_0=58 +lat_ts=60 +a=6371000 +units=m +no_defs");
	LatLonPoint p = impl.projToLatLon(0, 50000);

	assertEquals(-122, p.getLongitude(), e);
	assertEquals(89.518058, p.getLatitude(), e);
	
//	assertEquals(-148.565051, p.getLongitude(), e);
//	assertEquals(89.999784, p.getLatitude(), e); // ~ 89.785589
}
}
