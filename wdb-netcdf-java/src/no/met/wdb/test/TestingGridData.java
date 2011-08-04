package no.met.wdb.test;

import java.util.Date;

import no.met.wdb.GridData;
import no.met.wdb.Level;
import no.met.wdb.Parameter;

/**
 * Creating fake GridData objects, without going through any database.
 */
public class TestingGridData extends GridData {

	private static long gidCounter = 0;
	public static void resetGidCounter() {
		gidCounter = 0;
	}
	
	
	public static final String defaultDataProvider = "data provider";
	public static final String defaultLocation = "somewhere";
	@SuppressWarnings("deprecation")
	public static final Date defaultValidTime = new Date(2011,4,14,6,0,0);
	public static final String defaultValueParameter = "air temperature";
	public static final String defaultValueParameterUnit = "C";
	public static final String defaultLevel = "height above ground";
	public static final String defaultLevelUnit = "m";
	public static final float defaultLevelValue = 2;

	public static TestingGridData get(int hourOffsetFromDefaultValidTime) {
		TestingGridData ret = get();
		Date newDate = new Date(defaultValidTime.getTime() + (hourOffsetFromDefaultValidTime * 60 * 60 * 1000));
		ret.setValidTimeFrom(newDate);
		ret.setValidTimeTo(newDate);
		return ret;
	}

	public static TestingGridData get(int hourOffsetFromDefaultValidTime, int dataVersion) {
		TestingGridData ret = get(hourOffsetFromDefaultValidTime);
		ret.setDataVersion(dataVersion);
		return ret;
	}

	
	public static TestingGridData get(int hourOffsetFromDefaultValidTime, int dataVersion, String valueParameterName, String valueParameterUnit) {
		TestingGridData ret = get(hourOffsetFromDefaultValidTime);
		ret.setDataVersion(dataVersion);
		
		ret.setValueParameter(new Parameter(valueParameterName, valueParameterUnit));
		return ret;
	}

	
	public static TestingGridData get(String valueParameterName, String valueParameterUnit) {
		TestingGridData ret = get();
		ret.setValueParameter(new Parameter(valueParameterName, valueParameterUnit));
		return ret;
	}

	public static TestingGridData get(int hourOffsetFromDefaultValidTime, String valueParameterName, String valueParameterUnit) {
		TestingGridData ret = get(hourOffsetFromDefaultValidTime);
		ret.setValueParameter(new Parameter(valueParameterName, valueParameterUnit));
		return ret;
	}
	
	public static TestingGridData get(Level level, float value) {
		TestingGridData ret = get();
		ret.setLevel(level);
		ret.setLevelFrom(value);
		ret.setLevelTo(value);
		return ret;
	}

	public static TestingGridData get(String valueParameterName, String valueParameterUnit, Level level, float value) {
		TestingGridData ret = get(level, value);
		ret.setValueParameter(new Parameter(valueParameterName, valueParameterUnit));
		return ret;
	}

	public static TestingGridData get(int hourOffsetFromDefaultValidTime, String valueParameterName, String valueParameterUnit, Level level, float value) {
		TestingGridData ret = get(hourOffsetFromDefaultValidTime);
		ret.setLevel(level);
		ret.setLevelFrom(value);
		ret.setLevelTo(value);
		ret.setValueParameter(new Parameter(valueParameterName, valueParameterUnit));
		return ret;
	}
	
	
	public static TestingGridData get(int hourOffsetFromDefaultValidTime, Level level, float value) {
		TestingGridData ret = get(hourOffsetFromDefaultValidTime);
		ret.setLevel(level);
		ret.setLevelFrom(value);
		ret.setLevelTo(value);
		return ret;
	}

	
	public static TestingGridData get() {
		
		@SuppressWarnings("deprecation")
		Date d = new Date(2011,4,20,6,0,0);
		
		TestingGridData ret = new TestingGridData(
				++ gidCounter,
				defaultDataProvider,
				defaultLocation,
				defaultLocation,
				d,
				defaultValidTime,
				defaultValidTime,
				0,
				defaultValueParameter,
				defaultValueParameterUnit,
				defaultLevel,
				defaultLevelUnit,
				defaultLevelValue,
				defaultLevelValue,
				0,
				0,
				0,
				d,
				0
				);
		
		TestingPlaceRegularGrid grid = new TestingPlaceRegularGrid();
		ret.setGrid(grid);
		
		return ret;
	}
	
	
	
	private TestingGridData(
			long value, 
			String dataProviderName,
			String placeName, 
			String placeGeometry, 
			Date referenceTime,
			Date validTimeFrom, 
			Date validTimeTo,
			int validTimeIndeterminateCode, 
			String valueParameterName,
			String valueParameterUnit, 
			String levelParameterName,
			String levelParameterUnit, 
			float levelFrom, 
			float levelTo,
			int levelIndeterminateCode, 
			int dataVersion, 
			int confidenceCode,
			Date storeTime, 
			long valueID) {
		
		super(value, 
				dataProviderName, 
				placeName, 
				placeGeometry, 
				referenceTime,
				validTimeFrom, 
				validTimeTo, 
				validTimeIndeterminateCode,
				valueParameterName, 
				valueParameterUnit, 
				levelParameterName,
				levelParameterUnit, 
				levelFrom, 
				levelTo, 
				levelIndeterminateCode,
				dataVersion, 
				confidenceCode, 
				storeTime, 
				valueID);
	}

}
