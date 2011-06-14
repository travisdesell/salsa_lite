package salsa_lite.compiler.definitions;

import java.util.Vector;

public class CArrayInit extends CVariableInit {

	public Vector<CVariableInit> inits = new Vector<CVariableInit>();

	public boolean isToken() {
		for (int i = 0; i < inits.size(); i++) {
			if (inits.get(i).isToken()) return true;
		}
		return false;
	}

	public String toJavaCode() {
		String code = "{";

		for (int i = 0; i < inits.size(); i++) {
			code += inits.get(i).toJavaCode();
			if (i < inits.size() - 1) code += ", ";
		}

		return code + "}";
	}

	public String toSalsaCode() {
		return "";
	}
}
