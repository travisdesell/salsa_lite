package salsa_lite.compiler.symbol_table;

import java.util.ArrayList;

import salsa_lite.compiler.definitions.CompilerErrors;
import salsa_lite.compiler.definitions.CMessageHandler;


public class MessageSymbol extends Invokable {

    TypeSymbol passType;
    public TypeSymbol getPassType()     { return passType; }

    public String getLongSignature() {
        return passType.getLongSignature() + " " + super.getLongSignature();
    }   

    public MessageSymbol replaceGenerics(ArrayList<String> declaredGenericTypes, ArrayList<TypeSymbol> instantiatedGenericTypes) {
        TypeSymbol newPassType = Symbol.getGenericReplacement(this.passType.getName(), declaredGenericTypes, instantiatedGenericTypes );
        if (newPassType == null) newPassType = this.passType;

        TypeSymbol[] replacedParameters = new TypeSymbol[parameterTypes.length];
        for (int i = 0; i < replacedParameters.length; i++) {
            TypeSymbol replacement = Symbol.getGenericReplacement( parameterTypes[i].getName(), declaredGenericTypes, instantiatedGenericTypes );
            if (replacement != null) {
                replacedParameters[i] = replacement;
            } else {
                replacedParameters[i] = parameterTypes[i];
            }
        }

        return new MessageSymbol(id, getName(), this.enclosingType, newPassType, replacedParameters);
    }

    public MessageSymbol(int id, String name, TypeSymbol newEnclosingType, TypeSymbol newPassType, TypeSymbol[] parameterTypes) {
        super(id, name, newEnclosingType);

        this.passType = newPassType;
        this.parameterTypes = parameterTypes;
    }

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
