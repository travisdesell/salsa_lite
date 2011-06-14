package salsa_lite.compiler.definitions;

import java.util.Vector;

public class CWaitforMessage extends CStatement {

	public Vector<CExpression> conditions = new Vector<CExpression>();
	public CStatement statement;

	public String toJavaCode() {
		return "";
	}

	public String toSalsaCode() {
		return "";
	}
}
