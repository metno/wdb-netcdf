package no.met.wdb.store;


public class DuplicateDataException extends IndexCreationException {

	private static final long serialVersionUID = 5253976999274387552L;

	public DuplicateDataException(String reason) {
		super(reason);
	}
}
