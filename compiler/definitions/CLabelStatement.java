package salsa_lite.compiler.definitions;

public class CLabelStatement extends CStatement {

	public String label;

	public String toJavaCode() {
		return label + ":";
	}

	public String toSalsaCode() {
		return label + ":";
	}
}
