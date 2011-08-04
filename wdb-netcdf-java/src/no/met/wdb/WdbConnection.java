package no.met.wdb;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * A connection to a wdb database. Contains methods corresponding to the 
 * various sql functions a wdb database have.
 */
public class WdbConnection {
	
	private static boolean driverInitialized = false;
	
	private Connection connection;
	
	public WdbConnection(DatabaseConnectionSpecification configuration) throws ClassNotFoundException, SQLException {
		this(configuration, true);
	}

	
	public WdbConnection(DatabaseConnectionSpecification configuration, boolean alsoCallWciBegin) throws ClassNotFoundException, SQLException {

		if ( ! driverInitialized ) {
			Class.forName("org.postgresql.Driver");
			driverInitialized = true;
		}
		connection = DriverManager.getConnection(configuration.jdbcConnectionString(), configuration.getUser(), null);
		connection.setAutoCommit(false);

		if ( alsoCallWciBegin ) {
			String wciUser = configuration.getUser();
			if ( wciUser == null )
				wciUser = "wdb";
			begin(wciUser);
		}
	}

	
	public void close() {
		try {
			connection.close();
		}
		catch ( SQLException e ) {
			// ignored
		}
	}
	
	void begin(String wciUser) throws SQLException {
		PreparedStatement st = connection.prepareStatement("SELECT * FROM wci.begin(?)");
		st.setString(1, wciUser);
//		for ( int i = 2; i < 5; i ++ )
//			st.setInt(i, 88);
		st.executeQuery();
	}

	private String quote(String s) {

		// FIXME: Verify this!

		if ( s == null )
			return "NULL";

		String quote = "'";
		String between = "";
		while ( s.contains(quote) ) {
			quote = '$' + between + '$';
			between = between + 'n';
		}
		
		return quote + s + quote;
	}
	

	public Iterable<GridData> readGid(ReadQuery query) throws SQLException {

		Statement st = connection.createStatement();
		st.setFetchSize(64);
		System.out.println(query.toString());
		ResultSet result = st.executeQuery(query.toString());

		Vector<GridData> ret = new Vector<GridData>();
		try {
			while ( result.next() ) {
				ret.add(new GridData(result));
			}
			
			for ( GridData d : ret )
				d.setGrid(getRegularGridDefinition(d));
		}
		finally {
			result.close();
			st.close();
		}
		return ret;
	}
	
	public Grid getGrid(long gridId) throws SQLException, IOException {
		
		Statement st = connection.createStatement();
		String query = Grid.query(gridId);
		//System.out.println(query);
		ResultSet result = st.executeQuery(query);

		if ( ! result.next() )
			throw new IOException("Unable to find grid in database");
		
		try {
			return new Grid(result);
		}
		finally {
			result.close();
			st.close();
		}
	}
	
	public Iterable<PlaceRegularGrid> getRegularGridDefinition(String gridName) throws SQLException {
		
		String query = "SELECT astext(placegeometry), placename, numberx, numbery, " +
				"incrementx, incrementy, startx, starty, projdefinition " +
				"FROM wci.getplaceregulargrid(" + quote(gridName) + ")";
		
		//System.out.println(query);
		
		Statement st = connection.createStatement();
		ResultSet result = st.executeQuery(query);

		Vector<PlaceRegularGrid> ret = new Vector<PlaceRegularGrid>();
		try {
			while ( result.next() ) {
				ret.add(new PlaceRegularGrid(result));
			}
		}
		finally {
			result.close();
			st.close();
		}
		
		return ret;

	}
	

	private HashMap<String, PlaceRegularGrid> gridDefinitionCache = new HashMap<String, PlaceRegularGrid>();
	
	public PlaceRegularGrid getRegularGridDefinition(GridData data) throws SQLException {
		
		String placeName = data.getPlaceName();
		
		PlaceRegularGrid ret = gridDefinitionCache.get(placeName);
		if ( ret == null ) {
		
			Iterable<PlaceRegularGrid> gridList = getRegularGridDefinition(placeName);
			Iterator<PlaceRegularGrid> it = gridList.iterator();
			if ( ! it.hasNext() )
				throw new RuntimeException("Unable to find grid definition for data"); // should never happen
			
			ret = it.next();
			
			if ( it.hasNext() )
				throw new RuntimeException("Multiple grid definitions for data"); // should never happen
			
			gridDefinitionCache.put(placeName, ret);
		}
		
		return ret;
	}

}
