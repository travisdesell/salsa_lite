package salsa_lite.compiler.definitions;


public class CIndent {
	static int indent = 0;

	public static void increaseIndent() { indent++; }
	public static void decreaseIndent() { indent--; }

	public static String getIndent() {
		String code = "";
		for (int i = 0; i < indent; i++) code += "\t";
		return code;
	}
}
