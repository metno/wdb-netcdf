package no.met.wdb.store;

import java.util.Date;
import java.util.TreeSet;

import no.met.wdb.GridData;
import no.met.wdb.Level;
import no.met.wdb.Parameter;

class DataSummary {

	Parameter parameter;
	TreeSet<Date> referenceTimes = new TreeSet<Date>();
	TreeSet<Long> validTimes = new TreeSet<Long>();
	Level level = null;
	TreeSet<Float> levelValues = new TreeSet<Float>();
	TreeSet<Integer> versions = new TreeSet<Integer>();

	public void add(GridData gridData) throws DuplicateDataException {
		parameter = gridData.getValueParameter();

		referenceTimes.add(gridData.getReferenceTime());
		validTimes.add((gridData.getValidTimeTo().getTime()
				- gridData.getReferenceTime().getTime()) / 1000);
		if (level != null) {
			if (!level.equals(gridData.getLevel())) {

				String msg = "Multiple level types in same parameter: "
						+ gridData.getValueParameter().toString() + "\t"
						+ level.toString() + " and "
						+ gridData.getLevel().toString();
				throw new DuplicateDataException(msg);
			}
		} else
			level = gridData.getLevel();
		levelValues.add(gridData.getLevelTo());
		versions.add(gridData.getDataVersion());

	}

	public void mergeWith(DataSummary other) {

		if (referenceTimes.size() > 1)
			referenceTimes.addAll(other.getReferenceTimes());
		if (validTimes.size() > 1)
			validTimes.addAll(other.getValidTimes());
		if (level.equals(other.getLevel()) && levelValues.size() > 1)
			levelValues.addAll(other.getLevelValues());
		if (versions.size() > 1)
			versions.addAll(other.getVersions());
	}

	public Parameter getParameter() {
		return parameter;
	}

	public TreeSet<Date> getReferenceTimes() {
		return referenceTimes;
	}

	public TreeSet<Long> getValidTimes() {
		return validTimes;
	}

	public Level getLevel() {
		return level;
	}

	public TreeSet<Float> getLevelValues() {
		return levelValues;
	}

	public TreeSet<Integer> getVersions() {
		return versions;
	}

}
