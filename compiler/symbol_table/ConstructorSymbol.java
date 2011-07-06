package salsa_lite.compiler.symbol_table;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.ArrayList;

import salsa_lite.compiler.definitions.CConstructor;
import salsa_lite.compiler.definitions.CompilerErrors;

public class ConstructorSymbol extends Invokable {

    public ConstructorSymbol replaceGenerics(ArrayList<String> declaredGenericTypes, ArrayList<TypeSymbol> instantiatedGenericTypes) throws SalsaNotFoundException {
//        System.err.println("replacing generics on '" + getLongSignature() + "' -- declared: " + declaredGenericTypes.toString() + ", instantiated: " + instantiatedGenericTypes.toString());

        TypeSymbol[] replacedParameters = new TypeSymbol[parameterTypes.length];
        for (int i = 0; i < replacedParameters.length; i++) {
            TypeSymbol replacement = Symbol.getGenericReplacement( parameterTypes[i].getName(), declaredGenericTypes, instantiatedGenericTypes );
            if (replacement != null) {
                replacedParameters[i] = replacement;
            } else {
                replacedParameters[i] = parameterTypes[i];
            }
        }

        ConstructorSymbol replacement = new ConstructorSymbol(id, this.enclosingType, replacedParameters);
//        System.err.println("replaced generic constructor: " + replacement.getLongSignature());

        return replacement;
    }

    public ConstructorSymbol(int id, TypeSymbol enclosingType, TypeSymbol[] parameterTypes) {
        super(id, enclosingType);
        this.parameterTypes = parameterTypes;
    }

/*
    public ConstructorSymbol(int id, TypeSymbol enclosingType, Constructor constructor, ArrayList<String> declaredGenericTypes, ArrayList<TypeSymbol> instantiatedGenericTypes) throws SalsaNotFoundException {
        super(id, enclosingType);

        Type[] typeVariables = constructor.getGenericParameterTypes();
        parameterTypes = new TypeSymbol[typeVariables.length];

        int generic_index;
        for (int i = 0; i < typeVariables.length; i++) {
            generic_index = declaredGenericTypes.indexOf( typeVariables[i].toString() );

            String strippedArray = "";
            String typeName = typeVariables[i].toString();
            if (typeName.contains("[]")) {
                strippedArray = typeName.substring( typeName.indexOf("["), typeName.lastIndexOf("]") + 1);
                typeName = typeName.substring(0, typeName.indexOf("["));
            }

            generic_index = declaredGenericTypes.indexOf( typeName );

            if (generic_index < 0) {
                parameterTypes[i] = SymbolTable.getTypeSymbol( constructor.getParameterTypes()[i].getName() );
            } else {
                parameterTypes[i] = SymbolTable.getTypeSymbol( instantiatedGenericTypes.get(generic_index).getName() + strippedArray);
            }
        }
    }
*/

    public ConstructorSymbol(int id, TypeSymbol enclosingType, Constructor constructor) throws SalsaNotFoundException {
        super(id, enclosingType);

        Type[] parameterClasses = constructor.getParameterTypes();
        parameterTypes = new TypeSymbol[parameterClasses.length];

        int i = 0;
        for (Type t : parameterClasses) {
            String parameterName = TypeSymbol.genericStringToName(t.toString());
//            System.err.println("constructor parameterName: " + parameterName);
            parameterTypes[i++] = SymbolTable.getTypeSymbol( parameterName );
        }
    }

    public ConstructorSymbol(int id, TypeSymbol enclosingType, CConstructor constructor) {
        super(id, enclosingType);

        String[] argumentTypes = constructor.getArgumentTypes();
        parameterTypes = new TypeSymbol[argumentTypes.length];

        int i = 0;
        for (String s : argumentTypes) {
            try {
                parameterTypes[i++] = SymbolTable.getTypeSymbol( s );
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("Could not find argument type for constructor.", constructor.getArgument(i - 1));
                throw new RuntimeException(snfe);
            }
        }
    }
}
