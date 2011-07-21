package no.met.wdb.store;


public class InvalidRequestException extends IndexCreationException {

	private static final long serialVersionUID = 2817674210548851501L;

	public InvalidRequestException(String reason) {
		super(reason);
	}

}
