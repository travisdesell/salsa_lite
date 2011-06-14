package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.SimpleNode;

import java.util.Vector;

public class CMessageHandler extends CErrorInformation {

	public String pass_type;
	public String name;

	public Vector<CFormalParameter> parameters;

	public CBlock block;
	
	public void addContainedMessageHandlers(Vector<CMessageHandler> containedMessageHandlers) {
		block.addContainedMessageHandlers(containedMessageHandlers);
	}

	public String[] getArgumentTypes() {
		String[] argument_types = new String[parameters.size()];
		for (int i = 0; i < argument_types.length; i++) argument_types[i] = parameters.get(i).type;
		return argument_types;
	}

	public String getCaseInvocation() {
		String code = name + "(";
		for (int i = 0; i < parameters.size(); i++) {
			CFormalParameter parameter = parameters.get(i);
            try {
    			code += "(" + SymbolTable.getTypeSymbol(parameter.type).toNonPrimitiveString() + ")arguments[" + i +"]";
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("[CMessageHandler.getCaseInvocation] Could not find parameter type. " + snfe.toString(), parameter);
                throw new RuntimeException(snfe);
            }

			if (i != parameters.size() - 1) code += ", ";
		}
		code += ");";

		if (pass_type.equals("ack")) {
			code += " return null;";
		} else {
            code = "return " + code;
		}
		return code;
	}

	public String toJavaCode() {
		SymbolTable.currentMessageName = name;

		String definition_code = CIndent.getIndent() + "public ";
		if (pass_type.equals("ack")) definition_code += "void";
		else definition_code += pass_type;
		definition_code += " " + name + "(";

		SymbolTable.newMessageHandler();

		SymbolTable.openScope();

		for (int i = 0; i < parameters.size(); i++) {
			CFormalParameter p = parameters.get(i);
			definition_code += p.toJavaCode();

            try {
    			SymbolTable.addVariableType(p.name, p.type, false, false);
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("[CMessageHandler.toJavaCode] Could not find parameter type. " + snfe.toString(), p);
                throw new RuntimeException(snfe);
            }

			if (i != parameters.size() - 1) definition_code += ", ";
		}
		definition_code += ") ";

		String block_code = block.toJavaCode() + "\n";

		if (SymbolTable.token_pass_exception && SymbolTable.continuation_pass_exception) definition_code += "throws TokenPassException, ContinuationPassException ";
		else if (SymbolTable.token_pass_exception) definition_code += "throws TokenPassException ";
		else if (SymbolTable.continuation_pass_exception) definition_code += "throws ContinuationPassException ";

		SymbolTable.token_pass_exception = false;
		SymbolTable.continuation_pass_exception = false;

		SymbolTable.closeScope();

		return definition_code + block_code;
	}   

	public String toSalsaCode() {
		return ""; 
	}
}
