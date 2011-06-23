package no.met.wdb.netcdf;

import no.met.wdb.GridData;
import no.met.wdb.PlaceRegularGrid;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

class GridHandler implements DataHandler {

	private PlaceRegularGrid grid = null;

	public static GridHandler get(Iterable<GridData> gridData) {
		return new GridHandler(gridData);
	}
	
	private GridHandler(Iterable<GridData> gridData) {
	
		for ( GridData d : gridData ) {
			PlaceRegularGrid next = d.getGrid();
			if ( grid == null )
				grid = next;
			else if ( ! grid.getPlaceName().equals(next.getPlaceName()) )
				throw new RuntimeException("Many grid types is not allowed");
		}
	}
	
	@Override
	public void addToNetcdfFile(NetcdfFile out) {
		String xDimension = "x";
		out.addDimension(null, new Dimension(xDimension, grid.getNumberX()));
		Variable xvar = new Variable(out, null, null, xDimension, DataType.FLOAT, xDimension);
		xvar.addAttribute(new Attribute("long_name", "x stuff"));
		xvar.addAttribute(new Attribute("standard_name", "grid_longitude"));
		xvar.addAttribute(new Attribute("units", "whatever"));
		xvar.addAttribute(new Attribute("axis", "X"));
		out.addVariable(null, xvar);
		
		String yDimension = "y";
		out.addDimension(null, new Dimension(yDimension, grid.getNumberY()));
		Variable yvar = new Variable(out, null, null, yDimension, DataType.FLOAT, yDimension);
		yvar.addAttribute(new Attribute("long_name", "y stuff"));
		yvar.addAttribute(new Attribute("standard_name", "grid_latitude"));
		yvar.addAttribute(new Attribute("units", "whatever"));
		yvar.addAttribute(new Attribute("axis", "Y"));
		out.addVariable(null, yvar);
	}

	@Override
	public Array getData(Variable variable) {
		int[] shape = new int[1];
		if ( variable.getName().equals("x") )
			shape[0] = grid.getNumberX();
		else
			shape[0] = grid.getNumberY();

		Array ret = Array.factory(DataType.FLOAT, shape);

		for ( int i = 0; i < shape[0]; ++ i )
			ret.setFloat(i, i);
		
		return ret;
	}

	@Override
	public boolean canHandle(Variable variable) {
		return variable.getName().equals("x") || variable.getName().equals("y");
	}

	@Override
	public String getCoordinatesAttributes(String cfName) {
		return "";
	}

}
