package salsa_lite.compiler.symbol_table;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class MethodSymbol extends Invokable {

    boolean isStatic;

    TypeSymbol returnType;

    public TypeSymbol getReturnType() { return returnType; }

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

        /*
        TypeVariable[] typeVariables = method.getTypeParameters();
        for (TypeVariable tv : typeVariables) {
            System.out.println(enclosingType.getLongSignature() + " METHOD " + method.getName() + " -- generic type varaible: " + tv);
        }

        Type tv = method.getGenericReturnType();
        if (tv instanceof ParameterizedType) System.out.println(enclosingType.getLongSignature() + " METHOD " + method.getName() + " -- generic return type: " + tv);
        */
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
