package salsa_lite.compiler.symbol_table;

import salsa_lite.compiler.definitions.CMessageHandler;


public class MessageSymbol extends Invokable {

    TypeSymbol passType;
    public TypeSymbol getPassType()     { return passType; }

    public MessageSymbol(int id, TypeSymbol enclosingType, CMessageHandler messageHandler) throws SalsaNotFoundException {
        super(id, messageHandler.name, enclosingType);

        this.passType = SymbolTable.getTypeSymbol( messageHandler.pass_type );

        String[] argumentTypes = messageHandler.getArgumentTypes();
        parameterTypes = new TypeSymbol[argumentTypes.length];

        int i = 0;
        for (String s : argumentTypes) {
            parameterTypes[i++] = SymbolTable.getTypeSymbol( s );
        }
    }
}
