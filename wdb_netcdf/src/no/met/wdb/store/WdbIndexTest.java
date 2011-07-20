package no.met.wdb.store;

class WdbIndexTest {

	/*
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
		WdbIndex index = new WdbIndex(gridData);
		return index.getData(valueParameter, timeIndex,1,0,1,0,1,0,1);
	}
	
	
	@Before
	public void setUp() {
		TestingGridData.resetGidCounter();
	}
	
	
	@Test
	public void simpleCase() throws IndexCreationException  {
		gridData.add(TestingGridData.get());
		Iterator<Long> data = process().iterator();
		
		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(1, data.next().longValue());
		assertFalse(data.hasNext());
	}

	
	@Test
	public void requestFirstTimeEntry() throws IndexCreationException {
		// we insert data with no order to time 
		gridData.add(TestingGridData.get(2));
		gridData.add(TestingGridData.get(1));
		gridData.add(TestingGridData.get(0));

		Iterator<Long> data = process(0).iterator();

		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(3, data.next().longValue());
		assertFalse(data.hasNext());
	}

	
	@Test
	public void requestSecondTimeEntry() throws IndexCreationException {
		// we insert data with no order to time 
		gridData.add(TestingGridData.get(2));
		gridData.add(TestingGridData.get(0));
		gridData.add(TestingGridData.get(1));

		Iterator<Long> data = process(1).iterator();

		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(3, data.next().longValue());
		assertFalse(data.hasNext());
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
			fail("Expected InvalidRequestException exception");
		}
		catch ( IllegalArgumentException success ) {
		}
	}

	@Test
	public void throwOnRequestForNonexistingParameter() throws IndexCreationException {

		gridData.add(TestingGridData.get("pressure", "pa"));
		try {
			process("temperature");
			fail("Expected InvalidRequestException exception");
		}
		catch ( IllegalArgumentException success ) {
		}
	}

	
	@Test
	public void selectSingleParameter() throws IndexCreationException  {
		gridData.add(TestingGridData.get("temperature", "C"));
		gridData.add(TestingGridData.get("pressure", "pa"));
		
		WdbIndex index = new WdbIndex(gridData);
		
		Iterator<Long> data = index.getData("temperature", 0,1,0,1,0,1,0,1).iterator();
		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(1, data.next().longValue());
		assertFalse(data.hasNext());

		data = index.getData("pressure", 0,1,0,1,0,1,0,1).iterator();
		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(2, data.next().longValue());
		assertFalse(data.hasNext());
	}

	
	@Test
	public void selectParameterWithMissingTimeEntry() throws IndexCreationException {
		gridData.add(TestingGridData.get(0, "air temperature", "C"));
		gridData.add(TestingGridData.get(1, "air temperature", "C"));
		gridData.add(TestingGridData.get(2, "air temperature", "C"));
		gridData.add(TestingGridData.get(0, "air pressure", "pa"));
		//gridData.add(TestingGridData.get(1, "air pressure", "pa"));
		gridData.add(TestingGridData.get(2, "air pressure", "pa"));
		
		Iterator<Long> missing = process("air pressure", 1).iterator();
		assertTrue(missing != null);
		assertTrue(missing.hasNext());
		assertEquals(null, missing.next());
		assertFalse(missing.hasNext());
	}

	
	@Test
	public void parameterWithSingleTime() throws IndexCreationException {
		gridData.add(TestingGridData.get(0, "temperature", "c"));
		gridData.add(TestingGridData.get(1, "temperature", "c"));
		gridData.add(TestingGridData.get(2, "temperature", "c"));
		gridData.add(TestingGridData.get(1, "terrain height", "m"));
		Iterator<Long> data = process("terrain height", 0).iterator();
		assertTrue(data != null);
		assertTrue(data.hasNext());
		Long value = data.next();
		assertFalse(value == null);
		assertEquals(4, value.longValue());
		assertFalse(data.hasNext());
	}

	
	@Test
	public void getLevels() throws IndexCreationException  {
		Level lvl = new Level("lvl", "m");
		gridData.add(TestingGridData.get(lvl, 0));
		gridData.add(TestingGridData.get(lvl, 1));
		gridData.add(TestingGridData.get(lvl, 2));
		Iterator<Long> data = process().iterator();
		
		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(1, data.next().longValue());
		assertTrue(data.hasNext());
		assertEquals(2, data.next().longValue());
		assertTrue(data.hasNext());
		assertEquals(3, data.next().longValue());
		assertFalse(data.hasNext());
	}
	
	
	@Test
	public void twoLevelsComingInWrongOrder() throws IndexCreationException  {
		Level lvl = new Level("lvl", "m");
		gridData.add(TestingGridData.get(lvl, 1));
		gridData.add(TestingGridData.get(lvl, 0));
		Iterator<Long> data = process().iterator();
		
		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(2, data.next().longValue());
		assertTrue(data.hasNext());
		assertEquals(1, data.next().longValue());
		assertFalse(data.hasNext());
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
		Iterator<Long> data = process(0).iterator();

		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(1, data.next().longValue());
		assertTrue(data.hasNext());
		assertEquals(null, data.next());
		assertTrue(data.hasNext());
		assertEquals(2, data.next().longValue());
		assertFalse(data.hasNext());
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
		
		Iterator<Long> data = process(0).iterator();

		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(null, data.next());
		assertTrue(data.hasNext());
		assertEquals(null, data.next());
		assertTrue(data.hasNext());
		assertEquals(null, data.next());
		assertFalse(data.hasNext());
	}

	
	@Test
	public void separatesSeveralTypesOfLevels() throws IndexCreationException {
		Level lvlA = new Level("height", "m");
		Level lvlB = new Level("whatever", "m");
		gridData.add(TestingGridData.get("pressure", "pa", lvlA, 0));
		gridData.add(TestingGridData.get("temperature", "C", lvlB, 1));
		gridData.add(TestingGridData.get("temperature", "C", lvlB, 0));
		
		Iterator<Long> data = process("pressure").iterator();
		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(1, data.next().longValue());
		assertFalse(data.hasNext());
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
		
		Iterator<Long> data = process("wind speed").iterator();

		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(4, data.next().longValue());
		assertFalse(data.hasNext());

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
		
		Iterator<Long> data = process("wind speed", 0).iterator();
		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(null, data.next());
		assertFalse(data.hasNext());
	}


	@Test
	public void multipleVersions() throws IndexCreationException {
		gridData.add(TestingGridData.get(0, 0));
		gridData.add(TestingGridData.get(0, 1));
		Iterator<Long> data = process().iterator();
		
		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(1, data.next().longValue());
		assertTrue(data.hasNext());
		assertEquals(2, data.next().longValue());
		assertFalse(data.hasNext());
		
	}

	
	@Test
	public void missingVersions() throws IndexCreationException {
		gridData.add(TestingGridData.get(0, 0));
		gridData.add(TestingGridData.get(0, 1));
		gridData.add(TestingGridData.get(1, 0));
		
		Iterator<Long> data = process(1).iterator();

		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(3, data.next().longValue());
		assertTrue(data.hasNext());
		assertEquals(null, data.next());
		assertFalse(data.hasNext());
	}
	
	
	@Test
	public void missingTimeStepWithManyVersions() throws IndexCreationException {
		gridData.add(TestingGridData.get(0, 0, "temperature", "C"));
		gridData.add(TestingGridData.get(1, 0, "temperature", "C"));
		gridData.add(TestingGridData.get(2, 0, "temperature", "C"));
		gridData.add(TestingGridData.get(0, 0, "pressure", "pa"));
		gridData.add(TestingGridData.get(0, 1, "pressure", "pa"));
		gridData.add(TestingGridData.get(1, 0, "pressure", "pa"));
		
		Iterator<Long> data = process("pressure", 2).iterator();
		assertTrue(data != null);
		assertTrue(data.hasNext());
		assertEquals(null, data.next());
		assertTrue(data.hasNext());
		assertEquals(null, data.next());
		assertFalse(data.hasNext());
	}
*/
}
