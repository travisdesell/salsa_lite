package salsa_lite.compiler.symbol_table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.StringTokenizer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.ParseException;

import salsa_lite.compiler.definitions.CompilerErrors;
import salsa_lite.compiler.definitions.CExpression;

public class ObjectType extends TypeSymbol {

    public boolean isMutable = true;
    public boolean isEnum = false;

	protected ArrayList<MethodSymbol> method_handlers = new ArrayList<MethodSymbol>();

	public MethodSymbol getMethodHandler(int i) {
        return method_handlers.get(i);
    }

    public MethodSymbol getMethod(String name, Vector<CExpression> parameters) throws SalsaNotFoundException, VariableDeclarationException {
        return (MethodSymbol)Invokable.matchInvokable(new Invokable(name, this, parameters), method_handlers);
    }

    public ObjectType(String name, TypeSymbol superType) throws SalsaNotFoundException {
        this(name);
        this.superType = superType;
    }

	public ObjectType(String name) throws SalsaNotFoundException {
        if (!name.contains(".")) {
            if (SymbolTable.getCurrentModule().equals("") || name.startsWith("?")) {
                this.module = "";
                this.name = name;
            } else {
                System.err.println("ERROR: creating new ObjectType without package: " + name);
                throw new RuntimeException();
            }
        } else {
            this.module = name.substring(0, name.lastIndexOf('.') + 1);
            this.name = name.substring(name.lastIndexOf('.') + 1, name.length());
        }
    }

    private ObjectType(boolean isMutable, boolean isEnum, String module, String name, TypeSymbol superType, ArrayList<TypeSymbol> implementsTypes, ArrayList<String> declaredGenericTypes, ArrayList<FieldSymbol> fields, ArrayList<ConstructorSymbol> constructors, ArrayList<MethodSymbol> method_handlers) {
        this.isMutable = isMutable;
        this.isEnum = isEnum;
        this.module = module;
        this.name = name;
        this.superType = superType;
        this.implementsTypes = implementsTypes;
        this.declaredGenericTypes = declaredGenericTypes;
        this.fields = fields;
        this.constructors = constructors;
        this.method_handlers = method_handlers;
    }

    public TypeSymbol copy() {
        ObjectType copy = new ObjectType(isMutable, isEnum, module, name, superType, new ArrayList<TypeSymbol>(implementsTypes), new ArrayList<String>(declaredGenericTypes), new ArrayList<FieldSymbol>(fields), new ArrayList<ConstructorSymbol>(constructors), new ArrayList<MethodSymbol>(method_handlers));

        for (ConstructorSymbol cs : copy.constructors)   cs.enclosingType = copy;
        for (MethodSymbol ms : copy.method_handlers)    ms.enclosingType = copy;
        for (FieldSymbol fs : copy.fields)              fs.enclosingType = copy;

        return copy;
    } 

    public TypeSymbol replaceGenerics(String genericTypesString) throws SalsaNotFoundException, VariableDeclarationException {
        ObjectType copy = (ObjectType)this.copy();

        if (!copy.isGeneric()) throw new SalsaNotFoundException(module, name, "Tried to replace generics on non generic-class: " + copy.getLongSignature());

        ArrayList<TypeSymbol> instantiatedGenericTypes = parseGenerics(genericTypesString, declaredGenericTypes, true);

        if (instantiatedGenericTypes.size() != declaredGenericTypes.size()) {
            throw new SalsaNotFoundException(module, name, "Wrong number of generic parameters. instantiated " + instantiatedGenericTypes.toString() + ", declared " + declaredGenericTypes.toString() + ". generic object");
        }

        for (int i = 0; i < instantiatedGenericTypes.size(); i++) {
//            if (SymbolTable.isGeneric(instantiatedGenericTypes.get(i).getLongSignature())) {
//                copy.declaredGenericTypes.set(i, instantiatedGenericTypes.get(i).getLongSignature());
//            }
            copy.declaredGenericTypes.set(i, instantiatedGenericTypes.get(i).getLongSignature());
        }

        if (copy.superType != null && copy.superType.isGeneric()) {
            copy.superType = copy.superType.replaceGenerics(getGenericsString(copy.superType, declaredGenericTypes, instantiatedGenericTypes));
        } else {
            copy.superType = this.superType;
        }

        for (int i = 0; i < implementsTypes.size(); i++) {
            if (copy.implementsTypes.get(i).isGeneric()) {
                String genericsString = getGenericsString(copy.implementsTypes.get(i), declaredGenericTypes, instantiatedGenericTypes);

                copy.implementsTypes.set(i, copy.implementsTypes.get(i).replaceGenerics(genericsString));
            }
        }

        for (int i = 0; i < copy.constructors.size(); i++)       copy.constructors.set(i, copy.constructors.get(i).replaceGenerics(declaredGenericTypes, instantiatedGenericTypes));
        for (int i = 0; i < copy.fields.size(); i++)             copy.fields.set(i, copy.fields.get(i).replaceGenerics(declaredGenericTypes, instantiatedGenericTypes));
        for (int i = 0; i < copy.method_handlers.size(); i++)    copy.method_handlers.set(i, copy.method_handlers.get(i).replaceGenerics(declaredGenericTypes, instantiatedGenericTypes));

//        System.err.println("replaced generics for " + copy.getLongSignature());
//        System.err.println("\tfields: " + copy.fields.toString());
//        System.err.println("\tconstructors: " + copy.constructors.toString());
//        System.err.println("\tmethod_handlers: " + copy.method_handlers.toString());

        return copy;
    }

	public void load() throws SalsaNotFoundException, VariableDeclarationException {
		int i;
        Class importClass = null;

        try {
            importClass = Class.forName(this.module + this.name);

            for (TypeVariable tv : importClass.getTypeParameters()) {
                declaredGenericTypes.add(tv.getName());
                addGenericType(tv.getName(), "Object");

                TypeSymbol ts = SymbolTable.getTypeSymbol(tv.getName());
           }
        } catch(Exception e) {
//            System.err.println("Error using reflection to get class: '" + this.module + this.name + "'");
            throw new SalsaNotFoundException("Could not load java object '" + this.getLongSignature() + "'", e);
        }

        if (importClass.isEnum()) {
            isMutable = false;
            isEnum = true;
        }

        if (importClass.getSuperclass() != null) {
            String genericSuperclassName = importClass.getGenericSuperclass().toString();
            genericSuperclassName = genericStringToName(genericSuperclassName);

            superType = SymbolTable.getTypeSymbol( genericSuperclassName );
        }

        Type[] genericInterfaces = importClass.getGenericInterfaces();
        Class[] interfaces = importClass.getInterfaces();
        if (interfaces.length > 0) {
            for (i = 0; i < interfaces.length; i++) {
                String genericInterfaceName = genericInterfaces[i].toString();
                genericInterfaceName = genericStringToName(genericInterfaceName);

                TypeSymbol implementsType = SymbolTable.getTypeSymbol(genericInterfaceName);
                implementsTypes.add( implementsType );
            }
        }

		Constructor[] cv = importClass.getConstructors();
		for (i = 0; i < cv.length; i++) {
            ConstructorSymbol cs = new ConstructorSymbol(i, this, cv[i]);
            constructors.add( cs );
        }

		Method[] mv = importClass.getMethods();
		for (i = 0; i < mv.length; i++) {
//            System.err.println("Object; " + getName() + ", generic method: " + mv[i].toGenericString());
            MethodSymbol ms = new MethodSymbol(i, this, mv[i]);
            method_handlers.add( ms );
        }

        Field[] fv = importClass.getFields();
        for (i = 0; i < fv.length; i++) {
//            if (isEnum) {
//                System.err.println("Object: " + getName() + ", generic field: " + fv[i].toGenericString());
//            }

            FieldSymbol fs = new FieldSymbol(this, fv[i]);
            fields.add( fs );

            if (isEnum) {
                SymbolTable.addGlobalVariableType(fs.getName(), fs.getType().getLongSignature(), false, true);
                System.err.println("Added variable: " + fs.getName() + ", type: " + fs.getType().getLongSignature());
            }
//            if (isEnum) {
//                System.err.println("Field type is: " + fs.getType());
//            }
        }
	}
}
