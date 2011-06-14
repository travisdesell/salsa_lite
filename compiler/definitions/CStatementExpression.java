package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;


public class CStatementExpression extends CStatement {

	public CExpression expression;

	public String toJavaCode() {
		SymbolTable.messageContinues = continues;

		String code = expression.toJavaCode();

        SymbolTable.messageRequiresContinuation = continues;

		if (continues && !expression.isToken()) {
			if (next_statement != null && next_statement instanceof CPassStatement) {
				((CPassStatement)next_statement).continuedFromToken = false;
			}
		}

		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
