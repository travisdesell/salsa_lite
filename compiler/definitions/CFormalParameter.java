package salsa_lite.compiler.definitions;


public class CFormalParameter extends CErrorInformation {

	public CType type;
	public String name;

	public String toJavaCode() {
		return type.name + " " + name;
	}
}
