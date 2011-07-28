package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.ArrayType;
import salsa_lite.compiler.symbol_table.TypeSymbol;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;

import java.util.Vector;

public class CExpression extends CVariableInit {

	public CValue value;
	public String operator;
	public CExpression operator_expression;

	public Vector<String> prefixes;
	public Vector<Object> suffixes;

	public static TypeSymbol[] getArgumentTypes(Vector<CExpression> arguments) {
		TypeSymbol[] argumentTypes = new TypeSymbol[arguments.size()];

		for (int i = 0; i < argumentTypes.length; i++) argumentTypes[i] = arguments.get(i).getType();

		return argumentTypes;
	}

    public boolean containsMessageSends() {
        if (operator_expression != null) {
            return value.containsMessageSends() || operator_expression.containsMessageSends();
        } else {
            return value.containsMessageSends();
        }
    }

	public TypeSymbol getType() {
		TypeSymbol value_type = value.getType();

		if (operator == null) {
			return value_type;
		} else if (operator.equals("=")) {
			if (operator_expression.getType().canMatch(value_type) < 0) {
                CompilerErrors.printErrorMessage("Conflicting types.  Cannot assign '" + operator_expression.getType().getLongSignature() + "' to '" + value_type.getLongSignature() + "'", operator_expression);
			}

            if (!value.isToken() && operator_expression.isToken()) {
                CompilerErrors.printErrorMessage("Cannot assign a token to a non-token: '" + expression.getType().getLongSignature() + "'.", operator_expression);
            }

			return value_type;
		} else {
            try {
//                System.err.println();
//                System.err.println("getting dominating type for value: '" + value.toJavaCode() + "'");
//                System.err.println("getting dominating type for expression: '" + operator_expression.toJavaCode() + "'");

    			TypeSymbol dominatingType = SymbolTable.getDominatingType(value_type, operator_expression.getType());

//                System.err.println("dominating type: " + dominatingType.getLongSignature());
                return dominatingType;
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("[CExpression.getType]: " + snfe.toString(), this);
                throw new RuntimeException();
            }
		}
	}

	public boolean isToken() {
		if (operator_expression != null) return value.isToken() || operator_expression.isToken();
		else return value.isToken();
	}

	String implicit_token_code = null;
	public String getExpressionDirectorCode() {
		if (requiresExpressionDirector()) {
			CExpressionDirector ced = new CExpressionDirector(this);

			String code = ced.getDirectorsCode();
			implicit_token_code = ced.getMessageCode();

			return code + CIndent.getIndent();
		} else {
			return "";
		}
	}

	public boolean requiresExpressionDirector() {
		if (value.isToken()) {
			if (operator != null) return true;
			if (prefixes.size() > 0) return true;
			if (suffixes.size() > 0) return true;
		} else if (operator_expression != null && operator_expression.isToken()) {
			return true;
		}

		return false;
	}

    //This expression is the case of a case statement
    public String toJavaCode(TypeSymbol switchType) {
//        System.err.println("calling toJavaCode on CValue from CExpression, switchType: " + switchType.getLongSignature());
//        if (isToken()) {
//            CompilerErrors.printErrorMessage("[CExpression.toJavaCode] case expression currently cannot be a token.", this);
//            throw new RuntimeException();
//        }

   		String code = "";

		for (int i = 0; i < prefixes.size(); i++) code += prefixes.get(i);
		
		code += value.toJavaCode(switchType);
		
		for (int i = 0; i < suffixes.size(); i++) {
			code += suffixes.get(i);
		}

		if (operator != null) {
			code += " " + operator + " ";
			code += operator_expression.toJavaCode(switchType);
		}

		return code;
    }

	public String toJavaCode() {
		if (implicit_token_code != null) {
			return implicit_token_code;
		}

		String code = "";

		for (int i = 0; i < prefixes.size(); i++) code += prefixes.get(i);
		
		code += value.toJavaCode();
		
		for (int i = 0; i < suffixes.size(); i++) {
			code += suffixes.get(i);
		}

		if (operator != null) {
			code += " " + operator + " ";

            boolean isExpressionContinuation = SymbolTable.isExpressionContinuation;
            boolean continuationTokenMessage = SymbolTable.continuationTokenMessage;

//            System.err.println("java code of expression, value.getType(): " + value.getType());

            if (value.getType().getLongSignature().equals("ack")) {
//                System.err.println("continuation token message is true!");
                SymbolTable.continuationTokenMessage = true;
            }
            if (operator.equals("=")) SymbolTable.isExpressionContinuation = true;

			code += operator_expression.toJavaCode();

//            System.err.println("code is: " + code);

            SymbolTable.continuationTokenMessage = continuationTokenMessage;
            if (operator.equals("=")) SymbolTable.isExpressionContinuation = isExpressionContinuation;
		}

		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
