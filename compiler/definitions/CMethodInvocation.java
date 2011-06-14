package salsa_lite.compiler.definitions;

import java.util.Vector;

public class CMethodInvocation extends CErrorInformation implements CModification {

	public String method_name;

	public Vector<CExpression> arguments = new Vector<CExpression>();

	public String toJavaCode() {
		return "";
	}

	public String toSalsaCode() {
		return "";
	}
}
