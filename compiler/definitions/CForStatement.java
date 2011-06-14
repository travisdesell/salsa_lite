package salsa_lite.compiler.definitions;

import java.util.Vector;

public class CForStatement extends CStatement {

	public CLocalVariableDeclaration init;
	public Vector<CExpression> expression_inits = new Vector<CExpression>();

	public CExpression conditional;

	public Vector<CExpression> increment_expressions = new Vector<CExpression>();

	public CStatement statement;

	public String toJavaCode() {
        if (init.isToken()) {
                CompilerErrors.printErrorMessage("Cannot have tokens within a for loop's initializer.", init);
        }

		String code = "for (";

		if (init != null) {
			code += init.toJavaCode();
		} else {
			code += ";";
		}

        if (conditional.isToken()) {
                CompilerErrors.printErrorMessage("Cannot have tokens within a for loop's conditional.", conditional);
        }

		code += " " + conditional.toJavaCode() + "; ";

        for (CExpression expression : increment_expressions) {
            if (expression.isToken()) {
                CompilerErrors.printErrorMessage("Cannot have tokens within a for loop's increment expressions.", expression);
            }
        }

		for (int i = 0; i < increment_expressions.size(); i++) {
			code += increment_expressions.get(i).toJavaCode();
			
			if (i != increment_expressions.size() - 1) code += ", ";
		}
		code += ") " + statement.toJavaCode();

		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
