package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.TypeSymbol;


public class CPassStatement extends CStatement {

	public CExpression expression;
	public boolean will_pass_later = false;
	public boolean continuedFromToken = false;

	public String toJavaCode() {
		if (will_pass_later) {
			String code = "throw new TokenPassException();";
			SymbolTable.token_pass_exception = true;
			return code;
		}

		if (expression != null && expression.isToken()) {
			SymbolTable.continuesToPass = true;
			SymbolTable.withinArguments = false;
			String code = expression.getExpressionDirectorCode();
			code += expression.toJavaCode() + ";\n";
			code += CIndent.getIndent() + "throw new TokenPassException();";
			SymbolTable.continuesToPass = false;
			SymbolTable.withinArguments = false;

			SymbolTable.token_pass_exception = true;
			return code;

		} else if (continuedFromToken) {
			String code = "throw new TokenPassException();";
			SymbolTable.token_pass_exception = true;
			return code;

		} else {
			String code = "return";
			if (expression != null) {
                TypeSymbol expressionType = expression.getType();
                if (SymbolTable.isMutableObject( expressionType )) {
                    code += " SalsaSystem.deepCopy( " + expression.toJavaCode() + " )";
                } else {
                    code += " " + expression.toJavaCode();
                }
            }
			return code + ";";
		}
	}

	public String toSalsaCode() {
		return "";
	}
}
