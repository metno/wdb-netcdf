package no.met.wdb.store;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import no.met.wdb.GridData;
import no.met.wdb.Level;

/**
 * Provides information about a wdb grid data set as a whole. This object may be
 * used to acquire information about such things as what levels are available
 * for a given parameter.
 */
public class WdbIndex {

	private HashMap<String, ParameterData> data = new HashMap<String, ParameterData>();
	private HashMap<String, TreeSet<Float>> levels = new HashMap<String, TreeSet<Float>>();
	TreeSet<Long> allValidtimes = new TreeSet<Long>();
	TreeSet<Date> allReferenceTimes = new TreeSet<Date>();
	HashMap<String, String> parameterToUnit = new HashMap<String, String>();
	
	private NameTranslator translator;

	class SimpleTranslator implements NameTranslator {
		@Override
		public String translate(String wdbName) {
			return wdbName.replace(' ', '_');
		}
	}

	public WdbIndex(Iterable<GridData> gridData, NameTranslator nameTranslator)
			throws DuplicateDataException, IndexCreationException {

		if (nameTranslator == null)
			translator = new SimpleTranslator();
		else
			translator = nameTranslator;

		HashMap<String, DataSummary> dataSummary = new HashMap<String, DataSummary>();

		// index all elements
		for (GridData d : gridData) {
			String parameter = d.getValueParameter().getName();
			DataSummary summary = dataSummary.get(parameter);
			if (summary == null) {
				summary = new DataSummary();
				dataSummary.put(parameter, summary);
			}
			summary.add(d);
		}

		// extend indexes, so that parameters with (eg.) several versions gets
		// all nonunique versions
		for (DataSummary a : dataSummary.values())
			for (DataSummary b : dataSummary.values())
				a.mergeWith(b);

		for (Map.Entry<String, DataSummary> s : dataSummary.entrySet())
			data.put(translator.translate(s.getKey()),
					new ParameterData(s.getValue(), gridData));

		for (GridData d : gridData) {
			allValidtimes.add((d.getValidTimeTo().getTime() - d
					.getReferenceTime().getTime()) / 1000);
			allReferenceTimes.add(d.getReferenceTime());
			parameterToUnit.put(translator.translate(d.getValueParameter()
					.getName()), d.getValueParameter().getUnit());
			
			String levelName = translator.translate(d.getLevel().getName());
			TreeSet<Float> lvl = levels.get(levelName);
			if (lvl == null) {
				lvl = new TreeSet<Float>();
				levels.put(levelName, lvl);
			}
			lvl.add(d.getLevelTo());
		}

		if (allReferenceTimes.isEmpty())
			throw new IndexCreationException("No data");
	}

	public static final long UNDEFINED_GID = -1;

	public long[] getData(String parameter, ucar.ma2.Range referenceTime,
			ucar.ma2.Range validTime, ucar.ma2.Range level,
			ucar.ma2.Range version) {
		int size = referenceTime.length() * validTime.length() * level.length()
				* version.length();
		long[] ret = new long[size];

		ParameterData parameterData = data.get(parameter);
		if (parameterData == null)
			throw new IllegalArgumentException(parameter
					+ ": no such parameter");
		long[][][][] d = parameterData.getData();

		int idx = 0;

		try {
			for (int r = referenceTime.first(); r <= referenceTime.last(); r += referenceTime
					.stride())
				for (int t = validTime.first(); t <= validTime.last(); t += validTime
						.stride())
					for (int l = level.first(); l <= level.last(); l += level
							.stride())
						for (int v = version.first(); v <= version.last(); v += version
								.stride())
							ret[idx++] = d[r][t][l][v];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException(e);
		}

		return ret;
	}

	/**
	 * Get a list of all parameters that are stored here
	 */
	public Set<String> allParameters() {
		return data.keySet();
	}

	/**
	 * Get the unit name for the given parameter
	 */
	public String unitForParameter(String parameter) {
		return parameterToUnit.get(parameter);
	}

	/**
	 * Get a list of all times that are in use.
	 */
	public TreeSet<Long> getAllValidtimes() {
		return allValidtimes;
	}

	/**
	 * Get all available times for the given parameter
	 */
	public Vector<Long> timesForParameter(String parameter) {
		return parameterData(parameter).getValidTimes();
	}

	public boolean hasLevel(String level) {
		return levels.containsKey(level);
	}

	/**
	 * Find a parameter's level type
	 */
	public Level getLevelForParameter(String parameter) {
		return parameterData(parameter).getLevel();
	}

	/**
	 * Get a list of all available levels for the given parameter
	 */
	public Vector<Float> levelsForParameter(String parameter) {
		return parameterData(parameter).getLevels();
	}

	/**
	 * Get a list of all dataversions for a given parameter
	 */
	public Vector<Integer> versionsForParameter(String parameter) {
		return parameterData(parameter).getVersions();
	}

	/**
	 * does the given parameter name exist in this object?
	 */
	public boolean hasParameter(String parameter) {
		return data.containsKey(parameter);
	}

	public TreeSet<Float> getLevelValues(String levelName) {
		return levels.get(levelName);
	}

	/**
	 * Get data's reference time
	 */
	public TreeSet<Date> getAllReferenceTimes() {
		return allReferenceTimes;
	}

	public Vector<Date> referenceTimesForParameter(String parameter) {
		return parameterData(parameter).getReferenceTimes();
	}

	public boolean hasManyReferenceTimes(String parameter) {
		return parameterData(parameter).getReferenceTimes().size() > 1;
	}

	public boolean hasManyValidTimeOffsets(String parameter) {
		return parameterData(parameter).getValidTimes().size() > 1;
	}

	public boolean hasManyLevels(String parameter) {
		return parameterData(parameter).getLevels().size() > 1;
	}

	public boolean hasManyVersions(String parameter) {
		return parameterData(parameter).getVersions().size() > 1;
	}

	/**
	 * Get access to the translator instance that was used to translate wdb 
	 * names into cf names. 
	 */
	public NameTranslator getTranslator() {
		return translator;
	}
	
	private ParameterData parameterData(String parameter) {
		ParameterData ret = data.get(parameter);
		if (ret == null)
			throw new IllegalArgumentException(parameter
					+ ": no such parameter");
		return ret;
	}
}
