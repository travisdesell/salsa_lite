package salsa_lite.compiler.symbol_table;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.ArrayList;

import salsa_lite.compiler.definitions.CompilerErrors;
import salsa_lite.compiler.definitions.CLocalVariableDeclaration;
import salsa_lite.compiler.definitions.CVariableInit;

public class FieldSymbol {

    String name;
    TypeSymbol type;
    TypeSymbol enclosingType;

    public String getName()         { return name; }
    public TypeSymbol getType()     { return type; }

    public FieldSymbol(TypeSymbol enclosingType, Field f, ArrayList<String> declaredGenericTypes, ArrayList<TypeSymbol> instantiatedGenericTypes) throws SalsaNotFoundException {
        this.enclosingType = enclosingType;
        this.name = f.getName();

        Type t = f.getGenericType();
        int generic_index = declaredGenericTypes.indexOf(t.toString());
        this.type = instantiatedGenericTypes.get(generic_index);

        System.out.println("GENERIC FIELD TYPE: " + getLongSignature());
    }

    public FieldSymbol(TypeSymbol enclosingType, Field f) throws SalsaNotFoundException {
        this.enclosingType = enclosingType;

        this.name = f.getName();
        this.type = SymbolTable.getTypeSymbol( f.getType().getName() );
    }

    public FieldSymbol(TypeSymbol enclosingType, CLocalVariableDeclaration variableDeclaration, CVariableInit variableInit) {
        this.enclosingType = enclosingType;

        this.name = variableInit.name;
        try {
            this.type = SymbolTable.getTypeSymbol( variableDeclaration.type.name );
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("Could not find type for field. " + snfe.toString(), variableDeclaration);
            throw new RuntimeException(snfe);
        }
    }

    public FieldSymbol(TypeSymbol enclosingType, String name, TypeSymbol type) {
        this.name = name;
        this.enclosingType = enclosingType;
        this.type = type;
    }

    public String getSignature()            { return type.getSignature() + " " + name; }
    public String getLongSignature()        { return type.getLongSignature() + " " + name; }
}
