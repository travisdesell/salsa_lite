package salsa_lite.compiler.symbol_table;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.ArrayList;

public class MethodSymbol extends Invokable {

    boolean isStatic;

    TypeSymbol returnType;

    public TypeSymbol getReturnType() { return returnType; }

    public MethodSymbol(int id, TypeSymbol enclosingType, Method method, ArrayList<String> declaredGenericTypes, ArrayList<TypeSymbol> instantiatedGenericTypes) throws SalsaNotFoundException {
        super(id, method.getName(), enclosingType);
        
        Type[] typeVariables = method.getGenericParameterTypes();
        parameterTypes = new TypeSymbol[typeVariables.length];

        int generic_index;
        for (int i = 0; i < typeVariables.length; i++) {
            String strippedArray = "";
            String typeName = typeVariables[i].toString();
            if (typeName.contains("[]")) {
                strippedArray = typeName.substring( typeName.indexOf("["), typeName.lastIndexOf("]") + 1);
                typeName = typeName.substring(0, typeName.indexOf("["));
            }

            generic_index = declaredGenericTypes.indexOf( typeName );
            if (generic_index < 0) {
                parameterTypes[i] = SymbolTable.getTypeSymbol( method.getParameterTypes()[i].getName() );
            } else {
                parameterTypes[i] = SymbolTable.getTypeSymbol( instantiatedGenericTypes.get(generic_index).getName() + strippedArray);
            }
        }

        String strippedArray = "";
        String typeName = method.getGenericReturnType().toString();
        if (typeName.contains("[]")) {
            strippedArray = typeName.substring( typeName.indexOf("["), typeName.lastIndexOf("]") + 1);
            typeName = typeName.substring(0, typeName.indexOf("["));
        }

        generic_index = declaredGenericTypes.indexOf( typeName );
        if (generic_index < 0) {
            this.returnType = SymbolTable.getTypeSymbol( method.getReturnType().getName() );
        } else {
            this.returnType = SymbolTable.getTypeSymbol( instantiatedGenericTypes.get(generic_index).getName() + strippedArray);
        }
    }

    public MethodSymbol(int id, TypeSymbol enclosingType, Method method) throws SalsaNotFoundException {
        super(id, method.getName(), enclosingType);

        this.returnType = SymbolTable.getTypeSymbol( method.getReturnType().getName() );
        this.isStatic = Modifier.isStatic(method.getModifiers());

        Class[] parameterClasses = method.getParameterTypes();
        parameterTypes = new TypeSymbol[parameterClasses.length];

        int i = 0;
        for (Class c : parameterClasses) {
            parameterTypes[i++] = SymbolTable.getTypeSymbol( c.getName() );
        }
    }

    public MethodSymbol(int id, TypeSymbol enclosingType, String name, TypeSymbol returnType, TypeSymbol[] parameterTypes, boolean isStatic) {
        super(id, name, enclosingType);

        this.returnType = returnType;
        this.isStatic = isStatic;

        this.parameterTypes = new TypeSymbol[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            this.parameterTypes[i] = parameterTypes[i];
        }
    }
}
