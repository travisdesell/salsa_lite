package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.TypeSymbol;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;
import salsa_lite.compiler.symbol_table.VariableDeclarationException;

import java.util.LinkedList;
import java.util.Vector;

public class CSynchronizedStatement extends CStatement {

    public CExpression expression;
    public CBlock block;

    public String toJavaCode() {
        if (expression.isToken()) {
            CompilerErrors.printErrorMessage("Error in synchronized block. Cannot use a token as an argument to a synchronized block.", expression);
        }
        String code = "synchronized (" + expression.toJavaCode() + ")" + block.toJavaCode();
        return code;
    }
}
