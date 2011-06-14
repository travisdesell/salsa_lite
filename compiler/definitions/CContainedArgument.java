package salsa_lite.compiler.definitions;


public class CContainedArgument {

	String type;
	String name;
	CExpression expression;

	public CContainedArgument(String type, String name, CExpression expression) {
		this.type = type;
		this.name = name;
		this.expression = expression;
	}

	public String toJavaCode() {
		return "";
	}

	public String toSalsaCode() {
		return "";
	}
}
