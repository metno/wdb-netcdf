package no.met.wdb.netcdf;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import no.met.wdb.GridData;
import no.met.wdb.PlaceRegularGrid;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.util.Parameter;
import ucar.units.Unit;
import ucar.units.UnitException;
import ucar.units.UnitFormat;
import ucar.units.UnitFormatManager;


class GridHandler implements DataHandler {

	private String oneDimensionX = "";
	private String oneDimensionY = "";
	private String twoDimensionX = "";
	private String twoDimensionY = "";
	
	private PlaceRegularGrid grid;
	private ProjectionSpecification projection;
	private String coordinates = "";
	
	public GridHandler(Iterable<GridData> gridData) {
		
		Iterator<GridData> it = gridData.iterator();
		if ( it.hasNext() ) {
			GridData d = it.next();
			grid = d.getGrid();
			projection = new ProjectionSpecification(grid.getProjDefinition());
		}
		else
			throw new RuntimeException("No data");
	}

	
	public String getXDimension() {
		return oneDimensionX;
	}

	public String getYDimension() {
		return oneDimensionY;
	}
	
	private String createDerivedVariable(NetcdfFile out, String name, String dims, Attribute... attributes) {
		
		Variable v = new Variable(out, null, null, name, DataType.FLOAT, dims);
		for ( Attribute attr : attributes ) {
			v.addAttribute(attr);
		}
				
		out.addVariable(null, v);
		
		return v.getName();
	}
	
	private String createDimensionVariable(NetcdfFile out, String name, int size, Attribute... attributes) {
		
		out.addDimension(null, new Dimension(name, size));
	
		Variable v = new Variable(out, null, null, name, DataType.FLOAT, name);
		for ( Attribute attr : attributes ) {
			v.addAttribute(attr);
		}
				
		out.addVariable(null, v);
		
		return v.getName();
	}
	
	private String getProjectionVariableName() {

		return projection.getProjection().getName();
	}
	
	private void addProjectionVariable(NetcdfFile out) {

		Variable var = new Variable(out, null, null, getProjectionVariableName(), DataType.CHAR, null);
		
		for ( Parameter p : projection.getProjection().getProjectionParameters() )
			var.addAttribute(new Attribute(p));
		var.addAttribute(new Attribute("proj4", projection.getProjDefinition()));
		
		out.addVariable(null, var);
	}
	
	@Override
	public void addToNetcdfFile(NetcdfFile out) {
		
		// TODO: correct names for rotated lat/lon grids
		// TODO: add projection variable
		
		addProjectionVariable(out);
		
		if ( projection.getProjection().isLatLon() ) {
			oneDimensionX = createDimensionVariable(out, "longitude", grid.getNumberX(),
					new Attribute("long_name", "longitude"),
					new Attribute("standard_name", "longitude"),
					new Attribute("units", "degree_east"),
					new Attribute("axis", "x"));
			oneDimensionY = createDimensionVariable(out, "latitude", grid.getNumberY(),
					new Attribute("long_name", "latitude"),
					new Attribute("standard_name", "latitude"),
					new Attribute("units", "degree_north"),
					new Attribute("axis", "y"));
		}
		else {
			oneDimensionX = createDimensionVariable(out, "xc", grid.getNumberX(), 
					new Attribute("standard_name", "projection_x_coordinate"),
					new Attribute("units", "m"),
					new Attribute("axis", "x"));
			oneDimensionY = createDimensionVariable(out, "yc", grid.getNumberY(), 
					new Attribute("standard_name", "projection_y_coordinate"),
					new Attribute("units", "m"),
					new Attribute("axis", "y"));
			twoDimensionX = createDerivedVariable(out, "longitude", "yc xc",
					new Attribute("long_name", "longitude"),
					new Attribute("standard_name", "longitude"),
					new Attribute("units", "degree_east"));
			twoDimensionY = createDerivedVariable(out, "latitude", "yc xc",
					new Attribute("long_name", "latitude"),
					new Attribute("standard_name", "latitude"),
					new Attribute("units", "degree_north"));
			coordinates = "longitude latitude";
		}

	}

	@Override
	public Array getData(Variable variable) {
		
		String var = variable.getName();
		if ( var.equals(oneDimensionX) ) {
			Array ret = Array.factory(DataType.FLOAT, variable.getShape());
			float start = grid.getStartX();
			float increment = grid.getIncrementX();
			int stop = grid.getNumberX();
			for ( int i = 0; i < stop; i ++ )
				ret.setFloat(i, start + (i * increment));
			return ret;
		}
		else if ( var.equals(oneDimensionY) ) {
			Array ret = Array.factory(DataType.FLOAT, variable.getShape());
			float start = grid.getStartY();
			float increment = grid.getIncrementY();
			int stop = grid.getNumberY();
			for ( int i = 0; i < stop; i ++ )
				ret.setFloat(i, start + (i * increment));
			return ret;
		}
		else if ( var.equals(twoDimensionX) ) {
			convertLatLon();
			return Array.factory(DataType.FLOAT, variable.getShape(), points[1]);
		}
		else if ( var.equals(twoDimensionY) ) {
			convertLatLon();
			return Array.factory(DataType.FLOAT, variable.getShape(), points[0]);
		}
		else if ( var.equals(getProjectionVariableName()) ) {
			return Array.factory(variable.getDataType(), variable.getShape());
		}
		throw new RuntimeException("Internal error: No such grid handler variable: " + variable.getName());
	}

	@Override
	public boolean canHandle(Variable variable) {
		String name = variable.getName();
		
		return oneDimensionX.equals(name)
				|| oneDimensionY.equals(name)
				|| twoDimensionX.equals(name)
				|| twoDimensionY.equals(name)
				|| getProjectionVariableName().equals(name);
	}

	@Override
	public String getCoordinatesAttributes(String cfName) {
		return coordinates;
	}

	private float[][] points = null;
	
	private void convertLatLon() {

		if ( points == null ) {
			points = new float[2][grid.getNumberX() * grid.getNumberY()];
			
			float startX = grid.getStartX();
			float incrementX = grid.getIncrementX();
			float startY = grid.getStartY();
			float incrementY = grid.getIncrementY();
			
			
			int pos = 0;
			for ( int y = 0; y < grid.getNumberY(); y ++ ) {
				for ( int x = 0; x < grid.getNumberX(); x ++ ) {
					points[0][pos] = (startX + (incrementX * x));// * 1000;
					points[1][pos] = (startY + (incrementY * y));// * 1000;
					pos ++;
				}
			}

			String unit = projection.getDefinition().get("units");
			if ( unit != null ) {
				try {
					UnitFormat format = UnitFormatManager.instance();
					Unit to = format.parse(unit);
					Unit from = format.parse("km");
	
					to.convertTo(points[0], from, points[0]);
					to.convertTo(points[1], from, points[1]);
				}
				catch ( UnitException e ) {
					System.out.println(e.getLocalizedMessage());
				}
			}
			
			points = projection.getProjection().projToLatLon(points);
		}
	}
	
}
