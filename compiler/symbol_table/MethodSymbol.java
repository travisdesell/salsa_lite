package salsa_lite.compiler.symbol_table;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.Arrays;
import java.util.ArrayList;

public class MethodSymbol extends Invokable {

    boolean isStatic;

    TypeSymbol returnType;

    public TypeSymbol getReturnType() { return returnType; }

    public String getLongSignature() {
        return returnType.getLongSignature() + " " + super.getLongSignature();
    }
        
    public MethodSymbol replaceGenerics(ArrayList<String> declaredGenericTypes, ArrayList<TypeSymbol> instantiatedGenericTypes) throws SalsaNotFoundException {
        TypeSymbol newReturnType = Symbol.getGenericReplacement(this.returnType.getName(), declaredGenericTypes, instantiatedGenericTypes );
        if (newReturnType == null) {
            newReturnType = this.returnType;
        }

        TypeSymbol[] replacedParameters = new TypeSymbol[parameterTypes.length];
        for (int i = 0; i < replacedParameters.length; i++) {
            TypeSymbol replacement = Symbol.getGenericReplacement( parameterTypes[i].getName(), declaredGenericTypes, instantiatedGenericTypes );
            if (replacement != null) {
                replacedParameters[i] = replacement;
            } else {
                replacedParameters[i] = parameterTypes[i];
            }
        }

        return new MethodSymbol(id, getName(), isStatic, this.enclosingType, newReturnType, replacedParameters);
    }

    public MethodSymbol(int id, String name, boolean isStatic, TypeSymbol enclosingType, TypeSymbol returnType, TypeSymbol[] parameterTypes) {
        super(id, name, enclosingType);

        this.isStatic = isStatic;

        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public MethodSymbol(int id, TypeSymbol enclosingType, Method method) throws SalsaNotFoundException {
        super(id, method.getName(), enclosingType);

//        System.err.println("method non generic returnName: " + method.getReturnType().getName());
//        System.err.println("method returnName (before parse) is: " + method.getGenericReturnType().toString());
        if (method.getGenericReturnType() instanceof ParameterizedType) {
            for (Type t : ((ParameterizedType)(method.getGenericReturnType())).getActualTypeArguments()) {
//                System.err.println("\tgeneric types: " + t.toString());
            }
        }

        TypeVariable[] typeParameters = method.getTypeParameters();
        if (typeParameters.length > 0) {
//            System.err.println("initializing method with generic type parameters: " + method.toGenericString());
//            System.err.println("\ttype parameters: " + Arrays.toString(typeParameters));

            for (TypeVariable tv : typeParameters) {
                TypeSymbol.addGenericType(tv.getName(), "Object");
           }
        }

        String returnName = TypeSymbol.genericStringToName(method.getGenericReturnType().toString());
//        System.err.println("method returnName is: " + returnName);
        this.returnType = SymbolTable.getTypeSymbol( returnName );
        this.isStatic = Modifier.isStatic(method.getModifiers());

        Type[] parameterClasses = method.getGenericParameterTypes();
        parameterTypes = new TypeSymbol[parameterClasses.length];

        int i = 0;
        for (Type t : parameterClasses) {
            String parameterName = TypeSymbol.genericStringToName(t.toString());
//            System.err.println("method parameterName is: " + parameterName);
            parameterTypes[i++] = SymbolTable.getTypeSymbol( parameterName );
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
