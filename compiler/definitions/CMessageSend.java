package salsa_lite.compiler.definitions;

import java.util.Vector;

public class CMessageSend extends CSuffix implements CModification {

	public String message_name;

	public Vector<CExpression> arguments = new Vector<CExpression>();

	public CMessageProperty message_property;

	public String toJavaCode() {
		return "";
	}

	public String toSalsaCode() {
		return "";
	}
}
