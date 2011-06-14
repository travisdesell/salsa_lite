package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.TypeSymbol;

import java.util.Vector;

public class CSwitchStatement extends CStatement {
	public CExpression expression;
	public Vector<CStatement> statements = new Vector<CStatement>();

	public void addContainedMessageHandlers(Vector<CMessageHandler> containedMessageHandlers) {
		for (CStatement statement : statements) {
			statement.addContainedMessageHandlers(containedMessageHandlers);
		}
	}

	public String toJavaCode() {
		String code = "switch (" + expression.toJavaCode() + ") {\n";

        TypeSymbol switchType = expression.getType();

		CIndent.increaseIndent();
		for (int i = 0; i < statements.size(); i++) {
            CStatement statement = statements.get(i);

            if (statement instanceof CCaseStatement) {
                CCaseStatement case_statement = (CCaseStatement)statement;
			    code += CIndent.getIndent() + case_statement.toJavaCode(switchType) + "\n";
            } else {
			    code += CIndent.getIndent() + statement.toJavaCode() + "\n";
            }
		}
		CIndent.decreaseIndent();

		code += CIndent.getIndent() + "}\n";

		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
