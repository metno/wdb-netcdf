package no.met.wdb;
/*
    wdb_iosp

    Copyright (C) 2011 met.no

    Contact information:
    Norwegian Meteorological Institute
    Box 43 Blindern
    0313 OSLO
    NORWAY
    E-mail: post@met.no

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
    MA  02110-1301, USA
*/



import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.DataInput;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WdbConfiguration {

	private String database = null;
	private String host = null;
	private int port = -1;
	private String user = null;

	
	
	public WdbConfiguration(String configFile)  throws InvalidWdbConfigurationFileException, IOException, FileNotFoundException {
		this(new RandomAccessFile(configFile, "r"));
	}

	
	
	public WdbConfiguration(DataInput configurationFile) throws InvalidWdbConfigurationFileException, IOException {
		
		Pattern p = Pattern.compile("(database|host|port|user)\\s*=\\s*(.*)");

		for ( String s = configurationFile.readLine(); s != null; s = configurationFile.readLine() ) {
			int commentMarkerIndex = s.indexOf('#');
			if ( commentMarkerIndex >= 0 )
				s = s.substring(0, commentMarkerIndex);
			s = s.trim();
			if ( s.isEmpty() ) 
				continue;
			
			Matcher m = p.matcher(s);
			if ( ! m.matches() )
				throw new InvalidWdbConfigurationFileException("Invalid line in file: " + s);
			
			String key = m.group(1);
			String val = m.group(2);
			
			if ( key.equals("database") )
				database = val;
			else if ( key.equals("host") )
				host = val;
			else if ( key.equals("port") ) {
				try {
					port = Integer.parseInt(val);
				}
				catch (NumberFormatException e) {
					throw new InvalidWdbConfigurationFileException("Unable to parse port number: " + s);
				}
			}
			else if ( key.equals("user") )
				user = val;
			else
				throw new InvalidWdbConfigurationFileException("Unexpected value in configuration file: " + s);
		}
	}

	
	
	public String jdbcConnectionString() {
		
		StringBuilder ret = new StringBuilder();
		ret.append("jdbc:postgresql://");
		ret.append(getHost());
		if ( port != -1 ) {
			ret.append(":");
			ret.append(getPort());
		}
		ret.append("/");
		ret.append(getDatabase());
		
		return ret.toString();
	}
	
	
	
	public String getDatabase() {
		if ( database == null )
			return "wdb";
		return database;
	}

	
	
	public String getHost() {
		if ( host == null )
			return "localhost";
		return host;
	}

	
	
	public int getPort() {
		if ( port == -1 )
			return 5432; // default postgresql port
		return port;
	}

	
	
	public String getUser() {
		return user;
	}
}
