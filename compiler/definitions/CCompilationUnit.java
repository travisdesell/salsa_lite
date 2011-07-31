package salsa_lite.compiler.definitions;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.SimpleNode;

import java.util.Vector;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.ActorType;
import salsa_lite.compiler.symbol_table.TypeSymbol;
import salsa_lite.compiler.symbol_table.ConstructorSymbol;
import salsa_lite.compiler.symbol_table.MessageSymbol;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;

import salsa_lite.compiler.symbol_table.ObjectType;

public class CCompilationUnit {
	public String module_string = null;
    public String getModule() {
        return module_string;
    }

	public Vector<CImportDeclaration> import_declarations = new Vector<CImportDeclaration>();

	public CBehaviorDeclaration behavior_declaration;
	public CInterfaceDeclaration interface_declaration;

	public CJavaStatement java_statement;

	public CName getName() {
		if (behavior_declaration != null) return behavior_declaration.behavior_name;
		else return interface_declaration.interface_name;
	}

	public CName getExtendsName() {
		if (behavior_declaration != null) return behavior_declaration.getExtendsName();
		else return null;
	}

    public Vector<CName> getImplementsNames() {
        if (behavior_declaration == null) {
            return interface_declaration.extends_names;
        } else {
            return behavior_declaration.implements_names;
        }
    }

    public boolean isMobile() {
        return getExtendsName() != null && getExtendsName().name.equals("MobileActor");
    }

	public Vector<CEnumeration> getEnumerations() {
		if (behavior_declaration != null) return behavior_declaration.enumerations;
		else return null;
	}

	public Vector<CLocalVariableDeclaration> getFields() {
		if (behavior_declaration != null) return behavior_declaration.variable_declarations;
		else return interface_declaration.variable_declarations;
	}

	public Vector<CMessageHandler> getMessageHandlers() {
		if (behavior_declaration != null) return behavior_declaration.message_handlers;
		else return interface_declaration.message_handlers;
	}

	public Vector<CConstructor> getConstructors() {
		if (behavior_declaration != null) return behavior_declaration.constructors;
		else return null;
	}

	public String getImportDeclarationCode() {
		String code = "";
		String package_name = SymbolTable.getRuntimeModule();

		for (int i = 0; i < import_declarations.size(); i++) {
			CImportDeclaration import_declaration = import_declarations.get(i);
			String import_string = import_declaration.import_string;
			import_string = import_string.replace("salsa_lite.language.", package_name + ".language.");
			import_string = import_string.replace("salsa_lite.io.", package_name + ".io.");
			import_string = import_string.replace("salsa_lite.util.", package_name + ".util.");

			code += "import " + import_string + ";\n";

//            System.err.println("LOADING NEW: '" + import_string + "' FROM " + module_string + " " + getName());

            try {
                if (import_declaration.is_object) {
                    SymbolTable.importObject(import_string);
                    SymbolTable.addVariableType(import_declaration.import_string, import_declaration.import_string, false, true);

                } else if (import_declaration.is_enum) {
                    SymbolTable.importObject(import_string);
                    SymbolTable.addVariableType(import_declaration.import_string, import_declaration.import_string, false, true);

                } else SymbolTable.importActor(import_string);
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("[CCompilationUnit.getImportDeclarationCode] Could not find object, enum or actor to import. " + snfe.toString(), import_declaration);
                throw new RuntimeException(snfe);
            }
		}
		return code;
	}

	public String getFieldCode() {
		String code = "";
		for (int i = 0; i < getFields().size(); i++) {
            CLocalVariableDeclaration v = getFields().get(i);

            if (v.is_token) {
                CompilerErrors.printErrorMessage("Error declaring field(s). Currently, fields cannot be tokens.", v);
                continue;
            }

			for (int j = 0; j < getFields().get(i).variables.size(); j++) {
				CVariableInit vi = v.variables.get(j);

                try {
    				code += CIndent.getIndent() + v.type.name + " " + vi.toJavaCode(v.type.name, v.is_token) + ";\n";
				    SymbolTable.addVariableType(vi.name, v.type.name, v.is_token, false);
                } catch (SalsaNotFoundException snfe) {
                    CompilerErrors.printErrorMessage("[CCompilationUnit.getFieldCode] Could not find type for field. " + snfe.toString(), vi);
                    throw new RuntimeException(snfe);
                }
			}
		}
		return code;
	}

	public String getEnumerationCode() {
		String code = "";
        if (getEnumerations() != null) {
            for (int i = 0; i < getEnumerations().size(); i++) {
                code += CIndent.getIndent() + getEnumerations().get(i).toJavaCode() + "\n";
            }
        }
		return code;
	}

	public String getCaseInvocation(TypeSymbol[] parameterTypes) throws SalsaNotFoundException {
		String code = "(";
		for (int i = 0; i < parameterTypes.length; i++) {
            String typeString = parameterTypes[i].toNonPrimitiveString();
            typeString = TypeSymbol.removeBounds(typeString);

			code += " (" + typeString +")arguments[" + i +"]";
			if (i < parameterTypes.length - 1) code += ",";
			else code += " ";
		}

		return code + ")";
	}

	public String getInvokeMessageCode() {
		String code = "";

		code += CIndent.getIndent() + "public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {\n";
		CIndent.increaseIndent();
		code += CIndent.getIndent() + "switch(messageId) {\n";
		CIndent.increaseIndent();

		TypeSymbol typeSymbol = null;
        try {
            CName name = getName();
            typeSymbol = SymbolTable.getTypeSymbol(getName().name);
            if (!(typeSymbol instanceof ActorType)) {
                CompilerErrors.printErrorMessage("VERY BAD COMPILER ERROR [CCompilationUnit.getInvokeMessageCode]: Compiler thinks current actor is not an actor, thinks it is: " + typeSymbol.getLongSignature(), name);
            }
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("VERY BAD ERROR. Could not get type for actor being compiled. This should never happen.", behavior_declaration);
            throw new RuntimeException(snfe);
        }

        ActorType at = (ActorType)typeSymbol;
        int number_message_handlers = at.message_handlers.size();

        boolean overloads = false;
		for (int i = 0; i < at.message_handlers.size(); i++) {
			MessageSymbol sm = at.getMessageHandler(i);

            try {
                if (sm.isOverloadedByParent) {
                    if (overloads == false) {
                        overloads = true;
                        code += CIndent.getIndent() + "/* Superclasses Overloaded Message Handlers */\n";
                    }
                    code += CIndent.getIndent() + "case " + sm.getId() + ": return super.invokeMessage(messageId, arguments);\n";
                } else if (sm.getPassType().getName().equals("ack")) {
                    code += CIndent.getIndent() + "case " + sm.getId() + ": " + sm.getName() + getCaseInvocation(sm.parameterTypes) + "; return null;\n";
                } else {
                    code += CIndent.getIndent() + "case " + sm.getId() + ": return " + sm.getName() + getCaseInvocation(sm.parameterTypes) + ";\n";
                }
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("[CCompilationUnit.getInvokeMessageCode]: Error getting parameter types for message handler. " + snfe.toString(), getMessageHandlers().get(i));
                throw new RuntimeException(snfe);
            }
		}

        if (at.overloaded_message_handlers.size() > 0) {
            code += CIndent.getIndent() + "/* Overloaded Message handlers */\n";
            for (int j = 0; j < at.overloaded_message_handlers.size(); j++) {
                MessageSymbol sm = at.getOverloadedMessageHandler(j);

                try {
                    if (sm.getPassType().getName().equals("ack")) {
                        code += CIndent.getIndent() + "case " + sm.getId() + ": super." + sm.getName() + getCaseInvocation(sm.parameterTypes) + "; return null;\n";
                    } else {
                        code += CIndent.getIndent() + "case " + sm.getId() + ": return super." + sm.getName() + getCaseInvocation(sm.parameterTypes) + ";\n";
                    }
                } catch (SalsaNotFoundException snfe) {
                    CompilerErrors.printErrorMessage("[CCompilationUnit.getInvokeMessageCode]: Error getting parameter types for message handler. " + snfe.toString(), getMessageHandlers().get(j));
                    throw new RuntimeException(snfe);
                }
            }
        }

		code += CIndent.getIndent() + "default: throw new MessageHandlerNotFoundException(messageId, arguments);\n";
		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";

		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";
		return code;
	}

	public String getMessageCode() {
		String code = "";
		for (int i = 0; i < getMessageHandlers().size(); i++) {
			CMessageHandler mh = getMessageHandlers().get(i);
			code += mh.toJavaCode();
		}
		return code;
	}

	public String getInvokeConstructorCode() {
		String code = "";

		code += CIndent.getIndent() + "public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {\n";
		CIndent.increaseIndent();
		code += CIndent.getIndent() + "switch(messageId) {\n";
		CIndent.increaseIndent();

		TypeSymbol typeSymbol = null;
        try {
            CName name = getName();
            typeSymbol = SymbolTable.getTypeSymbol(name.name);
            if (!(typeSymbol instanceof ActorType)) {
                CompilerErrors.printErrorMessage("VERY BAD COMPILER ERROR [CCompilationUnit.getInvokeMessageCode]: Compiler thinks current actor is not an actor, thinks it is: " + typeSymbol.getLongSignature(), name);
            }
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("VERY BAD ERROR. Could not get type for actor being compiled. This should never happen.", behavior_declaration);
            throw new RuntimeException(snfe);
        }

        ActorType at = (ActorType)typeSymbol;

        boolean overloads = false;
		for (int i = 0; i < at.constructors.size(); i++) {
			ConstructorSymbol cm = at.getConstructor(i);

            try {
                if (cm.isOverloadedByParent) {
                    if (overloads == false) {
                        overloads = true;
                        code += CIndent.getIndent() + "/* Superclasses Overloaded Message Handlers */\n";
                    }
                    code += CIndent.getIndent() + "case " + cm.getId() + ": return super.invokeMessage(messageId, arguments);\n";
                } else {
    			    code += CIndent.getIndent() + "case " + cm.getId() + ": construct" + getCaseInvocation(cm.parameterTypes) + "; return;\n";
                }
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("[CCompilationUnit.getInvokeConstructorCode]: Error getting parameter types for constructor. " + snfe.toString(), getConstructors().get(i));
                throw new RuntimeException(snfe);
            }
		}

		code += CIndent.getIndent() + "default: throw new ConstructorNotFoundException(messageId, arguments);\n";
		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";

		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";
		return code;
	}

	public String getConstructorCode() {
		Vector<CConstructor> constructors = getConstructors();
		String code = "";
        if (interface_declaration == null && (constructors == null || constructors.size() == 0 || (constructors.size() == 1 && constructors.get(0).getArgumentTypes().length == 1 && constructors.get(0).getArgumentTypes()[0].equals("String[]")))) {
            //in this case we need to make a default constructor
            code += CIndent.getIndent() + "public void construct() {}\n\n";
        }
        if (constructors != null) {
            for (int i = 0; i < constructors.size(); i++) {
                code += constructors.get(i).toJavaCode(); 
            }
            code += "\n";
        } 
		return code;
	}

	public String getStateCode() {
		String code = "";

		if (module_string != null) {
			code += "package " + module_string + ";\n\n";
			SymbolTable.setCurrentModule(module_string + ".");
		}

		code += "/****** SALSA LANGUAGE IMPORTS ******/\n";
        code += "import salsa_lite.common.DeepCopy;\n";
        code += "import salsa_lite.runtime.ActorRegistry;\n";
        code += "import salsa_lite.runtime.Acknowledgement;\n";
        code += "import salsa_lite.runtime.SynchronousMailboxStage;\n";
        code += "import salsa_lite.runtime.Actor;\n";
        code += "import salsa_lite.runtime.Message;\n";
        code += "import salsa_lite.runtime.MobileActor;\n";
        code += "import salsa_lite.runtime.StageService;\n";
        code += "import salsa_lite.runtime.TransportService;\n";
        if (module_string == null || !module_string.equals("salsa_lite.runtime.language.")) {
            code += "import salsa_lite.runtime.language.Director;\n";
            code += "import salsa_lite.runtime.language.JoinDirector;\n";
            code += "import salsa_lite.runtime.language.MessageDirector;\n";
            code += "import salsa_lite.runtime.language.ContinuationDirector;\n";
            code += "import salsa_lite.runtime.language.TokenDirector;\n";
        }
        code += "\n";
        code += "import salsa_lite.runtime.language.exceptions.RemoteMessageException;\n";
        code += "import salsa_lite.runtime.language.exceptions.TokenPassException;\n";
        code += "import salsa_lite.runtime.language.exceptions.MessageHandlerNotFoundException;\n";
        code += "import salsa_lite.runtime.language.exceptions.ConstructorNotFoundException;\n";
        code += "\n";
		code += "/****** END SALSA LANGUAGE IMPORTS ******/\n";
		code += "\n";

		if (java_statement != null) code += java_statement.toJavaCode() + "\n";

		code += getImportDeclarationCode() + "\n";

        String actor_name = getName().name;
        try {
//            if (module_string == null) {
//                SymbolTable.importActor(actor_name);
//            } else {
//                SymbolTable.importActor(module_string + "." + actor_name);
//            }
            SymbolTable.addVariableType("self", actor_name, false, false);
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("VERY BAD ERROR. Could not get type for actor being compiled. This should never happen.", behavior_declaration);
            throw new RuntimeException(snfe);
        }
        if (behavior_declaration != null) {
            if (behavior_declaration.is_abstract) {
                code += "abstract public class " + actor_name;
            } else {
                code += "public class " + actor_name;
            }
        } else {
            code += "public interface " + actor_name;
        }

        if (getExtendsName() != null) {
            String extendsName = getExtendsName().name;
            code += " extends " + extendsName;
        }

		String implementsNames = null;
        if (behavior_declaration != null) implementsNames = behavior_declaration.getImplementsNames();
        else if (interface_declaration.extends_names.size() > 0) implementsNames = interface_declaration.getImplementsNames();

		if (implementsNames != null) {
            code += " implements " + implementsNames;
            
            if (behavior_declaration != null && !behavior_declaration.is_abstract) {
                code += ", java.io.Serializable";
            }
        }
        else {
            code += " implements java.io.Serializable";
        }

		code += " {\n";
		CIndent.increaseIndent();

        if (behavior_declaration != null && !behavior_declaration.is_abstract) {
            String tmp_name = actor_name;
            if (tmp_name.contains("<")) tmp_name = tmp_name.substring(0, tmp_name.indexOf('<'));

            code += "\n";
            code += CIndent.getIndent() + "public Object writeReplace() throws java.io.ObjectStreamException {\n";
            code += CIndent.getIndent() + "\tint hashCode = this.hashCode();\n";
            code += CIndent.getIndent() + "\tsynchronized (ActorRegistry.getLock(hashCode)) {\n";
            code += CIndent.getIndent() + "\t\tActorRegistry.addEntry(hashCode, this);\n";
            code += CIndent.getIndent() + "\t}\n";
            code += CIndent.getIndent() + "\treturn new Serialized" + tmp_name + "( this.hashCode(), TransportService.getHost(), TransportService.getPort() );\n";
            code += CIndent.getIndent() + "}\n\n";

            code += CIndent.getIndent() + "public static class " + tmp_name + "RemoteReference extends " + tmp_name + " {\n";
            code += CIndent.getIndent() + "\tint hashCode;\n";
            code += CIndent.getIndent() + "\tString host;\n";
            code += CIndent.getIndent() + "\tint port;\n";
            code += CIndent.getIndent() + "\t" + tmp_name + "RemoteReference(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }\n";
            code += "\n";
            code += CIndent.getIndent() + "\tpublic Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {\n";
            code += CIndent.getIndent() + "\t\tTransportService.sendMessage(host, port, this.stage.message);\n";
            code += CIndent.getIndent() + "\t\tthrow new RemoteMessageException();\n";
            code += CIndent.getIndent() + "\t}\n";
            code += "\n";
            code += CIndent.getIndent() + "\tpublic void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {\n";
            code += CIndent.getIndent() + "\t\tTransportService.sendMessage(host, port, this.stage.message);\n";
            code += CIndent.getIndent() + "\t\tthrow new RemoteMessageException();\n";
            code += CIndent.getIndent() + "\t}\n";
            code += "\n";
            code += CIndent.getIndent() + "\tpublic Object writeReplace() throws java.io.ObjectStreamException {\n";
            code += CIndent.getIndent() + "\t\treturn new Serialized" + tmp_name + "( this.hashCode(), TransportService.getHost(), TransportService.getPort() );\n";
            code += CIndent.getIndent() + "\t}\n";
            code += CIndent.getIndent() + "}\n\n";

            code += CIndent.getIndent() + "public static class Serialized" + tmp_name + " implements java.io.Serializable {\n";
            code += CIndent.getIndent() + "\tint hashCode;\n";
            code += CIndent.getIndent() + "\tString host;\n";
            code += CIndent.getIndent() + "\tint port;\n";
            code += "\n";
            code += CIndent.getIndent() + "\tSerialized" + tmp_name + "(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }\n";
            code += "\n";
            code += CIndent.getIndent() + "\tpublic Object readResolve() throws java.io.ObjectStreamException {\n";
            code += CIndent.getIndent() + "\t\tsynchronized (ActorRegistry.getLock(hashCode)) {\n";
            code += CIndent.getIndent() + "\t\t\t" + tmp_name + " actor = (" + tmp_name +")ActorRegistry.getEntry(hashCode);\n";
            code += CIndent.getIndent() + "\t\t\tif (actor == null) {\n";
            code += CIndent.getIndent() + "\t\t\t\tSystem.err.println(\"DESERIALIZING A REMOTE REFERENCE TO A LOCAL ACTOR\");\n";
            code += CIndent.getIndent() + "\t\t\t\t" + tmp_name + "RemoteReference remoteReference = new " + tmp_name + "RemoteReference(hashCode, host, port);\n";
            code += CIndent.getIndent() + "\t\t\t\tActorRegistry.addEntry(hashCode, remoteReference);\n";
            code += CIndent.getIndent() + "\t\t\t\treturn remoteReference;\n";
            code += CIndent.getIndent() + "\t\t\t} else {\n";
            code += CIndent.getIndent() + "\t\t\t\treturn actor;\n";
            code += CIndent.getIndent() + "\t\t\t}\n";
            code += CIndent.getIndent() + "\t\t}\n";
            code += CIndent.getIndent() + "\t}\n";
            code += CIndent.getIndent() + "}\n";
            code += "\n";
        }

//		System.err.println("ATTEMPTING TO ADD CONTAINED MESSAGE HANDLERS");
		Vector<CMessageHandler> containedMessageHandlers = new Vector<CMessageHandler>();
		int i = 0;
        if (getConstructors() != null) {
            for (CConstructor constructor : getConstructors()) {
    //			System.err.println("C : " + (i++));
                constructor.addContainedMessageHandlers(containedMessageHandlers);
            }
        }

		i = 0;
		for (CMessageHandler message_handler : getMessageHandlers()) {
//			System.err.println("MH : " + (i++));
			message_handler.addContainedMessageHandlers(containedMessageHandlers);
		}
//		System.err.println("FOUND " + containedMessageHandlers.size() + " CONTAINED MESSAGE HANDLERS");

		TypeSymbol self_type = null;
        try {
            self_type = SymbolTable.getVariableType("self");
            if (!(self_type instanceof ActorType)) {
                System.err.println("VERY BAD COMPILER ERROR [CCompilationUnit.getStateCode]: Compiler thinks current actor is not an actor, thinks it is: " + self_type.getLongSignature());
            }
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("VERY BAD ERROR. Could not get type for actor being compiled. This should never happen.", behavior_declaration);
            throw new RuntimeException(snfe);
        }

        ActorType at = (ActorType)self_type;

		int number_original_messages = getMessageHandlers().size();
		for (i = 0; i < containedMessageHandlers.size(); i++) {
            MessageSymbol ms = new MessageSymbol(i + number_original_messages, self_type, containedMessageHandlers.get(i));
            at.message_handlers.add( ms );
		}

        if (isMobile()) {
            String tmp_name = actor_name;
            if (tmp_name.contains("<")) tmp_name = tmp_name.substring(0, tmp_name.indexOf('<'));

            code += CIndent.getIndent() + "public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {\n";
            code += CIndent.getIndent() + "\tState state = (State)ActorRegistry.getEntry(this.hashCode());\n";
            code += CIndent.getIndent() + "\tif (state == null) {\n";
            code += CIndent.getIndent() + "\t\tTransportService.sendMessage(host, port, this.stage.message);\n";
            code += CIndent.getIndent() + "\t\tthrow new RemoteMessageException();\n";
            code += CIndent.getIndent() + "\t} else {\n";
            code += CIndent.getIndent() + "\t\treturn state.invokeMessage(messageId, arguments);\n";
            code += CIndent.getIndent() + "\t}\n";
            code += CIndent.getIndent() + "}\n";
            code += "\n";
            code += CIndent.getIndent() + "public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {\n";
            code += CIndent.getIndent() + "\tState state = (State)ActorRegistry.getEntry(this.hashCode());\n";
            code += CIndent.getIndent() + "\tif (state == null) {\n";
            code += CIndent.getIndent() + "\t\tTransportService.sendMessage(host, port, this.stage.message);\n";
            code += CIndent.getIndent() + "\t\tthrow new RemoteMessageException();\n";
            code += CIndent.getIndent() + "\t} else {\n";
            code += CIndent.getIndent() + "\t\tstate.invokeConstructor(messageId, arguments);\n";
            code += CIndent.getIndent() + "\t}\n";
            code += CIndent.getIndent() + "}\n\n\n";

            code += CIndent.getIndent() + "public " + tmp_name + "() { super(); }\n";
            code += CIndent.getIndent() + "public " + tmp_name + "(SynchronousMailboxStage stage) { super(stage); }\n\n";

            int act_constructor = behavior_declaration.getActConstructor();
            if (act_constructor >= 0) {
                code += CIndent.getIndent() + "public static void main(String[] arguments) {\n";
                code += CIndent.getIndent() + "\t" + tmp_name + ".construct(" + act_constructor + ", new Object[]{arguments});\n";
                code += CIndent.getIndent() + "}\n\n";
            }

            code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {\n";
            code += CIndent.getIndent() + "\t" + tmp_name + " actor = new " + tmp_name + "();\n";
            code += CIndent.getIndent() + "\tState state = new State(actor.stage);\n";
            code += CIndent.getIndent() + "\tActorRegistry.addEntry(actor.hashCode(), state);\n";
            code += CIndent.getIndent() + "\tTokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);\n";
            code += CIndent.getIndent() + "\tMessage input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);\n";
            code += CIndent.getIndent() + "\tMessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});\n";
            code += CIndent.getIndent() + "\treturn output_continuation;\n";
            code += CIndent.getIndent() + "}\n\n";

            code += CIndent.getIndent() + "public static " + tmp_name + " construct(int constructor_id, Object[] arguments) {\n";
            code += CIndent.getIndent() + "\t" + tmp_name + " actor = new " + tmp_name + "();\n";
            code += CIndent.getIndent() + "\tState state = new State(actor.stage);\n";
            code += CIndent.getIndent() + "\tActorRegistry.addEntry(actor.hashCode(), state);\n";
            code += CIndent.getIndent() + "\tStageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));\n";
            code += CIndent.getIndent() + "\treturn actor;\n";
            code += CIndent.getIndent() + "}\n";

            code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {\n";
            code += CIndent.getIndent() + "\t" + tmp_name + " actor = new " + tmp_name + "(target_stage);\n";
            code += CIndent.getIndent() + "\tState state = new State(target_stage);\n";
            code += CIndent.getIndent() + "\tActorRegistry.addEntry(actor.hashCode(), state);\n";
            code += CIndent.getIndent() + "\tTokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage);\n";
            code += CIndent.getIndent() + "\tMessage input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);\n";
            code += CIndent.getIndent() + "\tMessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage);\n";
            code += CIndent.getIndent() + "\treturn output_continuation;\n";
            code += CIndent.getIndent() + "}\n\n";

            code += CIndent.getIndent() + "public static " + tmp_name + " construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {\n";
            code += CIndent.getIndent() + "\t" + tmp_name + " actor = new " + tmp_name + "(target_stage);\n";
            code += CIndent.getIndent() + "\tState state = new State(target_stage);\n";
            code += CIndent.getIndent() + "\tActorRegistry.addEntry(actor.hashCode(), state);\n";
            code += CIndent.getIndent() + "\ttarget_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));\n";
            code += CIndent.getIndent() + "\treturn actor;\n";
            code += CIndent.getIndent() + "}\n\n\n";

            code += CIndent.getIndent() + "public static class State ";
            if (getExtendsName() != null) code += "extends " + getExtendsName().name + ".State ";
            code += "{\n";

            CIndent.increaseIndent();
        }

        if (actor_name.contains("<")) actor_name = actor_name.substring(0, actor_name.indexOf('<'));

        if (!isMobile()) {
            code += CIndent.getIndent() + "public " + actor_name + "() { super(); }\n";
            code += CIndent.getIndent() + "public " + actor_name + "(SynchronousMailboxStage stage) { super(stage); }\n\n";
        } else {
            code += CIndent.getIndent() + "public State() { super(); }\n";
            code += CIndent.getIndent() + "public State(SynchronousMailboxStage stage) { super(stage); }\n\n";
        }

        if (isMobile()) {
            code += CIndent.getIndent() + "public void migrate(String host, int port) {\n";
            code += CIndent.getIndent() + "}\n\n";
        }


		code += getFieldCode() + "\n";
		code += getEnumerationCode() + "\n";

        if (behavior_declaration != null) {
            code += getInvokeMessageCode() + "\n";
            if (!behavior_declaration.is_abstract) {
                code += getInvokeConstructorCode() + "\n";
            }
        }

        if (behavior_declaration != null && !behavior_declaration.is_abstract) {
    		code += getConstructorCode() + "\n";
        }
		code += getMessageCode() + "\n";

        if (behavior_declaration != null && !behavior_declaration.is_abstract) {
            if (!isMobile()) {
                int act_constructor = behavior_declaration.getActConstructor();
                if (act_constructor >= 0) {
                    code += CIndent.getIndent() + "public static void main(String[] arguments) {\n";
                    code += CIndent.getIndent() + "\t" + actor_name + ".construct(" + act_constructor + ", new Object[]{arguments});\n";
                    code += CIndent.getIndent() + "}\n\n";
                }

                code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {\n";
                code += CIndent.getIndent() + "\t" + actor_name + " actor = new " + actor_name + "();\n";
                code += CIndent.getIndent() + "\tTokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);\n";
                code += CIndent.getIndent() + "\tMessage input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);\n";
                code += CIndent.getIndent() + "\tMessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});\n";
                code += CIndent.getIndent() + "\treturn output_continuation;\n";
                code += CIndent.getIndent() + "}\n\n";

                code += CIndent.getIndent() + "public static " + actor_name + " construct(int constructor_id, Object[] arguments) {\n";
                code += CIndent.getIndent() + "\t" + actor_name + " actor = new " + actor_name + "();\n";
                code += CIndent.getIndent() + "\tStageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));\n";
                code += CIndent.getIndent() + "\treturn actor;\n";
                code += CIndent.getIndent() + "}\n";

                code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {\n";
                code += CIndent.getIndent() + "\t" + actor_name + " actor = new " + actor_name + "(target_stage);\n";
                code += CIndent.getIndent() + "\tTokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage);\n";
                code += CIndent.getIndent() + "\tMessage input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);\n";
                code += CIndent.getIndent() + "\tMessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage);\n";
                code += CIndent.getIndent() + "\treturn output_continuation;\n";
                code += CIndent.getIndent() + "}\n\n";

                code += CIndent.getIndent() + "public static " + actor_name + " construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {\n";
                code += CIndent.getIndent() + "\t" + actor_name + " actor = new " + actor_name + "(target_stage);\n";
                code += CIndent.getIndent() + "\ttarget_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));\n";
                code += CIndent.getIndent() + "\treturn actor;\n";
                code += CIndent.getIndent() + "}\n";
            }
        }

        if (isMobile()) {
            CIndent.decreaseIndent();
            code += CIndent.getIndent() + "}\n";
        }

		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";

		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
