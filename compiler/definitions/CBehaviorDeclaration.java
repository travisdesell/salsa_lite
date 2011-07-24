package salsa_lite.compiler.definitions;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.SimpleNode;

import salsa_lite.compiler.symbol_table.SymbolTable;

import java.util.Vector;

public class CBehaviorDeclaration extends CErrorInformation {

	int added_message_handlers = 0;
	int added_constructors = 0;

    public boolean is_abstract = false;

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
            return new CName("salsa_lite.runtime.Actor");
		} else {
			return extends_name;
		}
	}

	public String getImplementsNames() {
		if (implements_names.size() == 0) return null;

		String code = "";
		for (int i = 0; i < implements_names.size(); i++) {
			code += implements_names.get(i).name;
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

	public int getActConstructor() {
		for (int i = 0; i < constructors.size(); i++) {
			CConstructor con = constructors.get(i);
			if (con.parameters.size() == 1 && con.parameters.get(0).type.name.equals("String[]")) return i;

		}
		return -1;
	}

	public String toJavaCode() {
        String code = "";
		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
