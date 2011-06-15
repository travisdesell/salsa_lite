package salsa_lite.compiler.symbol_table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.StringTokenizer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.ParseException;

import salsa_lite.compiler.definitions.CompilerErrors;
import salsa_lite.compiler.definitions.CExpression;

public class ObjectType extends TypeSymbol {

    public boolean isMutable = true;
    public boolean isEnum = true;

    public ArrayList<TypeSymbol> instantiatedGenericTypes = new ArrayList<TypeSymbol>();
    public ArrayList<String> declaredGenericTypes = new ArrayList<String>();

	public ObjectType(String name) throws SalsaNotFoundException {
        if (!name.contains(".")) {
            System.err.println("ERROR: creating new ObjectType without package: " + name);
            throw new RuntimeException();
        }

        this.module = name.substring(0, name.lastIndexOf('.') + 1);
        this.name = name.substring(name.lastIndexOf('.') + 1, name.length());

        if (name.contains("<")) {
//            System.out.println("New Generic ObjectType -- [" + this.module + "] " + this.name);
            String genericTypesString = name.substring(name.indexOf("<") + 1, name.length() - 1);

//            System.out.println("generic types: " + genericTypesString);
            StringTokenizer st = new StringTokenizer(genericTypesString, ", ");
            while (st.hasMoreTokens()) {
                String generic_type = st.nextToken();

                TypeSymbol ts = SymbolTable.getTypeSymbol(generic_type);
                instantiatedGenericTypes.add(ts);
            }

            try {
                Class importClass = Class.forName(this.module + this.name.substring(0, this.name.indexOf("<")));
//                System.out.println("reflection class generic types:");
                for (TypeVariable tv : importClass.getTypeParameters()) {
                    declaredGenericTypes.add(tv.getName());
                }
            } catch(Exception e) {
                System.err.println("Error using reflection on generic class: '" + this.module + this.name + "'");
                throw new RuntimeException(e);
            }

            if (instantiatedGenericTypes.size() != declaredGenericTypes.size()) {
                throw new SalsaNotFoundException(module, name, "Wrong number of generic parameters. generic object");
            }

//            System.out.println("generic class instantiated types:");
//            for (int i = 0; i < instantiatedGenericTypes.size(); i++) {
//                System.out.println("\t" + declaredGenericTypes.get(i) + " -- " + instantiatedGenericTypes.get(i).getSignature());
//            }
        }
	}

    public ObjectType(String name, TypeSymbol superType) throws SalsaNotFoundException {
        this(name);
        this.superType = superType;
    }

	protected LinkedHashMap<String,MethodSymbol> method_handlers     = new LinkedHashMap<String,MethodSymbol>();

	public MethodSymbol         getMethodHandler(int i)     { return new ArrayList<MethodSymbol>(method_handlers.values()).get(i); }

    public MethodSymbol         getMethod(String s)         { return method_handlers.get(s); }

    public MethodSymbol getMethod(String name, Vector<CExpression> parameters) throws SalsaNotFoundException {
        return (MethodSymbol)Invokable.matchInvokable(new Invokable(name, this, parameters), method_handlers);
    }

	public void load() throws SalsaNotFoundException {
		int i;
        Class importClass = null;

//        System.err.println("Trying to load object: [" + module + "] " + name);

        String name = this.name;
        if (name.contains("<")) name = name.substring(0, name.indexOf("<"));

        try {
            importClass = Class.forName(module + name);
        } catch (ClassNotFoundException e) {
//			System.out.println("Compiler Error: Unknown class: [" + module + "] " + name + " -- imported Java Objects must be compiled.");
            throw new SalsaNotFoundException(module, name, "object");
		}

        if (importClass.isEnum()) {
            isMutable = false;
            isEnum = true;
        }

        /*
        TypeVariable[] typeParameters = importClass.getTypeParameters();
        for (TypeVariable tv : typeParameters) {
            System.out.println(getLongSignature() + " -- OBJECT GENERIC TYPE PARAMETER: " + tv);
        }
        o*/

        if (importClass.getSuperclass() != null) {
            if (this.name.contains("<")) {
                String superClassString = importClass.getGenericSuperclass().toString();
                if (superClassString.substring(0, 6).equals("class ")) {
                    superClassString = superClassString.substring(6, superClassString.length());
                }
                if (superClassString.substring(0, 10).equals("interface ")) {
                    superClassString = superClassString.substring(10, superClassString.length());
                }

                String replacedGenericString = TypeSymbol.replaceGenerics(superClassString, declaredGenericTypes, instantiatedGenericTypes);
                superType = SymbolTable.getTypeSymbol( replacedGenericString );
            } else {
                superType = SymbolTable.getTypeSymbol( importClass.getSuperclass().getName() );
            }
        }

        Class[] interfaces = importClass.getInterfaces();
        if (interfaces.length > 0) {
            if (this.name.contains("<")) {
                Type[] genericInterfaces = importClass.getGenericInterfaces();

                for (i = 0; i < genericInterfaces.length; i++) {
                    String interfaceString = genericInterfaces[i].toString();
                    if (interfaceString.substring(0, 6).equals("class ")) {
                        interfaceString = interfaceString.substring(6, interfaceString.length());
                    }
                    if (interfaceString.substring(0, 10).equals("interface ")) {
                        interfaceString = interfaceString.substring(10, interfaceString.length());
                    }

                    implementsTypes.add( SymbolTable.getTypeSymbol( TypeSymbol.replaceGenerics(interfaceString, declaredGenericTypes, instantiatedGenericTypes) ) );
                }
            } else {
                for (i = 0; i < interfaces.length; i++) {
                    implementsTypes.add( SymbolTable.getTypeSymbol( interfaces[i].getName() ) );
                }
            }
        }

		Constructor[] cv = importClass.getConstructors();
		for (i = 0; i < cv.length; i++) {
            ConstructorSymbol cs = null;

            Type[] tv = cv[i].getGenericParameterTypes();
            if (this.name.contains("<") && tv.length > 0) {
//                System.out.println("constructor type parameters for: " + cv[i].getName());
//                for (Type tv1 : tv) System.out.println("\t" + tv1.toString());
                cs = new ConstructorSymbol(i, this, cv[i], declaredGenericTypes, instantiatedGenericTypes);
            } else {
                cs = new ConstructorSymbol(i, this, cv[i]);
            }

            constructors.put( cs.getLongSignature(), cs );
        }

		Method[] mv = importClass.getMethods();
		for (i = 0; i < mv.length; i++) {
            MethodSymbol ms = null;

            Type[] tv = mv[i].getGenericParameterTypes();
            if (this.name.contains("<") && tv.length > 0) {
//                System.out.println("method type parameters for: " + getLongSignature() + " " + mv[i].getName());
//                for (Type tv1 : tv) System.out.println("\t" + tv1.toString());

                ms = new MethodSymbol(i, this, mv[i], declaredGenericTypes, instantiatedGenericTypes);
            } else {
                ms = new MethodSymbol(i, this, mv[i]);
            }

            method_handlers.put( ms.getLongSignature(), ms );
        }

        Field[] fv = importClass.getFields();
        for (i = 0; i < fv.length; i++) {
            FieldSymbol fs = null;
            Type t = fv[i].getGenericType();
            if (this.name.contains("<") && t != null) {
                fs = new FieldSymbol(this, fv[i], declaredGenericTypes, instantiatedGenericTypes);
            } else {
                fs = new FieldSymbol(this, fv[i]);
            }

            fields.put( fs.getLongSignature(), fs );
        }
	}
}
