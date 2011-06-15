package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;
import salsa_lite.compiler.symbol_table.TypeSymbol;

public class CVariableInit extends CErrorInformation {
	public String name;
	public CExpression expression;

	public boolean isToken() {
		if (expression != null && expression.isToken()) return true;
        try {
    		if (name != null && SymbolTable.isToken(name)) return true;
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("[CVariableInit.isToken]: " + snfe.toString(), this);
            throw new RuntimeException(snfe);
        }
		return false;
	}

	public String toJavaCodeAsToken(String type, boolean is_token) {
		String code = name;

        TypeSymbol ts = null;
        try {
            ts = SymbolTable.getTypeSymbol(type);
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("Could not determine type '" + type + "' for variable initialization.", this);
            throw new RuntimeException(snfe);
        }

		if (expression != null) {
            if (ts.canMatch(expression.getType()) < 0) {
                CompilerErrors.printErrorMessage("Conflicting types.  Cannot assign '" + expression.getType().getLongSignature() + "' to '" + ts.getLongSignature() + "'", expression);
            }

            if (is_token && expression.isToken()) {
                CompilerErrors.printErrorMessage("Cannot assign a token to a non-token.", expression);
            }

            if (expression.isToken()) {
                code += " = " + expression.toJavaCode();
            } else {
    			code += " = TokenDirector.construct(1, new Object[]{" + expression.toJavaCode() + "})";
            }
		}
		return code;
	}


	public String toJavaCode(String type, boolean is_token) {
		String code = name;

        TypeSymbol ts = null;
        try {
            ts = SymbolTable.getTypeSymbol(type);
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("Could not determine type '" + type + "' for variable initialization.", this);
            throw new RuntimeException(snfe);
        }

		if (expression != null) {
            if (ts.canMatch(expression.getType()) < 0) {
                CompilerErrors.printErrorMessage("Conflicting types.  Cannot assign '" + expression.getType().getLongSignature() + "' to '" + ts.getLongSignature() + "'", expression);
            }

            if (is_token && expression.isToken()) {
                CompilerErrors.printErrorMessage("Cannot assign a token to a non-token.", expression);
            }

			code += " = " + expression.toJavaCode();
		}
		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
