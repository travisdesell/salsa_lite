package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;

import java.util.Vector;

public class CExpressionDirector {

	String expression_director_name;
	String arguments_code, expression_code, required_messages_code, token_position_code;

	Vector<String> input_parameter_names = new Vector<String>();
	Vector<String> input_parameter_types = new Vector<String>();


	public CExpressionDirector(CExpression expression) {
		CExpression current_expression = expression;
		CValue current_value;

		expression_code = "";
		required_messages_code = "";
		arguments_code = "";
		token_position_code = "";

		int argument_number = 0;
		while (current_expression != null) {
			current_value = current_expression.value;

			if (current_value.isToken()) {
				expression_code += "(" + current_value.getType().toNonPrimitiveString() + ")arguments[" + argument_number + "]"; 
				
				if (!token_position_code.equals("")) token_position_code += ", ";
				token_position_code += argument_number;

				argument_number++;

				if (!arguments_code.equals("")) arguments_code += ", ";
				boolean previousWithin = SymbolTable.withinArguments;
				SymbolTable.withinArguments = true;
				boolean previousImplicit = SymbolTable.implicitMessage;
				SymbolTable.implicitMessage = true;
				arguments_code += current_value.toJavaCode();
				SymbolTable.withinArguments = previousWithin;
				SymbolTable.implicitMessage = previousImplicit;


			} else if (current_value.isLiteral()) {
				expression_code += current_value.toJavaCode();

			} else {
				expression_code += "(" + current_value.getType().toNonPrimitiveString() + ")arguments[" + argument_number + "]";
				argument_number++;

				if (!arguments_code.equals("")) arguments_code += ", ";
				arguments_code += current_value.toJavaCode();
			}

			if (current_expression.operator != null) expression_code += " " + current_expression.operator + " ";

			current_expression = current_expression.operator_expression;
		}
	}

	public String getExpressionCode() {
		return expression_code;
	}

	public String getDirectorsCode() {
		expression_director_name = SymbolTable.getExpressionDirectorName();

		String code = "";
		code += "class " + expression_director_name + " extends "; 
        code += "Actor";

		code += " {\n";
        code += CIndent.getIndent() + "\tpublic " + expression_director_name + "(SynchronousMailboxStage stage) { super(stage); }\n";
        code += CIndent.getIndent() + "\tpublic void invokeConstructor(int id, Object[] arguments) {}\n";

		code += CIndent.getIndent() + "\tpublic Object invokeMessage(int messageId, Object[] arguments) {\n";
		for (int i = 0; i < input_parameter_types.size(); i++) {
			code += CIndent.getIndent() + "\t\t" + input_parameter_types.get(i) + " " + input_parameter_names.get(i) + " = (" + input_parameter_types.get(i) + ")arguments[" + i + "];\n";
		}
		code += CIndent.getIndent() + "\t\treturn " + getExpressionCode() + ";\n";
		code += CIndent.getIndent() + "\t}\n";
		code += CIndent.getIndent() + "}\n";

		code += required_messages_code;

		return code;
	}

	public String getMessageCode() {
		String code = "";
		if (SymbolTable.continuesToPass && !SymbolTable.withinArguments) {
			code += "StageService.sendPassMessage(new " + expression_director_name + "(";
		} else {
			code += "StageService.sendImplicitTokenMessage(new " + expression_director_name + "(";
		}

        code += "this.stage";
		code += "), 0, new Object[]{" + arguments_code + "}, new int[]{" + token_position_code + "}";

		if (SymbolTable.continuesToPass && !SymbolTable.withinArguments) {
            code += ", this.stage.message.continuationDirector";
		}
		
		code += ")";

		return code;
	}
}
