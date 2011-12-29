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

    public ConstructorSymbol(int id, TypeSymbol enclosingType, Constructor constructor) throws SalsaNotFoundException, VariableDeclarationException {
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
            } catch (VariableDeclarationException vde) {
                CompilerErrors.printErrorMessage("Could not declare argument for constructor. " + vde.toString(), constructor.getArgument(i - 1));
                throw new RuntimeException(vde);
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("Could not find argument type for constructor.", constructor.getArgument(i - 1));
                throw new RuntimeException(snfe);
            }
        }
    }

    public ConstructorSymbol copy() {
        ConstructorSymbol copy = new ConstructorSymbol(this.id, this.enclosingType, this.parameterTypes);
        copy.isOverloadedByParent = this.isOverloadedByParent;
        return copy;
    }
}
