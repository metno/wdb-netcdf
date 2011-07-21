package no.met.wdb;


/**
 * Thrown if a database connection specification is unusable
 */
public class InvalidDatabaseConnectionSpecificationException extends Exception {
	
	private static final long serialVersionUID = -8744912489889639756L;

	public InvalidDatabaseConnectionSpecificationException(String msg) {
		super(msg);
	}

}
