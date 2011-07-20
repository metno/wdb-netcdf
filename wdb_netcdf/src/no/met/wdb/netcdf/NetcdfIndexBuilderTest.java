package no.met.wdb.netcdf;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import no.met.wdb.GridData;
import no.met.wdb.Level;
import no.met.wdb.store.IndexCreationException;
import no.met.wdb.test.TestingGridData;

import org.junit.Before;
import org.junit.Test;

import ucar.nc2.Dimension;
import ucar.nc2.Variable;


public class NetcdfIndexBuilderTest {

	private TestingNetcdfFile ncfile;
	private NetcdfIndexBuilder indexBuilder;
	private Vector<GridData> gridData = new Vector<GridData>();

	private Wdb2NetcdfNameTranslator translator = new Wdb2NetcdfNameTranslator();
	
	@Before
	public void setUp() throws Exception {
		ncfile = new TestingNetcdfFile();
		indexBuilder = new NetcdfIndexBuilder(new GlobalWdbConfiguration("etc/wdb_config.xml"));
	}
	
	@Test
	public void dimensions() throws IndexCreationException {

		gridData.add(TestingGridData.get());
		indexBuilder.populate(gridData, ncfile);
		
		List<Dimension> dims = ncfile.getDimensions();
		assertEquals(3, dims.size());

		HashSet<String> dimensionNames = new HashSet<String>();
		for ( Dimension d : dims )
			dimensionNames.add(d.getName());
		
		
		assertTrue(dimensionNames.contains("time"));
		// fix these later:
		assertTrue(dimensionNames.contains("x"));
		assertTrue(dimensionNames.contains("y"));
	}

	@Test
	public void variables() throws IndexCreationException {
		gridData.add(TestingGridData.get());
		indexBuilder.populate(gridData, ncfile);

		Variable timeVariable = ncfile.findTopVariable("time");
		assertTrue(timeVariable != null);
		List<Dimension> timeDimensions = timeVariable.getDimensions();
		assertEquals(1, timeDimensions.size());
		assertEquals("time", timeDimensions.get(0).getName());
		
		Variable param = ncfile.findTopVariable(translator.fromWdb(TestingGridData.defaultValueParameter));
		assertTrue(param != null);
		List<Dimension> paramDimensions = param.getDimensions();
		assertEquals(2, paramDimensions.size());
		// x and y
	}
		
	@Test
	public void levels() throws IndexCreationException {
		Level lvl = new Level("lvl", "m");
		gridData.add(TestingGridData.get(lvl, 0));
		gridData.add(TestingGridData.get(lvl, 1));
		indexBuilder.populate(gridData, ncfile);

		String cdlLevel = translator.fromWdb("lvl");
		
		List<Dimension> dims = ncfile.getDimensions();
		Dimension lvlDimension = null;
		for ( Dimension find: dims ) {
			if ( find.getName() == cdlLevel ) {
				lvlDimension = find;
				break;
			}
		}
		assertFalse(lvlDimension == null);
		assertEquals(2, lvlDimension.getLength());
		
		Variable lvlVariable = ncfile.findTopVariable(cdlLevel);
		assertFalse(lvlVariable == null);
		List<Dimension> lvlDimensions = lvlVariable.getDimensions();
		assertEquals(1, lvlDimensions.size());
		assertEquals(cdlLevel, lvlDimensions.get(0).getName());
		
		Variable param = ncfile.findTopVariable(translator.fromWdb(TestingGridData.defaultValueParameter));
		assertFalse(param == null);
		List<Dimension> paramDimensions = param.getDimensions();
		assertEquals(3, paramDimensions.size());
		assertEquals("lvl", paramDimensions.get(0).getName());
	}
	
	@Test
	public void multipleParameters() throws IndexCreationException {
		gridData.add(TestingGridData.get("air temperature", "C"));
		gridData.add(TestingGridData.get("air pressure", "pa"));
		gridData.add(TestingGridData.get("precipitation", "mm"));
		indexBuilder.populate(gridData, ncfile);

		Variable param = ncfile.findTopVariable(translator.fromWdb("air temperature"));
		assertTrue(param != null);
		List<Dimension> paramDimensions = param.getDimensions();
		assertEquals(2, paramDimensions.size());

		param = ncfile.findTopVariable(translator.fromWdb("air pressure"));
		assertTrue(param != null);
		paramDimensions = param.getDimensions();
		assertEquals(2, paramDimensions.size());

		param = ncfile.findTopVariable(translator.fromWdb("precipitation"));
		assertTrue(param != null);
		paramDimensions = param.getDimensions();
		assertEquals(2, paramDimensions.size());
	}
	
	@Test
	public void multipleTimeSteps() throws IndexCreationException {
		gridData.add(TestingGridData.get(0));
		gridData.add(TestingGridData.get(2));
		gridData.add(TestingGridData.get(1));
		indexBuilder.populate(gridData, ncfile);

		Variable param = ncfile.findTopVariable(translator.fromWdb(TestingGridData.defaultValueParameter));
		assertTrue(param != null);
		List<Dimension> paramDimensions = param.getDimensions();
		assertEquals(3, paramDimensions.size());
		assertEquals("time", paramDimensions.get(0).getName());
	}
	
	@Test
	public void oneParameterMissingTimeStep() throws IndexCreationException {
		gridData.add(TestingGridData.get(0, "air temperature", "C"));
		gridData.add(TestingGridData.get(1, "air temperature", "C"));
		gridData.add(TestingGridData.get(2, "air temperature", "C"));
		gridData.add(TestingGridData.get(0, "air pressure", "pa"));
		//gridData.add(TestingGridData.get(1, "air pressure", "pa"));
		gridData.add(TestingGridData.get(2, "air pressure", "pa"));
		indexBuilder.populate(gridData, ncfile);
		
		Variable param = ncfile.findTopVariable(translator.fromWdb("air temperature"));
		assertTrue(param != null);
		List<Dimension> paramDimensions = param.getDimensions();
		assertEquals(3, paramDimensions.size());
		assertEquals("time", paramDimensions.get(0).getName());

		param = ncfile.findTopVariable(translator.fromWdb("air pressure"));
		assertTrue(param != null);
		paramDimensions = param.getDimensions();
		assertEquals(3, paramDimensions.size());
		assertEquals("time", paramDimensions.get(0).getName());

	}
}
