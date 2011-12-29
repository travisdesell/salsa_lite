package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;
import salsa_lite.compiler.symbol_table.VariableDeclarationException;

import java.util.Vector;

public class CLocalVariableDeclaration extends CStatement {
	public boolean is_token, is_block;
	public CType type;

	public Vector<CVariableInit> variables = new Vector<CVariableInit>();

    public boolean isToken() {
        return is_token; 
    }

	public String toJavaCode() {
		SymbolTable.messageContinues = continues;

		String code = "";
		if (is_token) {
			for (int i = 0; i < variables.size(); i++) {
                if (type.name.equals("ack")) {
    				code += "ContinuationDirector ";
				    code += variables.get(i).toJavaCodeAsToken(type.name, false, true);
                } else {
				    code += "TokenDirector ";
				    code += variables.get(i).toJavaCodeAsToken(type.name, true, false);
                }

                try {
    				SymbolTable.addVariableType(variables.get(i).name, type.name, true, false);
                } catch (VariableDeclarationException vde) {
                    CompilerErrors.printErrorMessage("[CLocalVariableDeclaration.toJavaCode] Could not declare variable. " + vde.toString(), variables.get(i));
                    throw new RuntimeException(vde);
                } catch (SalsaNotFoundException snfe) {
                    CompilerErrors.printErrorMessage("[CLocalVariableDeclaration.toJavaCode] Could not find parameter type. " + snfe.toString(), variables.get(i));
                    throw new RuntimeException(snfe);
                }

				code += ";";
				
				if (i < variables.size() - 1) code += "\n" + CIndent.getIndent();
			}
		} else {
			for (int i = 0; i < variables.size(); i++) {
				code += type.name + " ";
				code += variables.get(i).toJavaCode(type.name, is_token);

                try {
				    SymbolTable.addVariableType(variables.get(i).name, type.name, false, false);
                } catch (VariableDeclarationException vde) {
                    CompilerErrors.printErrorMessage("[CLocalVariableDeclaration.toJavaCode] Could not declare variable. " + vde.toString(), variables.get(i));
                    throw new RuntimeException(vde);
                } catch (SalsaNotFoundException snfe) {
                    CompilerErrors.printErrorMessage("[CLocalVariableDeclaration.toJavaCode] Could not find parameter type. " + snfe.toString(), variables.get(i));
                    throw new RuntimeException(snfe);
                }
				code += ";";

                if (variables.get(i).isToken()) {
                    CompilerErrors.printErrorMessage("Assigning a a non-token variable to a token.", variables.get(i));
                }
				
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
