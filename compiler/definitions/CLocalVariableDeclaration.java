package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;

import java.util.Vector;

public class CLocalVariableDeclaration extends CStatement {
	public boolean is_token, is_block;
	public String type;

	public Vector<CVariableInit> variables = new Vector<CVariableInit>();

    public boolean isToken() {
        return is_token; 
    }

	public String toJavaCode() {
		SymbolTable.messageContinues = continues;

		String code = "";
		if (is_token) {
//			code = "TokenDirector " + variables.get(i).toJavaCode();
			for (int i = 0; i < variables.size(); i++) {
                /**
                 *  Need to implement tokens
                 */

				code += "TokenDirector ";
				code += variables.get(i).toJavaCodeAsToken();

                try {
    				SymbolTable.addVariableType(variables.get(i).name, type, true, false);
                } catch (SalsaNotFoundException snfe) {
                    CompilerErrors.printErrorMessage("[CLocalVariableDeclaration.toJavaCode] Could not find parameter type. " + snfe.toString(), variables.get(i));
                    throw new RuntimeException(snfe);
                }

				code += ";";
				
				if (i < variables.size() - 1) code += "\n" + CIndent.getIndent();
			}
		} else {
			for (int i = 0; i < variables.size(); i++) {
				code += type + " ";
				code += variables.get(i).toJavaCode();

                try {
				    SymbolTable.addVariableType(variables.get(i).name, type, false, false);
                } catch (SalsaNotFoundException snfe) {
                    CompilerErrors.printErrorMessage("[CLocalVariableDeclaration.toJavaCode] Could not find parameter type. " + snfe.toString(), variables.get(i));
                    throw new RuntimeException(snfe);
                }

				code += ";";
				
				if (i < variables.size() - 1) code += "\n" + CIndent.getIndent();
			}
		}

        SymbolTable.messageRequiresContinuation = continues;

		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
