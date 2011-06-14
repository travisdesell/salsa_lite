package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;

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

	public String toJavaCodeAsToken() {
		String code = name;
		if (expression != null) {
            if (expression.isToken()) {
                code += " = " + expression.toJavaCode();
            } else {
    			code += " = TokenDirector.construct(1, new Object[]{" + expression.toJavaCode() + "})";
            }
		}
		return code;
	}


	public String toJavaCode() {
		String code = name;
		if (expression != null) {
			code += " = " + expression.toJavaCode();
		}
		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
