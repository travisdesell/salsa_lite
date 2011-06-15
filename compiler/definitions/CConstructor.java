package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.TypeSymbol;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.SimpleNode;

import java.util.Vector;

public class CConstructor extends CErrorInformation {

	public String name;

	public Vector<CFormalParameter> parameters = new Vector<CFormalParameter>();
	public CBlock block;

	public void addContainedMessageHandlers(Vector<CMessageHandler> containedMessageHandlers) {
		block.addContainedMessageHandlers(containedMessageHandlers);
	}

    public CType getArgument(int i) {
        return parameters.get(i).type;
    }

	public String[] getArgumentTypes() {
		String[] argument_types = new String[parameters.size()];
		for (int i = 0; i < argument_types.length; i++) argument_types[i] = parameters.get(i).type.name;
		return argument_types;
	}   

    /*
	public String getCaseInvocation() {
		String code = "construct(";
		for (int i = 0; i < parameters.size(); i++) {
			CFormalParameter parameter = parameters.get(i);

            String parameter_type = "";
            try {
                TypeSymbol ts = SymbolTable.getTypeSymbol(parameter.type.name);
                System.err.println("getting type symbol: " + parameter.type.name + " got " + ts.getSignature());

                parameter_type = SymbolTable.getTypeSymbol(parameter.type.name).toNonPrimitiveString();
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("[CConstructor.getCaseInvocation]: Could not find parameter type. " + snfe.toString(), parameter);
                throw new RuntimeException(snfe);
            }

			code += "(" + parameter_type + ")arguments[" + i +"]";

			if (i != parameters.size() - 1) code += ", ";
		}
		code += ");";
		return code;
	}
*/

	public String toJavaCode() {
		//Check if name == class name

		SymbolTable.openScope();

		String code = CIndent.getIndent() + "public void construct(";
		for (int i = 0; i < parameters.size(); i++) {
			CFormalParameter p = parameters.get(i);
			code += p.toJavaCode();

            try {
			    SymbolTable.addVariableType(p.name, p.type.name, false, false);
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("[CConstructor.toJavaCode]: Could not find parameter type. " + snfe.toString(), p);
                throw new RuntimeException(snfe);
            }

			if (i != parameters.size() - 1) code += ", ";
		}
		code += ") ";
		code += block.toJavaCode() + "\n";

		SymbolTable.closeScope();
		return code; 
	}   

	public String toSalsaCode() {
		return ""; 
	}   
}
