package salsa_lite.compiler.definitions;

public class CSuffix extends CErrorInformation {
	public boolean is_increment = false;
	public boolean is_decrement = false;

	public String toJavaCode() {
		if (is_increment) return "++";
		if (is_decrement) return "--";
		return "";
	}

	public String toSalsaCode() {
		return "";
	}
}
