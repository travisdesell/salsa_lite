package salsa_lite.compiler.definitions;

import java.util.Vector;


public class CEnumeration {

	public String name;
	public Vector<String> fields = new Vector<String>();

	public String toJavaCode() {
		String code = "enum " + name + "{";
		for (int i = 0; i < fields.size(); i++) {
			code += fields.get(i);
			if (i < fields.size() - 1) code += ", ";
		}
		return code + "}";
	}

	public String toSalsaCode() {
		return "";
	}
}
