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



import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.DataInput;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DatabaseConnectionSpecification {

	private String database = null;
	private String host = null;
	private int port = PORT_NOT_SPECIFIED;
	private String user = null;

	public static final int PORT_NOT_SPECIFIED = -1;
	

	public DatabaseConnectionSpecification(String database, String host, int port, String user) {
		this.database = database;
		this.host = host;
		this.port = port;
		this.user = user;
	}

	
	public DatabaseConnectionSpecification(File configFile)  throws InvalidDatabaseConnectionSpecificationException, IOException, FileNotFoundException {
		this(new RandomAccessFile(configFile, "r"));
	}

	public DatabaseConnectionSpecification(DataInput configurationFile) throws InvalidDatabaseConnectionSpecificationException, IOException {
		
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
				throw new InvalidDatabaseConnectionSpecificationException("Invalid line in file: " + s);
			
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
					throw new InvalidDatabaseConnectionSpecificationException("Unable to parse port number: " + s);
				}
			}
			else if ( key.equals("user") )
				user = val;
			else
				throw new InvalidDatabaseConnectionSpecificationException("Unexpected value in configuration file: " + s);
		}
	}

	
	
	public String jdbcConnectionString() {
		
		StringBuilder ret = new StringBuilder();
		ret.append("jdbc:postgresql://");
		ret.append(getHost());
		if ( port != PORT_NOT_SPECIFIED ) {
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
		if ( port == PORT_NOT_SPECIFIED )
			return 5432; // default postgresql port
		return port;
	}

	
	
	public String getUser() {
		return user;
	}
	
	@Override
	public String toString() {
		return "dbname=" + database + " host=" + host + " port=" + port + " user=" + user;
	}
	
	@Override
	public boolean equals(Object object) {

		DatabaseConnectionSpecification other = (DatabaseConnectionSpecification) object;
		if ( other == null )
			return false;
		
		return database.equals(other.database) &&
				host.equals(other.host) &&
				port == other.port &&
				user.equals(other.user);
	}
}
