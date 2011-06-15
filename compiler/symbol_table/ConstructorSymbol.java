package salsa_lite.compiler.symbol_table;

import java.lang.reflect.Constructor;

import salsa_lite.compiler.definitions.CConstructor;
import salsa_lite.compiler.definitions.CompilerErrors;

public class ConstructorSymbol extends Invokable {

    public ConstructorSymbol(int id, TypeSymbol enclosingType, Constructor constructor) throws SalsaNotFoundException {
        super(id, enclosingType);

        Class[] parameterClasses = constructor.getParameterTypes();
        parameterTypes = new TypeSymbol[parameterClasses.length];

        int i = 0;
        for (Class c : parameterClasses) {
            parameterTypes[i++] = SymbolTable.getTypeSymbol( c.getName() );
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
