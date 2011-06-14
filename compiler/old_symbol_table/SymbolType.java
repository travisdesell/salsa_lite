package salsa_lite.compiler.symbol_table;

import java.util.StringTokenizer;
import java.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.ParseException;

import salsa_lite.compiler.definitions.CCompilationUnit;
import salsa_lite.compiler.definitions.CConstructor;
import salsa_lite.compiler.definitions.CMessageHandler;
import salsa_lite.compiler.definitions.CLocalVariableDeclaration;

public class SymbolType {
	boolean is_actor = true;
	public boolean isActor() { return is_actor; }

	public boolean is_primitive = false;
	public boolean isPrimitive() { return is_primitive; }

    boolean is_object = false;
    boolean is_enum = false;

    public boolean isObject() { return is_object; }
    public boolean isEnum() { return is_enum; }

	SymbolType parent_type = null;
	String name;
	String module = "";

	public String getName() { return name; }
	public String getModule() { return module; }

	private Vector<SymbolVariable> fields = new Vector<SymbolVariable>();
	private Vector<SymbolMessage> constructors = new Vector<SymbolMessage>();
	private Vector<SymbolMessage> message_handlers = new Vector<SymbolMessage>();

	public SymbolVariable getField(int i) { return fields.get(i); }
	public SymbolMessage getConstructor(int i) { return constructors.get(i); }
	public SymbolMessage getMessageHandler(int i) { return message_handlers.get(i); }

	public Vector<SymbolVariable> getFields() { return fields; }
	public Vector<SymbolMessage> getConstructors() { return constructors; }
	public Vector<SymbolMessage> getMessageHandlers() { return message_handlers; }

	public SymbolType() {
	}

	public SymbolType(String name) {
		this.name = name;
		this.is_primitive = true;
	}

	public SymbolType(String module, String name) {
		this.module = module;
		this.name = name;
		this.is_primitive = true;
	}

	public SymbolType(String module, String name, boolean is_actor) {
		this.module = module;
		this.name = name;
		this.parent_type = null;
		this.is_actor = is_actor;
	}

	public SymbolType(String module, String name, SymbolType parent_type, boolean is_actor) {
		this.module = module;
		this.name = name;
		this.parent_type = parent_type;
		this.is_actor = is_actor;
	}

	public void addField(SymbolVariable field) {
//		System.out.println(getLongSignature() + " -- added field -- " + field.getLongSignature());
		fields.add(field);
	}

	public void addConstructor(SymbolMessage message) {
//		System.out.println(getLongSignature() + " -- added constructor -- " + message.getLongSignature());
		constructors.add(message);
	}

	public void addMessageHandler(SymbolMessage message) {
//		System.out.println(getLongSignature() + " -- added message handler -- " + message.getLongSignature());
		message_handlers.add(message);
	}

	public String getSignature() {
		return name;
	}

	public String getLongSignature() {
		if (module.equals("")) return name;
		else return module + name;
	}


	static String[] classpaths = null;
	static {
                String classpath = System.getProperty("java.class.path");

                StringTokenizer st = new StringTokenizer(classpath, ":");
                classpaths = new String[st.countTokens()];
                for (int i = 0; i < classpaths.length; i++) {
                        classpaths[i] = st.nextToken();
                        if (classpaths[i].charAt(classpaths[i].length() - 1) != File.separatorChar) classpaths[i] += File.separatorChar;
                }
	}

	public void loadSymbolType() {
		if (loadAsActor()) return;
		if (loadAsObject()) return;

		System.err.println("Compiler Error: Could not load type: " + getLongSignature());
                String file = new String(module + name).replace('.', File.separatorChar);
		System.out.println("\tCompiler Error: Could not find actor: " + file);
		System.out.println("\tCompiler Error: Class file could not be loaded: " + name + " -- imported Java Objects must be compiled.");

	}


	public boolean loadAsActor() {
		int i;

		String file;
		String temp_module = "";
		if (module.equals("")) {
			temp_module = SymbolTable.getCurrentModule();
			if (temp_module.equals("")) {
				temp_module = SymbolTable.getWorkingDirectory();
			}
//			System.err.println("ATTEMPTING TO UPDATE MODULE TO: " + temp_module + " FOR " + name);
	                file = new String(temp_module + name).replace('.', File.separatorChar);
		} else {
	                file = new String(module + name).replace('.', File.separatorChar);
		}

                CCompilationUnit cu;
                try {
                        boolean found = false;
                        FileInputStream fis = null;
                        for (i = 0; i < SymbolType.classpaths.length; i++) {
//                            System.out.println("looking in classpath[" + i + "] " + file + ".salsa : " + SymbolType.classpaths[i] + file + ".salsa");
                                try {
                                        fis = new FileInputStream(SymbolType.classpaths[i] + file + ".salsa");
                                } catch (FileNotFoundException e) {
                                        continue;
                                }
                                found = true;
                                break;
                        }
                        if (!found) {
//                                System.out.println("Compiler Error: Could not find actor: " + file);
                                return false;
                        }
                        SalsaParser.ReInit(fis);
                        cu = SalsaParser.CompilationUnit();
                } catch (ParseException e) {
                        System.out.println(e.getMessage());
                        System.out.println("Compiler Error: Could not parse actor: " + file + ", imported SALSA actors must be parseable");
                        return false;
                }

		if (module.equals("")) {
//			System.err.println("UPDATING MODULE TO: " + temp_module + " FOR " + name) ;
			this.module = temp_module;
		}

		this.is_primitive = false;
		this.is_actor = true;
		this.parent_type = SymbolTable.getSymbolType(cu.getExtendsName());

		Vector<CConstructor> constructors = cu.getConstructors();
		for (i = 0; i < constructors.size(); i++) addConstructor( new SymbolMessage(i, this, constructors.get(i)) );

                Vector<CMessageHandler> message_handlers = cu.getMessageHandlers();
		for (i = 0; i < message_handlers.size(); i++) addMessageHandler( new SymbolMessage(i, this, message_handlers.get(i)) );

		Vector<CLocalVariableDeclaration> fields = cu.getFields();
		for (i = 0; i < fields.size(); i++) {
			CLocalVariableDeclaration field = fields.get(i);
			for (int j = 0; j < fields.get(i).variables.size(); j++) {
				SymbolType fieldSymbolType = SymbolTable.getSymbolType(field.type);
				if (fieldSymbolType == null) {
					System.err.println("Compiler Error: could not get type " + field.type + " of variable " + field.variables.get(j).name);
				} else {
					addField( new SymbolVariable(fieldSymbolType, field.variables.get(j).name) );
				}
			}
		}

		return true;
	}

	public boolean loadAsObject() {
		int i;
        Class importClass = null;

		String temp_module = "";
		if (module == null) module = "";

        System.err.println("Trying to load object: [" + module + "] " + name);

        try {
            importClass = Class.forName(name);
        } catch (ClassNotFoundException e) {
//			System.out.println("Compiler Error: Unknown class: " + name + ", imported Java Objects must be compiled.");
			if (name.lastIndexOf('.') < 0) {
				temp_module = "java.lang.";
			}

			try {
				importClass = Class.forName(temp_module + name);
			} catch (ClassNotFoundException e2) {
				try {
					importClass = Class.forName(module + name);
					temp_module = module;
				} catch (ClassNotFoundException e3) {
					return false;
				}
			}
			module = temp_module;
		}
//		System.err.println("Found class: [" + module + "]" + name);
		if (module.equals("") && importClass.getPackage() != null) {
			this.module = importClass.getPackage().getName() + ".";
			this.name = importClass.getSimpleName();
//			System.err.println("module: " + this.module);
//			System.err.println("name: " + this.name);
		}
//		System.err.println("Found class: [" + module + "]" + name);

		this.is_primitive = false;
		this.is_actor = false;
		this.parent_type = null;
		
		Constructor[] constructors = importClass.getConstructors();
		for (i = 0; i < constructors.length; i++) addConstructor( new SymbolMessage(i, this, constructors[i]) );

		Method[] methods = importClass.getMethods();
		for (i = 0; i < methods.length; i++) addMessageHandler( new SymbolMessage(i, this, methods[i]) );

        Field[] fields = importClass.getFields();
        for (i = 0; i < fields.length; i++) {
            System.out.println("adding field [" + fields[i].getType().getCanonicalName() + " -- " + fields[i].getName() + "] to object [" + name + "]");

            SymbolType fieldSymbolType = SymbolTable.getSymbolType(fields[i].getType().getCanonicalName());
            if (fieldSymbolType == null) {
                System.err.println("Compiler Error: could not get type " + fields[i].getType().getCanonicalName() + " of variable " + fields[i].getName());
            } else {
                fieldSymbolType.is_object = true;
                fieldSymbolType.is_actor = false;
                addField( new SymbolVariable(fieldSymbolType, fields[i].getName()) );
            }
        }


		return true;
	}
}
