package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.TypeSymbol;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;
import salsa_lite.compiler.symbol_table.VariableDeclarationException;

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

        try {
            TypeSymbol ack_type = SymbolTable.getTypeSymbol("ack");

            if (SymbolTable.currentPassType.equals( ack_type ) && expression != null && expression.getType().canMatch(ack_type) < 0) {
                System.err.println("canMatch: " + expression.getType().canMatch(ack_type));
                CompilerErrors.printErrorMessage("Message handler has return type 'ack' and is trying to pass a '" + expression.getType().toString() + "' value.", this);
            } else if (expression == null && SymbolTable.currentPassType.canMatch( ack_type ) < 0) {
                CompilerErrors.printErrorMessage("Message handler has return type '" + SymbolTable.currentPassType.toString() + "' and is not passing a value.", this);
            } else if (expression != null && !(expression.getType().canMatch(SymbolTable.currentPassType) >= 0)) {
                CompilerErrors.printErrorMessage("Current message handler has return type '" + SymbolTable.currentPassType.toString() + "', which does not match pass statements type '" + expression.getType().toString() + "'.", this);
            }
        } catch (VariableDeclarationException vde) {
            CompilerErrors.printErrorMessage("[CPassStatement.toJavaCode]: Could not declare variable. " + vde.toString(), this);
            throw new RuntimeException(vde);
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("[CPassStatement.toJavaCode]: Could not determine type for variable. " + snfe.toString(), this);
            throw new RuntimeException(snfe);
        }

		if (expression != null && expression.isToken()) {
            if (expression.containsMessageSends()) {
                SymbolTable.continuesToPass = true;
                SymbolTable.withinArguments = false;
                String code = expression.getExpressionDirectorCode();
                code += expression.toJavaCode() + ";\n";
                code += CIndent.getIndent() + "throw new TokenPassException();";
                SymbolTable.continuesToPass = false;
                SymbolTable.withinArguments = false;

                SymbolTable.token_pass_exception = true;
                return code;
            } else {
                String code = "StageService.passToken(" + expression.toJavaCode() + ", this.getStage().message.continuationDirector);\n";
                code += CIndent.getIndent() + "throw new TokenPassException();";
                SymbolTable.token_pass_exception = true;
                return code;
            }

		} else if (continuedFromToken) {
            String code;
            if (expression == null) {
//                code = "StageService.sendMessage(this.getStage().message.continuationDirector, 2 /*resolve*/, null, continuation_token);\n";
			    code = "throw new TokenPassException();";
            } else {
                code = "StageService.sendMessage(this.getStage().message.continuationDirector, 2 /*setValue*/, new Object[]{" + expression.toJavaCode() + "}, continuation_token);\n";
			    code += CIndent.getIndent() + "throw new TokenPassException();";
            }
			SymbolTable.token_pass_exception = true;
			return code;

		} else {
			String code = "return";
			if (expression != null) {
                TypeSymbol expressionType = expression.getType();
                if (SymbolTable.isMutableObject( expressionType )) {
                    code += " (" + expressionType.getName() + ")DeepCopy.deepCopy( " + expression.toJavaCode() + " )";
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
