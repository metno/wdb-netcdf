package no.met.wdb.store;

public interface NameTranslator {

	/**
	 * Translate a wdb parameter name (value- or level-) into a another name, 
	 * such as a cf standard name.
	 * 
	 * cf standard name. If no explicit translations are given in configuration - make
	 * a guess.
	 */
	public String translate(String wdbName);
}