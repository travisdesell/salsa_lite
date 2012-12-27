package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.TypeSymbol;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;
import salsa_lite.compiler.symbol_table.VariableDeclarationException;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.SimpleNode;

import java.util.Vector;

public class CMessageHandler extends CErrorInformation {

    public boolean is_abstract = false;

	public CType pass_type;
	public String name;

	public Vector<CFormalParameter> parameters;

	public CBlock block;

    public CType getArgument(int i) {
        return parameters.get(i).type;
    }
	
	public void addContainedMessageHandlers(Vector<CMessageHandler> containedMessageHandlers) {
        if (block != null) block.addContainedMessageHandlers(containedMessageHandlers);
	}

	public String[] getArgumentTypes() {
		String[] argument_types = new String[parameters.size()];
		for (int i = 0; i < argument_types.length; i++) argument_types[i] = parameters.get(i).type.name;
		return argument_types;
	}

    public String toString() {
        String[] argumentTypes = getArgumentTypes();

        String s = pass_type.name + " " + name + "(";
        for (int i = 0; i < argumentTypes.length; i++) {
            s += argumentTypes[i];
            if (i != argumentTypes.length - 1) s += ", ";
        }

        s += ")";

        return s;
    }

	public String toJavaCode() {
		SymbolTable.currentMessageName = name;

		String definition_code = "";
        
        if (is_abstract) {
            definition_code += CIndent.getIndent() + "abstract public ";
        } else {
            definition_code += CIndent.getIndent() + "public ";
        }

        if (pass_type.name.equals("void")) {
            CompilerErrors.printErrorMessage("[CMessageHandler.toJavaCode] 'void' is not a valid pass type for a message. Use 'ack' instead.", pass_type);
        }
		if (pass_type.name.equals("ack")) definition_code += "void";
		else definition_code += pass_type.name;
		definition_code += " " + name + "(";

        try {
            SymbolTable.currentPassType = SymbolTable.getTypeSymbol(pass_type.name);
        } catch (VariableDeclarationException vde) {
            CompilerErrors.printErrorMessage("[CValue.getValueType]: Could not declare variable. " + vde.toString(), this);
            throw new RuntimeException(vde);
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("[CValue.getValueType]: Could not determine type for variable. " + snfe.toString(), this);
            throw new RuntimeException(snfe);
        }

		SymbolTable.newMessageHandler();

		SymbolTable.openScope();

		for (int i = 0; i < parameters.size(); i++) {
			CFormalParameter p = parameters.get(i);
			definition_code += p.toJavaCode();

            try {
    			SymbolTable.addVariableType(p.name, p.type.name, false, false);
            } catch (VariableDeclarationException vde) {
                CompilerErrors.printErrorMessage("[CMessageHandler.toJavaCode] Could not declare variable. " + vde.toString(), p);
                throw new RuntimeException(vde);
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("[CMessageHandler.toJavaCode] Could not find parameter type. " + snfe.toString(), p);
                throw new RuntimeException(snfe);
            }

			if (i != parameters.size() - 1) definition_code += ", ";
		}
		definition_code += ")";

		String block_code = ";\n";
        if (block != null) {
            definition_code += " ";
            block_code = block.toJavaCode() + "\n";
        }

		if (SymbolTable.token_pass_exception && SymbolTable.continuation_pass_exception) definition_code += "throws TokenPassException";
//		if (SymbolTable.token_pass_exception && SymbolTable.continuation_pass_exception) definition_code += "throws TokenPassException, ContinuationPassException ";
		else if (SymbolTable.token_pass_exception) definition_code += "throws TokenPassException ";
//		else if (SymbolTable.continuation_pass_exception) definition_code += "throws ContinuationPassException ";

		SymbolTable.token_pass_exception = false;
		SymbolTable.continuation_pass_exception = false;

		SymbolTable.closeScope();

		return definition_code + block_code;
	}   

	public String toSalsaCode() {
		return ""; 
	}
}
