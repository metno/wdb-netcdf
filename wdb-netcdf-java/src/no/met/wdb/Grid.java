package no.met.wdb;

import java.io.DataInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Holds a grid with data, along with a minimum of metadata information. This 
 * object is not created directly, but through the 
 * {@link no.met.wdb.WdbConnection#getGrid(long)} method. 
 */
public class Grid {

	private float grid[];
	private int numberX;
	private int numberY;
	private float incrementX;
	private float incrementY;
	private float startX;
	private float startY;
	private String projDefinition;
	
	Grid(ResultSet queryResult) throws SQLException {

		int i = 1;
		numberX = queryResult.getInt(++i);
		numberY = queryResult.getInt(++i);
		incrementX = queryResult.getFloat(++i);
		incrementY = queryResult.getFloat(++i);
		startX = queryResult.getFloat(++i);
		startY = queryResult.getFloat(++i);
		projDefinition = queryResult.getString(++i);
		
		int size = numberX * numberY;
		
		grid = new float[size];
		
		try {
			DataInputStream gridStream = new DataInputStream(queryResult.getBinaryStream(1));
			
			for ( int j = 0; j < size; ++ j ) {
				int val = gridStream.readInt();
				val = Integer.reverseBytes(val);
				grid[j] = Float.intBitsToFloat(val);
			}
		}
		catch ( IOException e ) {
			throw new SQLException(e);
		}
	}

	/**
	 * Get the query that willl be used to produce a Grid object.
	 * @param gridId Identifier for the grid we want.
	 * @return An SQL formatted string for getting grid data from a wdb database. 
	 */
	public static String query(long gridId) {
		return "SELECT grid, numberx::int, numbery::int, incrementx, incrementy, startx, starty, projdefinition FROM wci.fetch(" + gridId + ", NULL::wci.grid)";
	}

	/**
	 * Access the grid.
	 * @return The data in the grid
	 */
	public float[] getGrid() {
		return grid;
	}

	public int getNumberX() {
		return numberX;
	}

	public int getNumberY() {
		return numberY;
	}

	public float getIncrementX() {
		return incrementX;
	}

	public float getIncrementY() {
		return incrementY;
	}

	public float getStartX() {
		return startX;
	}

	public float getStartY() {
		return startY;
	}

	public String getProjDefinition() {
		return projDefinition;
	}

	
}
