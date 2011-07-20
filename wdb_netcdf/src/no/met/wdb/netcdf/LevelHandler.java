package no.met.wdb.netcdf;

import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import no.met.wdb.Level;
import no.met.wdb.store.WdbIndex;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

class LevelHandler implements DataHandler {

	private WdbIndex index;
	private GlobalWdbConfiguration config;
	
	public LevelHandler(WdbIndex index, GlobalWdbConfiguration config) {
		this.index = index;
		this.config = config;
	}
	
	
	@Override
	public void addToNetcdfFile(NetcdfFile out) {
		
		HashSet<String> addedLevels = new HashSet<String>();

		for ( String parameter : index.allParameters() ) {
			
			Level lvl = index.getLevelForParameter(parameter);

			if ( ! addedLevels.contains(lvl.getName()) ) {

				Vector<Float> levels = index.levelsForParameter(parameter);
				if ( levels.size() > 1 ) {
			
					String dimension = config.cfName(lvl.getName());

					out.addDimension(null, new Dimension(dimension, levels.size()));

					Variable var = new Variable(out, null, null, dimension, DataType.FLOAT, dimension);
					
					for ( Attribute attr : config.getAttributes(dimension, lvl.getUnit()) ) 
						var.addAttribute(attr);
					var.addAttribute(new Attribute("axis", "Z"));

					out.addVariable(null, var);

					addedLevels.add(lvl.getName());
//				}
//			}
//		}
				}
			}
		}
	}

	@Override
	public Array getData(Variable variable) {
		
		TreeSet<Float> levels = index.getLevelValues(config.wdbName(variable.getName()));

		int[] shape = new int[1];
		shape[0] = levels.size();
		
		Array ret = Array.factory(DataType.FLOAT, shape);

		int i = 0;
		for ( Float f : levels )
			ret.setFloat(i ++, f);

		return ret;
		
	}

	@Override
	public boolean canHandle(Variable variable) {

		return index.hasLevel(config.wdbName(variable.getName()));
	}

	@Override
	public String getCoordinatesAttributes(String cfName) {
		return "";
	}

}
