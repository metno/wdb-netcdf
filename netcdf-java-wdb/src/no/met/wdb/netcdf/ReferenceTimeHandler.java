package no.met.wdb.netcdf;

import java.util.Date;
import java.util.TreeSet;

import no.met.wdb.store.WdbIndex;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

class ReferenceTimeHandler implements DataHandler {

	private WdbIndex index;
	private Array data = null;
	
	public static String cfName = "runtime";

	
	public ReferenceTimeHandler(WdbIndex index) {
		this.index = index;
	}
	
	
	@Override
	public void addToNetcdfFile(NetcdfFile out) {
		TreeSet<Date> referenceTimes = index.getAllReferenceTimes();

		Variable var = new Variable(out, null, null, cfName);
		var.setDataType(DataType.DOUBLE);

		if ( referenceTimes.size() > 1 ) {
			Dimension dim = new Dimension(cfName, referenceTimes.size());
			dim.setUnlimited(true);
			out.addDimension(null, dim);
			
			var.setDimensions(cfName);
		}
		else
			var.setDimensions("");

		var.addAttribute(new Attribute("long_name", "Run time for model"));
		var.addAttribute(new Attribute("standard_name", "forecast_reference_time"));
		var.addAttribute(new Attribute("units", "seconds since 1970-01-01 00:00:00 +00:00"));
		//var.addAttribute(new Attribute("_CoordinateAxisType", "RunTime"));

		out.addVariable(null, var);
	}

	@Override
	public Array getData(Variable variable) {
		if ( data == null ) {
			TreeSet<Date> referenceTimes = index.getAllReferenceTimes();
			
			if ( referenceTimes.size() > 1 )
			{
				int[] shape = new int[1];
				shape[0] = referenceTimes.size();
				data = Array.factory(DataType.DOUBLE, shape);
				
				int i = 0;
				for ( Date d : referenceTimes ) {
					data.setDouble(i ++, d.getTime() / 1000);
				}
			}
			else {
				ArrayDouble.D0 a = new ArrayDouble.D0();
				a.set(referenceTimes.first().getTime() / 1000);
				data = a;
			}
		}
		return data;
	}

	@Override
	public boolean canHandle(String wdbName) {
		return wdbName == cfName;
	}

	@Override
	public String getCoordinatesAttributes(String wdbName) {
		return "";
	}
}
