package no.met.wdb;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Grid definition for regular grids.
 */
public class PlaceRegularGrid {

	private String placeGeometry;
	private String placeName;
	private int numberX;
	private int numberY;
	private float incrementX;
	private float incrementY;
	private float startX;
	private float startY;
	private String projDefinition;
	
	PlaceRegularGrid(ResultSet queryResult) throws SQLException {
		
		int i = 0;
		placeGeometry = queryResult.getString(++i);
		placeName = queryResult.getString(++i);
		numberX = queryResult.getInt(++i);
		numberY = queryResult.getInt(++i);
		incrementX = queryResult.getFloat(++i);
		incrementY = queryResult.getFloat(++i);
		startX = queryResult.getFloat(++i);
		startY = queryResult.getFloat(++i);
		projDefinition = queryResult.getString(++i);
	}

	public String getPlaceGeometry() {
		return placeGeometry;
	}

	public String getPlaceName() {
		return placeName;
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
	
	@Override
	public boolean equals(Object other) {
		try {
			return equals((PlaceRegularGrid) other);
		}
		catch ( ClassCastException e)
		{
			return super.equals(other);
		}
	}
	
	public boolean equals(PlaceRegularGrid other) {
		return placeGeometry.equals(other.placeGeometry) 
		&& placeName.equals(other.placeName)
		&& numberX == other.numberY
		&& numberY == other.numberY
		&& incrementX == other.incrementX
		&& incrementY == other.incrementY
		&& startX == other.startX
		&& startY == other.startY
		&& projDefinition.equals(other.projDefinition);
	}
	
	@Override
	public int hashCode() {
		int ret;
		
		ret = placeGeometry.hashCode();
		ret ^= placeName.hashCode();
		ret ^= numberX;
		ret ^= numberY;
		ret ^= Float.floatToIntBits(incrementX);
		ret ^= Float.floatToIntBits(incrementY);
		ret ^= Float.floatToIntBits(startX);
		ret ^= Float.floatToIntBits(startY);
		ret ^= projDefinition.hashCode();

		return ret;
	}
}
