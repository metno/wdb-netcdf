package no.met.wdb.netcdf;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * Handling data for wdb/fimex. Objects of this class may be used to add time
 * dimensions and -variables to CDM objects, and to get values for the
 * same variables.
 */
interface DataHandler {

	/**
	 * Add relevant dimensions and -variables to the given CDM object.
	 */
	void addToNetcdfFile(NetcdfFile out);

	/**
	 * Get data for the given variable name and unlimited dimension
	 */
	Array getData(Variable variable);

	/**
	 * Does the given cdm variable name refer to anything that this object can
	 * handle via the getData method?
	 */
	boolean canHandle(Variable variable);

	/**
	 * Get names of variables that needs entries in a parameter's coordinates 
	 * attribute
	 */
	public String getCoordinatesAttributes(String cfName);
}
