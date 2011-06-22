package no.met.wdb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * A single return row from wci.read(..., wci.returnGid);
 */
public class GridData {

	private long value;
	private String dataProviderName;
	private String placeName;
	private String placeGeometry;
	private PlaceRegularGrid grid = null;
	private Date referenceTime;
	private Date validTimeFrom;
	private Date validTimeTo;
	private int validTimeIndeterminateCode;
	private Parameter valueParameter;
	private Level level;
	private float levelFrom;
	private float levelTo;
	private int levelIndeterminateCode;
	private int dataVersion;
	private int confidenceCode;
	private Date storeTime;
	private long valueID;
	private int valueType;
	
	
	private Date getDate(ResultSet queryResult, int index) throws SQLException {
		java.sql.Timestamp ret = queryResult.getTimestamp(index);
		if ( ret == null )
			return null;
		return new Date(ret.getTime());
	}

	
	/**
	 * Initialize object, based on a wci.read query result.
	 * 
	 * @param queryResult A result row, as returned from a database call to 
	 * wci.read(..., wci.returngid).  Note that next() will not be called on 
	 * the given object. It is the caller's responsibility to do this.
	 * 
	 * @throws SQLException in case something was wrong with the given queryResult.
	 */
	GridData(ResultSet queryResult) throws SQLException {
		int i = 0;
		value = queryResult.getLong(++i);
		dataProviderName = queryResult.getString(++i);
		placeName = queryResult.getString(++i);
		placeGeometry = queryResult.getString(++i);
		referenceTime = getDate(queryResult, ++i);
		validTimeFrom = getDate(queryResult, ++i);
		validTimeTo = getDate(queryResult, ++i);
		validTimeIndeterminateCode = queryResult.getInt(++i);
		String valueParameterName = queryResult.getString(++i);
		String valueParameterUnit = queryResult.getString(++i);
		valueParameter = new Parameter(valueParameterName, valueParameterUnit);
		String levelParameterName = queryResult.getString(++i);
		String levelParameterUnit = queryResult.getString(++i);
		level = new Level(levelParameterName, levelParameterUnit);
		levelFrom = queryResult.getFloat(++i);
		levelTo = queryResult.getFloat(++i);
		levelIndeterminateCode = queryResult.getInt(++i);
		dataVersion = queryResult.getInt(++i);
		confidenceCode = queryResult.getInt(++i);
		storeTime = getDate(queryResult, ++i);
		valueID = queryResult.getLong(++i);
		valueType = queryResult.getInt(++i);
	}

	/**
	 * This is meant for tests
	 */
	protected GridData(	
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
			long valueID,
			int valueType
	) {
		this.value = value;
		this.dataProviderName = dataProviderName;
		this.placeName = placeName;
		this.placeGeometry = placeGeometry;
		this.referenceTime = referenceTime;
		this.validTimeFrom = validTimeFrom;
		this.validTimeTo = validTimeTo;
		this.validTimeIndeterminateCode = validTimeIndeterminateCode;
		this.valueParameter = new Parameter(valueParameterName, valueParameterUnit);
		level = new Level(levelParameterName, levelParameterUnit);
		this.levelFrom = levelFrom;
		this.levelTo = levelTo;
		this.levelIndeterminateCode = levelIndeterminateCode;
		this.dataVersion = dataVersion;
		this.confidenceCode = confidenceCode;
		this.storeTime = storeTime;
		this.valueID = valueID;
		this.valueType = valueType;
	}
	
	@Override
	public String toString() {
		String s = " | "; // separator
		return value+s+dataProviderName+s+placeName+s+placeGeometry+s+referenceTime+s+validTimeFrom+s+validTimeTo
		+s+validTimeIndeterminateCode+s+valueParameter.getName()+s+valueParameter.getUnit()+s+level.getName()+s+level.getUnit()
		+s+levelFrom+s+levelTo+s+levelIndeterminateCode+s+dataVersion+s+confidenceCode+s+storeTime+s+valueID+s+valueType;
	}

	public long getValue() {
		return value;
	}

	public String getDataProviderName() {
		return dataProviderName;
	}

	public String getPlaceName() {
		return placeName;
	}

	public String getPlaceGeometry() {
		return placeGeometry;
	}

	void setGrid(PlaceRegularGrid grid) {
		this.grid = grid;
	}
	
	public PlaceRegularGrid getGrid() {
		return grid;
	}
	
	public Date getReferenceTime() {
		return referenceTime;
	}

	public Date getValidTimeFrom() {
		return validTimeFrom;
	}

	public Date getValidTimeTo() {
		return validTimeTo;
	}

	public int getValidTimeIndeterminateCode() {
		return validTimeIndeterminateCode;
	}

	public Parameter getValueParameter() {
		return valueParameter;
	}
	
	public String getValueParameterName() {
		return valueParameter.getName();
	}

	public String getValueParameterUnit() {
		return valueParameter.getUnit();
	}

	public Level getLevel() {
		return level;
	}

	public float getLevelFrom() {
		return levelFrom;
	}

	public float getLevelTo() {
		return levelTo;
	}

	public int getLevelIndeterminateCode() {
		return levelIndeterminateCode;
	}

	public int getDataVersion() {
		return dataVersion;
	}

	public int getConfidenceCode() {
		return confidenceCode;
	}

	public Date getStoreTime() {
		return storeTime;
	}

	public long getValueID() {
		return valueID;
	}

	public int getValueType() {
		return valueType;
	}


	protected void setValue(long value) {
		this.value = value;
	}


	protected void setDataProviderName(String dataProviderName) {
		this.dataProviderName = dataProviderName;
	}


	protected void setPlaceName(String placeName) {
		this.placeName = placeName;
	}


	protected void setPlaceGeometry(String placeGeometry) {
		this.placeGeometry = placeGeometry;
	}


	protected void setReferenceTime(Date referenceTime) {
		this.referenceTime = referenceTime;
	}


	protected void setValidTimeFrom(Date validTimeFrom) {
		this.validTimeFrom = validTimeFrom;
	}


	protected void setValidTimeTo(Date validTimeTo) {
		this.validTimeTo = validTimeTo;
	}


	protected void setValidTimeIndeterminateCode(int validTimeIndeterminateCode) {
		this.validTimeIndeterminateCode = validTimeIndeterminateCode;
	}

	protected void setValueParameter(Parameter valueParameter)
	{
		this.valueParameter = valueParameter;
	}
	
	protected void setLevel(Level level) {
		this.level = level;
	}
	

	protected void setLevelFrom(float levelFrom) {
		this.levelFrom = levelFrom;
	}


	protected void setLevelTo(float levelTo) {
		this.levelTo = levelTo;
	}


	protected void setLevelIndeterminateCode(int levelIndeterminateCode) {
		this.levelIndeterminateCode = levelIndeterminateCode;
	}


	protected void setDataVersion(int dataVersion) {
		this.dataVersion = dataVersion;
	}


	protected void setConfidenceCode(int confidenceCode) {
		this.confidenceCode = confidenceCode;
	}


	protected void setStoreTime(Date storeTime) {
		this.storeTime = storeTime;
	}


	protected void setValueID(long valueID) {
		this.valueID = valueID;
	}


	protected void setValueType(int valueType) {
		this.valueType = valueType;
	}
}
