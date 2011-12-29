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

public class FieldSymbol extends Symbol {

    String name;
    TypeSymbol type;
    TypeSymbol enclosingType;

    public String getName()                 { return name; }
    public TypeSymbol getType()             { return type; }

    public String getSignature()            { return type.getSignature() + " " + name; }
    public String getLongSignature()        { return type.getLongSignature() + " " + name; }

    public FieldSymbol replaceGenerics(ArrayList<String> declaredGenericTypes, ArrayList<TypeSymbol> instantiatedGenericTypes) {
        TypeSymbol replacementType = getGenericReplacement(getName(), declaredGenericTypes, instantiatedGenericTypes); 

        if (replacementType != null) {
//            System.out.println("GENERIC FIELD TYPE: " + getLongSignature());
            return new FieldSymbol(this.enclosingType, this.name, replacementType);
        } else {
            return this;
        }
    }

    public FieldSymbol(TypeSymbol enclosingType, Field f) throws SalsaNotFoundException, VariableDeclarationException {
        this.enclosingType = enclosingType;

        this.name = f.getName();
        this.type = SymbolTable.getTypeSymbol( TypeSymbol.genericStringToName(f.getGenericType().toString()) );
    }

    public FieldSymbol(TypeSymbol enclosingType, CLocalVariableDeclaration variableDeclaration, CVariableInit variableInit) {
        this.enclosingType = enclosingType;

        this.name = variableInit.name;
//        System.err.println("initializing field with name: " + this.name + ", type: " + variableDeclaration.type.name);

        try {
            this.type = SymbolTable.getTypeSymbol( variableDeclaration.type.name );
        } catch (VariableDeclarationException vde) {
            CompilerErrors.printErrorMessage("Could not declare field. " + vde.toString(), variableDeclaration);
            throw new RuntimeException(vde);
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
}
