package salsa_lite.compiler.definitions;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.SimpleNode;

import salsa_lite.compiler.symbol_table.SymbolTable;

import java.util.Vector;

public class CBehaviorDeclaration extends CErrorInformation {

	int added_message_handlers = 0;
	int added_constructors = 0;

	public CName behavior_name;
	public CName extends_name;
	public Vector<CName> implements_names = new Vector<CName>();

	public Vector<CEnumeration> enumerations = new Vector<CEnumeration>();
	public Vector<CConstructor> constructors = new Vector<CConstructor>();
	public Vector<CMessageHandler> message_handlers = new Vector<CMessageHandler>();
	public Vector<CLocalVariableDeclaration> variable_declarations = new Vector<CLocalVariableDeclaration>();

	public CJavaStatement java_statement;

	public CName getExtendsName() {
		if (this.extends_name == null) {
			if (System.getProperty("local_fcs") != null) {
                return new CName("salsa_lite.local_fcs.LocalActor");
            } else if (System.getProperty("wwc") != null) {
                return new CName("salsa_lite.wwc.WWCActor");
            } else {
                return new CName("");
            }
		} else {
			return extends_name;
		}
	}

	public String getImplementsNames() {
		if (implements_names.size() == 0) return null;

		String code = "";
		for (int i = 0; i < implements_names.size(); i++) {
			code += implements_names.get(i);
			if (i < implements_names.size() - 1) code += ", ";
		}
		return code;
	}

	public String getEnumerationCode() {
		String code = "";
		for (int i = 0; i < enumerations.size(); i++) {
			code += CIndent.getIndent() + enumerations.get(i).toJavaCode() + "\n";
		}
		return code;
	}

	public String getInvokeMessageCode() {
		String code = "";

		code += CIndent.getIndent() + "public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {\n";
		CIndent.increaseIndent();
		code += CIndent.getIndent() + "switch(messageId) {\n";
		CIndent.increaseIndent();

		for (int i = 0; i < message_handlers.size(); i++) {
			CMessageHandler mh = message_handlers.get(i);

			code += CIndent.getIndent() + "case " + i + ": " + mh.getCaseInvocation() + "\n";
//			SymbolTable.addMessageId(i, behavior_name, mh.name, mh.getArgumentTypes(), mh.pass_type);
		}

		code += CIndent.getIndent() + "default: throw new MessageHandlerNotFoundException(messageId, arguments);\n";
		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";
//		code += CIndent.getIndent() + "return null;\n";
		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";
		return code;
	}

	public String getInvokeConstructorCode() {
		String code = "";

		code += CIndent.getIndent() + "public void invokeConstructor(int constructorId, Object[] arguments) throws ConstructorNotFoundException {\n";
		CIndent.increaseIndent();
		code += CIndent.getIndent() + "switch(constructorId) {\n";
		CIndent.increaseIndent();

		for (int i = 0; i < constructors.size(); i++) {
			CConstructor con = constructors.get(i);
			code += CIndent.getIndent() + "case " + i + ": " + con.getCaseInvocation() + " break;\n";
//			SymbolTable.addConstructorId(i, behavior_name, con.getArgumentTypes());
		}

		code += CIndent.getIndent() + "default: throw new ConstructorNotFoundException(constructorId, arguments);\n";
		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";
		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";
		return code;
	}

	public int getActConstructor() {
		for (int i = 0; i < constructors.size(); i++) {
			CConstructor con = constructors.get(i);
			if (con.parameters.size() == 1 && con.parameters.get(0).type.equals("String[]")) return i;

		}
		return -1;
	}

	public String toJavaCode() {
/*		String code = "public class " + behavior_name;
		
		if (System.getProperty("local") != null) code += "State";
		if (System.getProperty("wwc") != null) code += "State";

//		SymbolTable.knownActors.add(behavior_name);

		extends_name = getExtendsName();
		if (System.getProperty("local") != null) extends_name += "State";
		if (System.getProperty("wwc") != null) extends_name += "State";

		code += " extends " + extends_name;

		String implements_names = getImplementsNames();
		if (implements_names != null) {
			code += " implements " + implements_names;
		}
		code += " {\n";

		CIndent.increaseIndent();

		SymbolTable.addVariableType("self", behavior_name, false, false);
		SymbolTable.addVariableType("parent", extends_name, false, false);

		if (System.getProperty("local") != null) {
			code += CIndent.getIndent() + behavior_name + "State(long identifier) { super(identifier); }\n";
		} else if (System.getProperty("wwc") != null) {
			code += CIndent.getIndent() + behavior_name + "State(String identifier) { super(identifier); }\n";
		}

		if (java_statement != null) code += CIndent.getIndent() + java_statement.toJavaCode();

		code += "\n";
		code += getEnumerationCode();

		code += "\n";
		code += getInvokeConstructorCode();

		code += "\n";
		code += getInvokeMessageCode();
		code += "\n";

		if (System.getProperty("local_noref") != null || System.getProperty("local_fcs") != null) {
			code += "\n";

			int act_constructor = getActConstructor();
			if (act_constructor >= 0) {
				code += CIndent.getIndent() + "public static void main(String[] arguments) {\n";
				code += CIndent.getIndent() + "\tconstruct(" + act_constructor + ", new Object[]{arguments});\n";
				code += CIndent.getIndent() + "}\n";
			}
		}

		for (int i = 0; i < variable_declarations.size(); i++) {
			for (int j = 0; j < variable_declarations.get(i).variables.size(); j++) {
				CLocalVariableDeclaration v = variable_declarations.get(i);
				CVariableInit vi = v.variables.get(j);

//				SymbolTable.addFieldType(behavior_name, vi.name, v.type);
//				SymbolTable.addVariableType(vi.name, v.type);
				code += CIndent.getIndent() + v.type + " " + vi.toJavaCode() + ";\n";
			}
		}
		code += "\n";

		if (System.getProperty("local_noref") != null || System.getProperty("local_fcs") != null) {
			code += CIndent.getIndent() + "public static " + behavior_name + " construct(int constructor_id, Object[] arguments) {\n";
			code += CIndent.getIndent() + "\t" + behavior_name + " actor = new " + behavior_name + "();\n";
			code += CIndent.getIndent() + "\tStageService.createActor(actor, constructor_id, arguments);\n";
//			code += CIndent.getIndent() + "\ttry {\n";
//			code += CIndent.getIndent() + "\t\tactor.invokeConstructor(constructor_id, arguments);\n";
//			code += CIndent.getIndent() + "\t} catch (ConstructorNotFoundException e) {\n";
//			code += CIndent.getIndent() + "\t\tSystem.out.println(\"Caught constructor exception: \" + e);\n";
//			code += CIndent.getIndent() + "\t\te.printStackTrace();\n";
//			code += CIndent.getIndent() + "\t}\n";
			code += CIndent.getIndent() + "\treturn actor;\n";
			code += CIndent.getIndent() + "}\n";
		}
		code += "\n";

		for (int i = 0; i < constructors.size(); i++) {
			CConstructor con = constructors.get(i);
			code += con.toJavaCode();
		}
		code += "\n";

		for (int i = 0; i < message_handlers.size(); i++) {
			CMessageHandler mh = message_handlers.get(i);
			code += mh.toJavaCode();
		}

		CIndent.decreaseIndent();

		code += "}\n";
*/
        String code = "";
		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
