/*
    netcdf-java-wdb

    Copyright (C) 2011 met.no

    Contact information:
    Norwegian Meteorological Institute
    Box 43 Blindern
    0313 OSLO
    NORWAY
    E-mail: post@met.no

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
    MA  02110-1301, USA
 */

package no.met.wdb.netcdf;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.jdom.JDOMException;

import no.met.wdb.Grid;
import no.met.wdb.GridData;
import no.met.wdb.WdbConnection;
import no.met.wdb.store.WdbIndex;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.AbstractIOServiceProvider;
import ucar.nc2.util.CancelTask;

/**
 * IOServiceProvider for accessing a wdb database.
 */
public class WdbIOServiceProvider extends AbstractIOServiceProvider { // implements IOServiceProvider {

	private WdbConnection connection = null;
	private NetcdfIndexBuilder index = null; 
	

	/**
	 * Register this class as a netcdf-java IOServiceProvider
	 * 
	 * @throws IOException if unable to find or parse internal configuration
	 */
	public static void register() throws IOException {

		//ucar.nc2.NetcdfFile.registerIOProvider(WdbIOServiceProvider.class);

		InputStream is = WdbIOServiceProvider.class.getClassLoader().getResourceAsStream("wdb-netcdf-java.config.xml");
		StringBuilder errlog = new StringBuilder();
		ucar.nc2.util.xml.RuntimeConfigParser.read(is, errlog);
		
		if ( errlog.length() > 0 )
			throw new IOException(errlog.toString());
	}
	
	@Override
	public boolean isValidFile(ucar.unidata.io.RandomAccessFile raf)
			throws IOException {
		try {
			new WdbConfiguration(raf);
		}
		catch ( JDOMException e ) {
			return false;
		}
		return true;
	}

	static String resourcePath = "wdb_config.xml";
	
	private GlobalWdbConfiguration getGlobalWdbConfiguration() throws IOException, JDOMException {

		ClassLoader classLoader = this.getClass().getClassLoader();
		InputStream globalConfigStream = classLoader.getResourceAsStream(resourcePath);
		
		return new GlobalWdbConfiguration(globalConfigStream);
	}
	
	@Override
	public void open(ucar.unidata.io.RandomAccessFile raf, NetcdfFile ncfile,
			CancelTask cancelTask) throws IOException {
		try {

			WdbConfiguration configuration = new WdbConfiguration(raf);
			
			connection = new WdbConnection(configuration.getDatabaseConnectionSpecification());

			if ( cancelTask != null && cancelTask.isCancel() )
				return;

			Iterable<GridData> gridData = connection.readGid(configuration.getReadQuery());

			index = new NetcdfIndexBuilder(gridData, getGlobalWdbConfiguration());

			if ( cancelTask != null && cancelTask.isCancel() )
				return;

			index.populate(ncfile);
		}
		catch ( Exception e ) {
			throw new IOException(e);
		}
		finally {
			raf.close();
		}
	}

	@Override
	public ucar.ma2.Array readData(Variable v2, Section section)
			throws java.io.IOException, InvalidRangeException {

		//System.out.println("public ucar.ma2.Array readData(Variable(" + v2.getName() + "), Section(" + section.toString() + "))");
		
		ucar.ma2.Array ret;

		if ( index.isDatabaseField(v2.getName()) ) {
			ret = Array.factory(DataType.FLOAT, section.getShape());
			long[] gids = index.getGridIdentifiers(v2, section);

			try {
				int idx = 0;
				for ( long g : gids ) {
					int rank = section.getRank();
					Range yRange = section.getRange(rank -2);
					Range xRange = section.getRange(rank -1);

					if ( g != WdbIndex.UNDEFINED_GID ) {
						Grid grid = connection.getGrid(g);
						float[] data = grid.getGrid();
						for ( int y = yRange.first(); y <= yRange.last(); y += yRange.stride() )
							for ( int x = xRange.first(); x <= xRange.last(); x += xRange.stride() ) {
								int gridIdx = (y * grid.getNumberX()) + x;
								ret.setFloat(idx ++, data[gridIdx]);
							}
					}
					else
						for ( int y = yRange.first(); y <= yRange.last(); y += yRange.stride() )
							for ( int x = xRange.first(); x <= xRange.last(); x += xRange.stride() )
								ret.setFloat(idx ++, Float.NaN);
				}
			}
			catch (SQLException e) {
				throw new IOException(e);
			}
		}
		else
			ret = index.getMetadata(v2, section);
		
		if ( ret == null )
			throw new IOException("Internal error: Unable to read data: " + v2.getName());
		
		return ret;
	}


	@Override
	public void close() throws IOException {
		if ( connection != null )
			connection.close();
	}

	@Override
	public String getFileTypeId() {
		return "wbd";
	}

	@Override
	public String getFileTypeVersion() {
		return "1.0.0";
	}

	@Override
	public String getFileTypeDescription() {
		return "wdb connection";
	}
}
