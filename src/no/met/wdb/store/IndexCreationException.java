package no.met.wdb.store;

public class IndexCreationException extends Exception {
	
	private static final long serialVersionUID = 7925913249108290301L;

	public IndexCreationException(String reason) {
		super(reason);
	}
}
