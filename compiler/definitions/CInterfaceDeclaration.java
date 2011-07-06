package salsa_lite.compiler.definitions;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.SimpleNode;

import java.util.Vector;

public class CInterfaceDeclaration {
	public CName interface_name;
	public Vector<CName> extends_names = new Vector<CName>();

	public Vector<CLocalVariableDeclaration> variable_declarations = new Vector<CLocalVariableDeclaration>();
	public Vector<CMessageHandler> message_handlers = new Vector<CMessageHandler>();

    public String getImplementsNames() {
        if (extends_names.size() == 0) return null;

        String code = "";
        for (int i = 0; i < extends_names.size(); i++) {
            code += extends_names.get(i).name;
            if (i < extends_names.size() - 1) code += ", ";
        }
        return code;
    }

	public String toJavaCode() {
		return "";
	}

	public String toSalsaCode() {
		return "";
	}
}
