package salsa_lite.compiler.definitions;

import java.util.Vector;

public class CVariableDeclaration extends CStatement {
	public Vector<CVariable> variables = new Vector<CVariable>();

	public String toJavaCode() {
		CVariable variable = variables.get(0);
		String code = variable.type + " ";
		for (int i = 0; i < variables.size(); i++) {
			variable = variables.get(i);
			code += variable.toJavaCode();
			if (i < variables.size() - 1) code += ", ";
		}


		return code;
	}
}
