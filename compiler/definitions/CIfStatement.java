package salsa_lite.compiler.definitions;

import java.util.Vector;



public class CIfStatement extends CStatement {

	public CExpression expression;
	public CStatement statement;
	public CStatement else_statement;

	public void addContainedMessageHandlers(Vector<CMessageHandler> containedMessageHandlers) {
		statement.addContainedMessageHandlers(containedMessageHandlers);

		if (else_statement != null) else_statement.addContainedMessageHandlers(containedMessageHandlers);
	}

	public String toJavaCode() {
        if (expression.isToken()) {
            CompilerErrors.printErrorMessage("Cannot use tokens within an if statement conditional.", expression);
        }

		String code = "if (" + expression.toJavaCode() + ") ";
		if (statement instanceof CBlock) code += statement.toJavaCode() + CIndent.getIndent();
		else {
			code += "{\n";
			CIndent.increaseIndent();
			code += CIndent.getIndent() + statement.toJavaCode(); 
			if (statement instanceof CStatementExpression) code += ";";
			code += "\n";
			CIndent.decreaseIndent();
			code += CIndent.getIndent() + "} ";
		}

		if (else_statement != null) {
			code += "else ";
			if (else_statement instanceof CBlock || else_statement instanceof CIfStatement) code += else_statement.toJavaCode();
			else {
				code += "{\n";
				CIndent.increaseIndent();
				code += CIndent.getIndent() + else_statement.toJavaCode();
				if (else_statement instanceof CStatementExpression) code += ";";
				code += "\n";
				CIndent.decreaseIndent();
				code += CIndent.getIndent() + "}";
			}
		}

		return code;
	}

	public String toSalsaCode() {
		return "";
	}

}
