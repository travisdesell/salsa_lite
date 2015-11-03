package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.FieldSymbol;
import salsa_lite.compiler.symbol_table.ObjectType;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;
import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.TypeSymbol;
import salsa_lite.compiler.symbol_table.VariableDeclarationException;

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

        if (switchType instanceof ObjectType) {
            ObjectType ot = (ObjectType)switchType;
            //System.err.println("switching an enum:");
            for (FieldSymbol fs : ot.fields) {
                //System.err.println("\t" + fs.getName());
                try {
                    SymbolTable.addVariableType(fs.getName(), fs.getType().getLongSignature(), false, true);
                } catch (VariableDeclarationException vde) {
                    CompilerErrors.printErrorMessage("Error declaring enum type in switch statement: " + vde.toString());
                    throw new RuntimeException(vde);
                } catch (SalsaNotFoundException snfe) {
                    CompilerErrors.printErrorMessage("Error getting type of enum in switch statement: " + snfe.toString());
                    throw new RuntimeException(snfe);
                }
            }
        }

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
