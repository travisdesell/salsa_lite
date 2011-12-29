package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.TypeSymbol;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;
import salsa_lite.compiler.symbol_table.VariableDeclarationException;

import java.util.LinkedList;
import java.util.Vector;

public class CTryStatement extends CStatement {

    public CBlock try_block;
    public LinkedList<CBlock> catch_blocks = new LinkedList<CBlock>();
    public LinkedList<Vector<CFormalParameter>> catch_parameters = new LinkedList<Vector<CFormalParameter>>();

    public String toJavaCode() {
        String code = "try " + try_block.toJavaCode();

        int i = 0;
        for (CBlock block : catch_blocks) {
            code += CIndent.getIndent() + "catch (";

            SymbolTable.openScope();
            for (int j = 0; j < catch_parameters.get(i).size(); j++) {
                CFormalParameter parameter = catch_parameters.get(i).get(j);

                try {
                    SymbolTable.addVariableType(parameter.name, parameter.type.name, false, false);
                } catch (VariableDeclarationException vde) {
                    CompilerErrors.printErrorMessage("[CMessageHandler.toJavaCode] Could not declare variable. " + vde.toString(), parameter);
                    throw new RuntimeException(vde);
                } catch (SalsaNotFoundException snfe) {
                    CompilerErrors.printErrorMessage("[CMessageHandler.toJavaCode] Could not find parameter type. " + snfe.toString(), parameter);
                    throw new RuntimeException(snfe);
                }

                code += parameter.toJavaCode();

                if (j != catch_parameters.get(i).size() - 1) code += ", ";
            }

            code += ") " + block.toJavaCode();
            i++;

            SymbolTable.closeScope();
        }

        return code;
    }
}
