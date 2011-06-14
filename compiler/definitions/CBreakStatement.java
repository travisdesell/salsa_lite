package salsa_lite.compiler.definitions;

public class CBreakStatement extends CStatement {

	public String toJavaCode() {
		return "break;";
	}

	public String toSalsaCode() {
		return "break;";
	}
}
