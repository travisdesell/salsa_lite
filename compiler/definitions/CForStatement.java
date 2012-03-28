package salsa_lite.compiler.definitions;

import java.util.Vector;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;
import salsa_lite.compiler.symbol_table.VariableDeclarationException;


public class CForStatement extends CStatement {

    public CExtendedFor extended_for;

	public CLocalVariableDeclaration init;
	public Vector<CExpression> expression_inits = new Vector<CExpression>();

	public CExpression conditional;

	public Vector<CExpression> increment_expressions = new Vector<CExpression>();

	public CStatement statement;

	public String toJavaCode() {
        String code = "for (";

        if (extended_for != null) {
            code += extended_for.type.name + " ";
            code += extended_for.name + " : ";

            try {
                SymbolTable.addVariableType(extended_for.name, extended_for.type.name, false, false);
            } catch (VariableDeclarationException vde) {
                CompilerErrors.printErrorMessage("[CForStatement.toJavaCode] Could not declare variable. " + vde.toString() , extended_for);
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("[CForStatement.toJavaCode] Could not fine parameter type. " + snfe.toString() , extended_for);
            }

            if (extended_for.expression.isToken()) {
                CompilerErrors.printErrorMessage("Cannot use a token within an extended for statement.", extended_for);
            } else {
                code += extended_for.expression.toJavaCode();
            }

            code += ")";
            SymbolTable.openScope();

        } else {
            if (init.isToken()) {
                    CompilerErrors.printErrorMessage("Cannot have tokens within a for loop's initializer.", init);
            }

            SymbolTable.openScope();

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
            code += ") ";
        }
        
        code += statement.toJavaCode();
        if (!(statement instanceof CBlock)) code += ";";

        SymbolTable.closeScope();

		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
