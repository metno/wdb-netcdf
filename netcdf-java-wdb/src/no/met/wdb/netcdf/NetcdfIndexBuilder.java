package no.met.wdb.netcdf;

import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import no.met.wdb.GridData;
import no.met.wdb.store.IndexCreationException;
import no.met.wdb.store.WdbIndex;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

class NetcdfIndexBuilder {

	private WdbIndex index;
	private GlobalWdbConfiguration config;
	private Vector<DataHandler> dataHandlers = new Vector<DataHandler>();
	
	
	private void setupHandlers(Iterable<GridData> gridData) {
		dataHandlers.add(new ReferenceTimeHandler(index));
		dataHandlers.add(new ValidTimeHandler(index));
		dataHandlers.add(new TimeOffsetHandler(index));
		dataHandlers.add(new VersionHandler(index, config));
		dataHandlers.add(new LevelHandler(index, config));
		dataHandlers.add(GridHandler.get(gridData));
	}
	
	public NetcdfIndexBuilder(Iterable<GridData> gridData, GlobalWdbConfiguration config) throws IndexCreationException {

		index = new WdbIndex(gridData);
		this.config = config;

		setupHandlers(gridData);
	}
	
	/**
	 * For testing - allows setting data to be worked on later
	 */
	NetcdfIndexBuilder(GlobalWdbConfiguration config) {
		index = null;
		this.config = config;
	}
	
	/**
	 * For testing, create and throw away a WdbIndex object.
	 * 
	 * @param gridData input to WdbIndex object creation
	 * @param out the object to be populated
	 */
	void populate(Iterable<GridData> gridData, NetcdfFile out) throws IndexCreationException {
		index = new WdbIndex(gridData);
		setupHandlers(gridData);
		populate(out);
	}
	
	
	public void populate(NetcdfFile out) {

		addDimensions(out);
		addParameterVariables(out);
	}

	public Array getMetadata(Variable v2, Section section) throws InvalidRangeException {
		
		String wdbName = config.wdbName(v2.getName());
		
		System.out.println(section.toString());
		
		Array ret = null;
		for ( DataHandler handler : dataHandlers )
			if ( handler.canHandle(wdbName) )
				ret = handler.getData(v2);

		if ( ret != null )
			return ret.section(section.getRanges());
		
		return null;
	}
	
	/**
	 * Does the given variable name refer to a wdb parameter?
	 */
	public boolean isDatabaseField(String variableName) {
		return index.hasParameter(config.wdbName(variableName));
	}

	public Array getGridData(Variable variable, Section section) 
	{
		return Array.factory(DataType.FLOAT, section.getShape());
	}

	private long[] getGridIdentifiers(Variable variable, Section section) {
		return null;
		
//		List<String> dimensions = getDimensionList(config.wdbName(variable.getName()));
//		
//		std::vector<size_t> start = slicer.getDimensionStartPositions();
//		std::vector<size_t> size = slicer.getDimensionSizes();
//
//
//		std::vector<size_t>::iterator st = start.begin();
//		std::advance(st, 2); // skip x/y dimension
//		std::vector<size_t>::iterator sz = size.begin();
//		std::advance(sz, 2); // skip x/y dimension
//		if ( not index_.hasManyVersions(wdbName) )
//		{
//			st = start.insert(st, 0);
//			sz = size.insert(sz, 1);
//		}
//		++ st;
//		++ sz;
//		if ( not index_.hasManyLevels(wdbName) )
//		{
//			st = start.insert(st, 0);
//			sz = size.insert(sz, 1);
//		}
//		++ st;
//		++ sz;
//		if ( not index_.hasManyValidTimeOffsets(wdbName) )
//		{
//			st = start.insert(st, 0);
//			sz = size.insert(sz, 1);
//		}
//		++ st;
//		++ sz;
//		if ( not index_.hasManyReferenceTimes(wdbName) )
//		{
//			st = start.insert(st, 0);
//			sz = size.insert(sz, 1);
//		}
//		++ st;
//		++ sz;
//
//		if ( start.size() != 6 or size.size() != 6 )
//			throw CDMException("Internal error: Generating indices failed");
//
//		std::reverse(start.begin(), start.end());
//		std::reverse(size.begin(), size.end());
//
//		return index_.getData(wdbName, start, size);
	}


	private String addToString(String base, String toAdd) {
		if ( ! base.isEmpty() )
			return base + " " + toAdd;
		return toAdd;
	}
	
	private String getDimensionList(String parameter) {

		String ret = "";
		
		if ( index.hasManyReferenceTimes(parameter) )
			ret = addToString(ret, ReferenceTimeHandler.cfName);
		if ( index.hasManyValidTimeOffsets(parameter) )
		{
			if ( index.hasManyReferenceTimes(parameter) )
				ret = addToString(ret, TimeOffsetHandler.cfName);
			else
				ret = addToString(ret, ValidTimeHandler.cfName);
		}
		if ( index.hasManyLevels(parameter) )
			ret = addToString(ret, config.cfName(index.getLevelForParameter(parameter).getName()));
		if ( index.hasManyVersions(parameter) )
			ret = addToString(ret, VersionHandler.cfName);
	
		ret = addToString(ret, "x");
		ret = addToString(ret, "y");
		
		return ret;
	}

	private void addDimensions(NetcdfFile out) {
		for ( DataHandler handler : dataHandlers )
			handler.addToNetcdfFile(out);
	}
	
	private void addParameterVariables(NetcdfFile out) {

		for ( String parameter : index.allParameters() ) {

			String cfName = config.cfName(parameter);

			Variable var = new Variable(out, null, null, cfName);
			var.setDataType(DataType.FLOAT);

			String dimensions = getDimensionList(parameter);
			var.setDimensions(dimensions);
			
			for ( Attribute attr : config.getAttributes(parameter, index.unitForParameter(parameter)) )
				var.addAttribute(attr);
			var.addAttribute(new Attribute("FillValue", Float.NaN));

			String coordinates = "";
			for ( DataHandler handler : dataHandlers ) {
				String coordinateAddition = handler.getCoordinatesAttributes(parameter);
				if ( coordinateAddition != null && ! coordinateAddition.isEmpty()) {
					if ( ! coordinates.isEmpty() )
						coordinates += " ";
					coordinates += coordinateAddition;
				}
			}
			if ( ! coordinates.isEmpty() )
				var.addAttribute(new Attribute("coordinates", coordinates));
			
//		   setAttribute(attributes, "grid_mapping", gridInfo->getProjectionName());

			out.addVariable(null, var);
		}
	}
}
