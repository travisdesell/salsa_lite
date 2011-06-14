package salsa_lite.compiler.definitions;


public class CWhileStatement extends CStatement {

	public CExpression conditional;
	public CStatement block;

	public String toJavaCode() {
        if (conditional.isToken()) {
            CompilerErrors.printErrorMessage("Cannot have tokens within a while loop's conditional.", conditional);
        }

        String code = "while (" + conditional.toJavaCode() + ") " + block.toJavaCode();
		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
