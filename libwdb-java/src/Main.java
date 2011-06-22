

import java.util.Vector;

import no.met.wdb.GridData;
import no.met.wdb.ReadQuery;
import no.met.wdb.WdbConfiguration;
import no.met.wdb.WdbConnection;


public class Main {
	
	static void useWdbConnection() throws Exception
	{
		WdbConfiguration config = new WdbConfiguration("/disk1/wdb/etc/wdb.conf");
		System.out.println(config.jdbcConnectionString());
		
		WdbConnection connection = new WdbConnection(config);
		
		Vector<String> dataProvider = new Vector<String>();
		dataProvider.add("met.no eceps modification");
		
		String location = null;
		
		String referenceTime = "2011-02-14 00:00:00Z";
		
		String validTime = null;
		
		Vector<String> parameter = new Vector<String>();
		parameter.add("air pressure at sea level");
		
		String level = null;
		
		Vector<Integer> dataVersion = null;
		
		ReadQuery query = new ReadQuery(dataProvider, location, referenceTime, validTime, parameter, level, dataVersion);
		Iterable<GridData> data = connection.readGid(query);
		
		for (GridData d : data ) {
			System.out.println(d.toString());
		}
		connection.close();
	}
	
	public static void main(String[] args) throws Exception {
		useWdbConnection();
	}
	
}