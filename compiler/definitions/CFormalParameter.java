package salsa_lite.compiler.definitions;


public class CFormalParameter extends CErrorInformation {

	public String type;
	public String name;

	public String toJavaCode() {
		return type + " " + name;
	}
}
