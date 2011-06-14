package salsa_lite.compiler.definitions;


public class CLiteral extends CErrorInformation {
	public String type;
	public String value;

	public String toJavaCode() {
		return value;
	}

	public String toSalsaCode() {
		return "";
	}
}
