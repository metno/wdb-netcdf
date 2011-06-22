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
		return index.hasParameter(variableName);
	}

	/**
	 * Does the given name refer to a level?
	 */
	public boolean isLevel(String levelName)
	{
		try
		{
			getLevelValues(levelName);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Get all values for the given level type
	 */
	public TreeSet<Float> getLevelValues(String levelName) {
		return index.getLevelValues(levelName);
	}


	public long[] getGridIdentifiers(Variable variable, Section section) {
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

	/**
	 * Get information about the grid in use.
	 */
	//public const GridInformation gridInformation() {}

	/**
	 * Get a list of all times in use
	 */
	public TreeSet<Long> allTimes()
	{
		return index.getAllValidtimes();
	}

	/**
	 * Get the reference time for the data in this object.
	 */
	public TreeSet<Date> referenceTimes() {
		return index.getAllReferenceTimes();
	}

	public Vector<DataHandler> dataHandlers() { 
		return dataHandlers; 
	}

	private List<String> getDimensionList(String parameter) {

		List<String> ret = new Vector<String>();
		
		//gridInformation().addSpatialDimensions(ret);

		if ( index.hasManyVersions(parameter) )
			ret.add(VersionHandler.cfName);
		if ( index.hasManyLevels(parameter) )
			ret.add(config.cfName(index.getLevelForParameter(parameter).getName()));
		if ( index.hasManyValidTimeOffsets(parameter) )
		{
			if ( index.hasManyReferenceTimes(parameter) )
				ret.add(TimeOffsetHandler.cfName);
			else
				ret.add(ValidTimeHandler.cfName);
		}
		if ( index.hasManyReferenceTimes(parameter) )
			ret.add(ReferenceTimeHandler.cfName);
	
		ret.add("x");
		ret.add("y");
		
		return ret;
	}

	private void addDimensions(NetcdfFile out) {
		for ( DataHandler handler : dataHandlers )
			handler.addToNetcdfFile(out);
	}
	
	private void addParameterVariables(NetcdfFile out) {

		for ( String parameter : index.allParameters() ) {

//			GridSpecMap::const_iterator find = grids_.find(parameter);
//			if ( find == grids_.end() )
//				throw CDMException("Internal error - unable to find grid mapping"); // should never happen
//			GridData::GridInformationPtr gridInfo = find->second;

			String cfName = config.cfName(parameter);

			Variable var = new Variable(out, null, null, cfName);
			var.setDataType(DataType.FLOAT);

//			List<String> dimensions = getDimensionList(parameter);
//			var.setDimensions(dimensions);
			
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
