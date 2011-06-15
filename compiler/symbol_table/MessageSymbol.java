package salsa_lite.compiler.symbol_table;

import salsa_lite.compiler.definitions.CompilerErrors;
import salsa_lite.compiler.definitions.CMessageHandler;


public class MessageSymbol extends Invokable {

    TypeSymbol passType;
    public TypeSymbol getPassType()     { return passType; }

    public MessageSymbol(int id, TypeSymbol enclosingType, CMessageHandler messageHandler) {
        super(id, messageHandler.name, enclosingType);

        try {
            this.passType = SymbolTable.getTypeSymbol( messageHandler.pass_type.name );
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("Could not find pass type for message handler.", messageHandler.pass_type);
            throw new RuntimeException(snfe);
        }

        String[] argumentTypes = messageHandler.getArgumentTypes();
        parameterTypes = new TypeSymbol[argumentTypes.length];

        int i = 0;
        for (String s : argumentTypes) {
            try {
                parameterTypes[i++] = SymbolTable.getTypeSymbol( s );
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("Could not find argument type for message handler.", messageHandler.getArgument(i - 1));
                throw new RuntimeException(snfe);
            }
        }
    }
}
