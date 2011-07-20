package no.met.wdb;

/**
 * A specification of anything measurable, such as temperature, of height 
 * above ground.
 */
public class Parameter implements Comparable<Parameter> {

	private String name;
	private String unit;
	
	public Parameter(String name, String unit) {
		this.name = name;
		this.unit = unit;
	}
	
	public String getName() {
		return name;
	}

	public String getUnit() {
		return unit;
	}

	@Override
	public int compareTo(Parameter other) {
		return name.compareTo(other.name);
	}
	
	@Override
	public String toString() {
		return name + "(" + unit + ")";
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	public boolean equals(String s) {
		return name.equals(s);
	}
	
	public boolean equals(Parameter p) {
		return name.equals(p.name);
	}
	
	@Override
	public boolean equals(Object obj) {

		try {
			Parameter p = (Parameter) obj;
			return name.equals(p.name);
		}
		catch ( ClassCastException e ) {
			try {
				
				String s = (String) obj;
				return name.equals(s);
			}
			catch ( ClassCastException e2 ) { }
		}
		return name.equals(obj);
	}
}
