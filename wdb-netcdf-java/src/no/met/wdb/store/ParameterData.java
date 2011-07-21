package no.met.wdb.store;

import java.util.Date;
import java.util.Vector;

import no.met.wdb.GridData;
import no.met.wdb.Level;

class ParameterData {

	private long[][][][] data;
	private Vector<Date> referenceTimes;
	private Vector<Long> validTimes;
	private Level level;
	private Vector<Float> levels;
	private Vector<Integer> versions;

	private <T> int indexOf(T value, Iterable<T> container) {
		int idx = 0;
		for (T t : container) {
			if (t.equals(value))
				return idx;
			idx++;
		}
		throw new RuntimeException(
				"Internal error: Invalid request for index of "
						+ value.toString());
	}

	public ParameterData(DataSummary s, Iterable<GridData> gridData)
			throws DuplicateDataException {
		referenceTimes = new Vector<Date>(s.getReferenceTimes());
		validTimes = new Vector<Long>(s.getValidTimes());
		level = s.getLevel();
		levels = new Vector<Float>(s.getLevelValues());
		versions = new Vector<Integer>(s.getVersions());

		data = new long[referenceTimes.size()][validTimes.size()][levels.size()][versions
				.size()];

		for (int r = 0; r < referenceTimes.size(); r++)
			for (int t = 0; t < validTimes.size(); t++)
				for (int l = 0; l < levels.size(); l++)
					for (int v = 0; v < versions.size(); v++)
						data[r][t][l][v] = WdbIndex.UNDEFINED_GID;

		for (GridData d : gridData) {
			if (s.getParameter().equals(d.getValueParameter())) {

				int r = indexOf(d.getReferenceTime(), referenceTimes);
				int t = indexOf((d.getValidTimeTo().getTime() - d.getReferenceTime().getTime()) / 1000, validTimes);
				int l = indexOf(d.getLevelTo(), levels);
				int v = indexOf(d.getDataVersion(), versions);

				long oldValue = data[r][t][l][v];
				if (oldValue != WdbIndex.UNDEFINED_GID)
					throw new DuplicateDataException("duplicate data in souce");
				data[r][t][l][v] = d.getValue();

				level = d.getLevel();
			}
		}
	}

	public long[][][][] getData() {
		return data;
	}

	public Vector<Date> getReferenceTimes() {
		return referenceTimes;
	}

	public Vector<Long> getValidTimes() {
		return validTimes;
	}

	public Level getLevel() {
		return level;
	}

	public Vector<Float> getLevels() {
		return levels;
	}

	public Vector<Integer> getVersions() {
		return versions;
	}

	// private enum DataIndices
	// {
	// ReferenceTimeIndex, ValidTimeIndex, LevelIndex, VersionIndex
	// };
}
