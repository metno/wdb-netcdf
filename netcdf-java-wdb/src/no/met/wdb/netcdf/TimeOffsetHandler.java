package no.met.wdb.netcdf;

import java.util.Date;
import java.util.TreeSet;

import no.met.wdb.store.WdbIndex;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

class TimeOffsetHandler implements DataHandler {

	private WdbIndex index;
	private Array data = null;
	
	public static String cfName = "offsetTime";

	
	public TimeOffsetHandler(WdbIndex index) {
		this.index = index;
	}
	
	
	@Override
	public void addToNetcdfFile(NetcdfFile out) {
		TreeSet<Date> referenceTimes = index.getAllReferenceTimes();
		TreeSet<Long> validTimes = index.getAllValidtimes();

		if ( referenceTimes.size() > 1 && validTimes.size() > 1 ) {
			out.addDimension(null, new Dimension(cfName, validTimes.size()));

			Variable var = new Variable(out, null, null, cfName, DataType.DOUBLE, cfName);
			
			var.addAttribute(new Attribute("long_name", "offset since referenceTime"));
			var.addAttribute(new Attribute("units", "seconds"));
			
			out.addVariable(null, var);
		}
	}

	@Override
	public Array getData(Variable variable) {
		if ( data == null ) {
			TreeSet<Long> validTimes = index.getAllValidtimes();
			
			int[] shape = new int[1];
			shape[0] = validTimes.size();
			data = Array.factory(DataType.DOUBLE, shape);
			
			int i = 0;
			for ( Long l : validTimes ) {
				data.setDouble(i ++, l.doubleValue());
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
