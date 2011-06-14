package salsa_lite.compiler.definitions;

import java.util.Vector;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.TypeSymbol;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;

public class CAllocation extends CVariableInit {

	public boolean remote_reference = false;

	public String type;

	public int array_dimensions = 0;

	public Vector<CExpression> arguments;

	public Vector<CExpression> array_arguments;
	public CArrayInit array_init;

	public CExpression first_expression, second_expression, third_expression;

	public boolean isToken() {
		if (arguments != null) {
			for (int i = 0; i < arguments.size(); i++) {
				if (arguments.get(i).isToken()) return true;
			}
		} else if (array_arguments != null) {
			for (int i = 0; i < array_arguments.size(); i++) {
				if (array_arguments.get(i).isToken()) return true;
			}
		} else if (array_init != null) {
			return array_init.isToken();
		}

		if (first_expression != null && first_expression.isToken()) return true;
		if (second_expression != null && second_expression.isToken()) return true;
		if (third_expression != null && third_expression.isToken()) return true;

		return false;
	}

	public TypeSymbol getType() {
        try {
            TypeSymbol t = SymbolTable.getTypeSymbol(type);
            return t;
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("[CAllication.getType]: " + snfe.toString(), this);
            throw new RuntimeException(snfe);
        }

	}

	public String toJavaCode() {
		String code = "";

        try {
            if (arguments != null) {
                if (SymbolTable.isActor(type)) {
                    code += type +".construct(" + SymbolTable.getTypeSymbol(type).getConstructor(arguments).getId() + ", ";

                    String argument_code = "";
                    for (int i = 0; i < arguments.size(); i++) {
                        argument_code += arguments.get(i).toJavaCode();
                        if (i < arguments.size() - 1) argument_code += ", ";
                    }

                    if (arguments.size() == 0) {
                        code += "null";
                    } else {
                        code += "new Object[]{" + argument_code + "}";
                    }

                    if (first_expression != null) {
                        if (System.getProperty("local_fcs") != null) {
        //					if (first_expression.getType().equals("int") || first_expression.getType().equals("Integer")) {
                                code += ", StageService.getStage(" + first_expression.toJavaCode() + ")";
        //					}
                        } else {
                            code += ", " + first_expression.toJavaCode();

                            if (second_expression != null) {
                                code += ", " + second_expression.toJavaCode() + ", " + third_expression.toJavaCode();
                            }
                        }
           
                    } else {
        //				if (System.getProperty("local_fcs") != null) {
        //					code += ", StageService.getStage()";
        //				}
                    }
                    
                    code += ")";
                } else {
                    code += "new " + type + "( ";
                    for (CExpression argument : arguments) {
                        code += argument.toJavaCode();

                        if (!argument.equals(arguments.lastElement())) code += ", ";
                    }
                    code += " )";
                }
            } else {
                code += "new " + type;

                for (int i = 0; i < array_dimensions; i++) {
                    code += "[";
                    if (array_arguments != null && array_arguments.get(i) != null) code += array_arguments.get(i).toJavaCode();
                    code += "]";
                }

                if (array_init != null) code += array_init.toJavaCode();
            }
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("[CAllication.getType]: " + snfe.toString(), this);
            throw new RuntimeException(snfe);
        }

		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
