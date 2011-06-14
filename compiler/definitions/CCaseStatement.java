package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.TypeSymbol;

public class CCaseStatement extends CStatement {

	public CExpression expression;

	public String toJavaCode(TypeSymbol switchType) {
		return "case " + expression.toJavaCode(switchType) + ":";
	}

    public String toJavaCode() {
        throw new RuntimeException("Cannot call toJavaCode on CCaseStatement without providing the switch type");
    }

	public String toSalsaCode() {
		return "case " + expression.toSalsaCode() + ":";
	}
}
