package salsa_lite.compiler.definitions;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.SimpleNode;

import java.util.Vector;

public class CInterfaceDeclaration {
	public CName interface_name;
	public CName extends_name;

	public Vector<CLocalVariableDeclaration> variable_declarations = new Vector<CLocalVariableDeclaration>();
	public Vector<CMessageHandler> message_handlers = new Vector<CMessageHandler>();

	public String toJavaCode() {
		return "";
	}

	public String toSalsaCode() {
		return "";
	}
}
