package salsa_lite.compiler.definitions;

import java.util.Vector;

public class CArrayAccess extends CErrorInformation implements CModification {
	public CExpression expression;

    public CArrayAccess(CExpression expression) {
        this.expression = expression;
    }

	public String toJavaCode() {
		String code = "";
        code += "[" + expression.toJavaCode() + "]";
		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
