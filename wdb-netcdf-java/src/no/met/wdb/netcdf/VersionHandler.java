package no.met.wdb.netcdf;

import java.util.Vector;

import no.met.wdb.store.WdbIndex;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

class VersionHandler implements DataHandler {

	private WdbIndex index;

	private int[] versions = null;
	
	public static String cfName = "ensemble_member";

	
	
	public VersionHandler(WdbIndex index) {
		this.index = index;
	}
	
	@Override
	public void addToNetcdfFile(NetcdfFile out) {
		
		for ( String parameter : index.allParameters() ) {

			Vector<Integer> versions = index.versionsForParameter(parameter);
			if ( versions.size() > 1 )
			{
				Dimension dim = new Dimension(cfName, versions.size());
				out.addDimension(null, dim);

				Variable var = new Variable(out, null, null, cfName, DataType.INT, cfName);
	
				var.addAttribute(new Attribute("long_name", "ensemble run number"));
				var.addAttribute(new Attribute("standard_name", "realization"));
				//var.addAttribute(new Attribute("axis", "Ensemble"));
	
				out.addVariable(null, var);
				
				this.versions = new int[versions.size()];
				int idx = 0;
				for ( Integer i : versions )
					this.versions[idx ++] = i;

				return;
			}
		}
	}

	@Override
	public Array getData(Variable variable) {
		int[] shape = new int[1];
		shape[0] = versions.length;
		Array ret = Array.factory(DataType.INT, shape);
		
		for ( int i = 0; i < versions.length; i ++ )
			ret.setInt(i, versions[i]);
		
		return ret;
	}

	@Override
	public boolean canHandle(Variable variable) {
		return variable.getName().equals(cfName);
	}

	@Override
	public String getCoordinatesAttributes(String cfName) {
		return "";
	}
}
