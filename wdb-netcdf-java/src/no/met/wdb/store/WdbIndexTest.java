package no.met.wdb.store;

import static org.junit.Assert.*;

import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;

import no.met.wdb.GridData;
import no.met.wdb.Level;
import no.met.wdb.test.TestingGridData;

public class WdbIndexTest {

	private Vector<GridData> gridData = new Vector<GridData>();

	
	
	private long[] process() throws IndexCreationException {
		return process(TestingGridData.defaultValueParameter);
	}

	
	private long[] process(String valueParameter) throws IndexCreationException  {
		return process(valueParameter, 0);
	}

	
	private long[] process(int timeIndex) throws IndexCreationException  {
		return process(TestingGridData.defaultValueParameter, timeIndex);
	}

	
	private long[] process(String valueParameter, int timeIndex) throws IndexCreationException  {
		WdbIndex index = new WdbIndex(gridData, null);
		
		try {
			return index.getData(valueParameter, 
					new Range(index.getAllReferenceTimes().size()), 
					new Range(timeIndex, timeIndex), 
					new Range(index.hasManyLevels(valueParameter) ? index.levelsForParameter(valueParameter).size() : 1), 
					new Range(index.versionsForParameter(valueParameter).size()));
		} catch (InvalidRangeException e) {
			throw new RuntimeException(e); // should never happen
		}
		//public long[] getData(String parameter, ucar.ma2.Range referenceTime, ucar.ma2.Range validTime, ucar.ma2.Range level, ucar.ma2.Range version) {
	}
	
	
	@Before
	public void setUp() {
		TestingGridData.resetGidCounter();
	}
	
	
	@Test
	public void simpleCase() throws IndexCreationException  {
		gridData.add(TestingGridData.get());
		long[] data = process();
		
		assertEquals(1, data.length);
		assertEquals(1, data[0]);
	}

	
	@Test
	public void requestFirstTimeEntry() throws IndexCreationException {
		// we insert data with no order to time 
		gridData.add(TestingGridData.get(2));
		gridData.add(TestingGridData.get(1));
		gridData.add(TestingGridData.get(0));

		long[] data = process(0);

		assertEquals(1, data.length);
		assertEquals(3, data[0]);
	}

	
	@Test
	public void requestSecondTimeEntry() throws IndexCreationException {
		// we insert data with no order to time 
		gridData.add(TestingGridData.get(2));
		gridData.add(TestingGridData.get(0));
		gridData.add(TestingGridData.get(1));

		long[] data = process(1);

		assertEquals(1, data.length);
		assertEquals(3, data[0]);
	}
	

	@Test
	public void throwOnSameData() throws IndexCreationException {
		gridData.add(TestingGridData.get());
		gridData.add(TestingGridData.get());
		try {
			process();
			fail("expected DuplicateDataException");
		}
		catch ( DuplicateDataException e) {
		}
	}


	@Test
	public void throwsOnInvalidTimeIndex() throws IndexCreationException {

		gridData.add(TestingGridData.get());
		try {
			process(1);
			fail("Expected IllegalArgumentException exception");
		}
		catch ( IllegalArgumentException success ) {
		}
	}

	@Test
	public void throwOnRequestForNonexistingParameter() throws IndexCreationException {

		gridData.add(TestingGridData.get("pressure", "pa"));
		try {
			process("temperature");
			fail("Expected IllegalArgumentException exception");
		}
		catch ( IllegalArgumentException success ) {
		}
	}


	@Test
	public void selectSingleParameter() throws IndexCreationException  {
		gridData.add(TestingGridData.get("temperature", "C"));
		gridData.add(TestingGridData.get("pressure", "pa"));
		
		WdbIndex index = new WdbIndex(gridData, null);
		
		long[] data = index.getData("temperature", new Range(1), new Range(1), new Range(1), new Range(1));
		assertEquals(1, data.length);
		assertEquals(1, data[0]);

		data = index.getData("pressure", new Range(1), new Range(1), new Range(1), new Range(1));
		assertEquals(1, data.length);
		assertEquals(2, data[0]);
	}


	@Test
	public void selectParameterWithMissingTimeEntry() throws IndexCreationException {
		gridData.add(TestingGridData.get(0, "air temperature", "C"));
		gridData.add(TestingGridData.get(1, "air temperature", "C"));
		gridData.add(TestingGridData.get(2, "air temperature", "C"));
		gridData.add(TestingGridData.get(0, "air pressure", "pa"));
		//gridData.add(TestingGridData.get(1, "air pressure", "pa"));
		gridData.add(TestingGridData.get(2, "air pressure", "pa"));
		
		long[] missing = process("air pressure", 1);
		assertEquals(1, missing.length);
		assertEquals(WdbIndex.UNDEFINED_GID, missing[0]);
	}

	
	@Test
	public void parameterWithSingleTime() throws IndexCreationException {
		gridData.add(TestingGridData.get(0, "temperature", "c"));
		gridData.add(TestingGridData.get(1, "temperature", "c"));
		gridData.add(TestingGridData.get(2, "temperature", "c"));
		gridData.add(TestingGridData.get(1, "terrain height", "m"));
	
		long[] data = process("terrain height", 0);
		assertEquals(1, data.length);
		assertEquals(4, data[0]);
	}


	@Test
	public void getLevels() throws IndexCreationException  {
		Level lvl = new Level("lvl", "m");
		gridData.add(TestingGridData.get(lvl, 0));
		gridData.add(TestingGridData.get(lvl, 1));
		gridData.add(TestingGridData.get(lvl, 2));
		long[] data = process();

		assertEquals(3, data.length);
		assertEquals(1, data[0]);
		assertEquals(2, data[1]);
		assertEquals(3, data[2]);
	}
	

	@Test
	public void twoLevelsComingInWrongOrder() throws IndexCreationException  {
		Level lvl = new Level("lvl", "m");
		gridData.add(TestingGridData.get(lvl, 1));
		gridData.add(TestingGridData.get(lvl, 0));
		long[] data = process();
	
		assertEquals(2, data.length);
		assertEquals(2, data[0]);
		assertEquals(1, data[1]);
	}


	@Test
	public void getMissingLevels() throws IndexCreationException {
		Level lvl = new Level("height", "m");
		gridData.add(TestingGridData.get(0, lvl, 0));
		//gridData.add(TestingGridData.get(0, lvl, 1)); // missing
		gridData.add(TestingGridData.get(0, lvl, 2));
		gridData.add(TestingGridData.get(1, lvl, 0));
		gridData.add(TestingGridData.get(1, lvl, 1));
		gridData.add(TestingGridData.get(1, lvl, 2));
		long[] data = process(0);

		assertEquals(3, data.length);
		assertEquals(1, data[0]);
		assertEquals(WdbIndex.UNDEFINED_GID, data[1]);
		assertEquals(2, data[2]);
	}
	

	@Test
	public void getMissingLevelsForMissingTimes() throws IndexCreationException {
		gridData.add(TestingGridData.get(1, "pressure", "pa"));
		gridData.add(TestingGridData.get(0, "pressure", "pa"));
		gridData.add(TestingGridData.get(2, "pressure", "pa"));

		Level lvl = new Level("height", "m");
		gridData.add(TestingGridData.get(1, lvl, 0));
		gridData.add(TestingGridData.get(1, lvl, 1));
		gridData.add(TestingGridData.get(1, lvl, 2));
		gridData.add(TestingGridData.get(2, lvl, 0));
		gridData.add(TestingGridData.get(2, lvl, 1));
		gridData.add(TestingGridData.get(2, lvl, 2));
		
		long[] data = process(0);

		assertEquals(3, data.length);
		assertEquals(WdbIndex.UNDEFINED_GID, data[0]);
		assertEquals(WdbIndex.UNDEFINED_GID, data[1]);
		assertEquals(WdbIndex.UNDEFINED_GID, data[2]);
	}


	@Test
	public void separatesSeveralTypesOfLevels() throws IndexCreationException {
		Level lvlA = new Level("height", "m");
		Level lvlB = new Level("whatever", "m");
		gridData.add(TestingGridData.get("pressure", "pa", lvlA, 0));
		gridData.add(TestingGridData.get("temperature", "C", lvlB, 1));
		gridData.add(TestingGridData.get("temperature", "C", lvlB, 0));
		
		long[] data = process("pressure");
		assertEquals(1, data.length);
		assertEquals(1, data[0]);
	}


	@Test
	public void throwOnManyLevelTypesForSameParameterInData() throws IndexCreationException {
		gridData.add(TestingGridData.get(new Level("level a", "m"), 1));
		gridData.add(TestingGridData.get(new Level("level b", "ft"), 3));
		try {
			process();
			fail("expected DuplicateDataException");
		}
		catch ( DuplicateDataException e) {
		}
	}	


	@Test
	public void onlyOneLevelWhenAllEntriesHaveOneLevel() throws IndexCreationException {
		Level lvl = new Level("height", "m");
		gridData.add(TestingGridData.get("temperature", "c", lvl, 0));
		gridData.add(TestingGridData.get("temperature", "c", lvl, 2));
		gridData.add(TestingGridData.get("temperature", "c", lvl, 10));
		gridData.add(TestingGridData.get("wind speed", "m/s", lvl, 10));
		
		long[] data = process("wind speed");

		assertEquals(1, data.length);
		assertEquals(4, data[0]);
	}
	
	
	@Test
	public void onlyOneMissingLevelWhenAllEntriesHaveOneLevel() throws IndexCreationException {
		Level lvl = new Level("height", "m");
		gridData.add(TestingGridData.get(0, "temeperature", "c", lvl, 2));
		gridData.add(TestingGridData.get(0, "temeperature", "c", lvl, 10));
		gridData.add(TestingGridData.get(1, "temeperature", "c", lvl, 2));
		gridData.add(TestingGridData.get(1, "temeperature", "c", lvl, 10));
		gridData.add(TestingGridData.get(2, "temeperature", "c", lvl, 2));
		gridData.add(TestingGridData.get(2, "temeperature", "c", lvl, 10));
		// gridData.add(TestingGridData.get(0, "wind speed", "m/s", lvl, 10)); // missing
		gridData.add(TestingGridData.get(1, "wind speed", "m/s", lvl, 10));
		gridData.add(TestingGridData.get(2, "wind speed", "m/s", lvl, 10));
		
		long[] data = process("wind speed", 0);
		assertEquals(1, data.length);
		assertEquals(WdbIndex.UNDEFINED_GID, data[0]);
	}


	@Test
	public void multipleVersions() throws IndexCreationException {
		gridData.add(TestingGridData.get(0, 0));
		gridData.add(TestingGridData.get(0, 1));
		long[] data = process();
	
		assertEquals(2, data.length);
		assertEquals(1, data[0]);
		assertEquals(2, data[1]);
	}


	@Test
	public void missingVersions() throws IndexCreationException {
		gridData.add(TestingGridData.get(0, 0));
		gridData.add(TestingGridData.get(0, 1));
		gridData.add(TestingGridData.get(1, 0));
		
		long[] data = process(1);

		assertEquals(2, data.length);
		assertEquals(3, data[0]);
		assertEquals(WdbIndex.UNDEFINED_GID, data[1]);
	}
	

	@Test
	public void missingTimeStepWithManyVersions() throws IndexCreationException {
		gridData.add(TestingGridData.get(0, 0, "temperature", "C"));
		gridData.add(TestingGridData.get(1, 0, "temperature", "C"));
		gridData.add(TestingGridData.get(2, 0, "temperature", "C"));
		gridData.add(TestingGridData.get(0, 0, "pressure", "pa"));
		gridData.add(TestingGridData.get(0, 1, "pressure", "pa"));
		gridData.add(TestingGridData.get(1, 0, "pressure", "pa"));
		
		long[] data = process("pressure", 2);
		
		assertEquals(2, data.length);
		assertEquals(WdbIndex.UNDEFINED_GID, data[0]);
		assertEquals(WdbIndex.UNDEFINED_GID, data[1]);
	}
}
