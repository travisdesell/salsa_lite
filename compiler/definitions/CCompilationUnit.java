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
		else return interface_declaration.extends_name;
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
                    SymbolTable.loadObject(import_string);
                    SymbolTable.addVariableType(import_declaration.import_string, import_declaration.import_string, false, true);

                } else if (import_declaration.is_enum) {
                    SymbolTable.loadObject(import_string);
                    SymbolTable.addVariableType(import_declaration.import_string, import_declaration.import_string, false, true);

                } else SymbolTable.loadActor(import_string);
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
    				code += CIndent.getIndent() + v.type.name + " " + vi.toJavaCode() + ";\n";
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
		for (int i = 0; i < getEnumerations().size(); i++) {
			code += CIndent.getIndent() + getEnumerations().get(i).toJavaCode() + "\n";
		}
		return code;
	}

	public String getCaseInvocation(TypeSymbol[] parameterTypes) throws SalsaNotFoundException {
		String code = "(";
		for (int i = 0; i < parameterTypes.length; i++) {
            System.err.println("parameter[" + i + "].toNonPrimitiveString(): " + parameterTypes[i].toNonPrimitiveString() + ", " + parameterTypes[i].getClass().getName());

			code += " (" + parameterTypes[i].toNonPrimitiveString() +")arguments[" + i +"]";
			if (i < parameterTypes.length - 1) code += ",";
			else code += " ";
		}

		return code + ")";
	}

	public String getInvokeMessageCode() {
		String code = "";

		code += CIndent.getIndent() + "public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {\n";
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

		for (int i = 0; i < at.message_handlers.size(); i++) {
			MessageSymbol sm = at.getMessageHandler(i);

            try {
                if (sm.getPassType().getName().equals("ack")) {
                    code += CIndent.getIndent() + "case " + sm.getId() + ": " + sm.getName() + getCaseInvocation(sm.parameterTypes) + "; return null;\n";
                } else {
                    code += CIndent.getIndent() + "case " + sm.getId() + ": return " + sm.getName() + getCaseInvocation(sm.parameterTypes) + ";\n";
                }
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("[CCompilationUnit.getInvokeMessageCode]: Error getting parameter types for message handler. " + snfe.toString(), getMessageHandlers().get(i));
                throw new RuntimeException(snfe);
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

		code += CIndent.getIndent() + "public void invokeConstructor(int messageId, Object[] arguments) throws ConstructorNotFoundException {\n";
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

		for (int i = 0; i < at.constructors.size(); i++) {
			ConstructorSymbol cm = at.getConstructor(i);

            try {
			    code += CIndent.getIndent() + "case " + cm.getId() + ": construct" + getCaseInvocation(cm.parameterTypes) + "; return;\n";
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
		for (int i = 0; i < constructors.size(); i++) {
			code += constructors.get(i).toJavaCode(); 
		}
		code += "\n";
		return code;
	}

	public String getReferenceCode() {
		int place = 0;

		String code = "";
		if (module_string != null) {
			code += "package " + module_string + ";\n\n";
			SymbolTable.setCurrentModule(module_string + ".");
		}

		code += "// Import declarations generated by the SALSA compiler.\n";
		code += "\n";
        code += "import salsa_lite.common.DeepCopy;\n";
		if (System.getProperty("wwc") != null) {
			code += "import salsa_lite.wwc.WWCActorReference;\n";
			code += "import salsa_lite.wwc.WWCActorState;\n";
			code += "import salsa_lite.wwc.StageService;\n";
			code += "import salsa_lite.wwc.language.exceptions.ConstructorNotFoundException;\n";
			code += "import salsa_lite.wwc.language.exceptions.MessageHandlerNotFoundException;\n";
			code += "import salsa_lite.wwc.language.exceptions.ContinuationPassException;\n";
			code += "import salsa_lite.wwc.language.exceptions.TokenPassException;\n";
		} else if (System.getProperty("local") != null) {
			code += "import salsa_lite.local.LocalActorReference;\n";
			code += "import salsa_lite.local.LocalActorState;\n";
			code += "import salsa_lite.local.StageService;\n";
			code += "import salsa_lite.local.language.exceptions.ConstructorNotFoundException;\n";
			code += "import salsa_lite.local.language.exceptions.MessageHandlerNotFoundException;\n";
			code += "import salsa_lite.local.language.exceptions.ContinuationPassException;\n";
			code += "import salsa_lite.local.language.exceptions.TokenPassException;\n";
		}
		code += "\n";
		code += "// End SALSA compiler generated import delcarations.\n\n";

		if (java_statement != null) code += java_statement.toJavaCode() + "\n";

		code += getImportDeclarationCode();

		String actor_name = getName().name;
        try {
            if (module_string == null) {
                SymbolTable.loadActor(actor_name);
            } else {
                SymbolTable.loadActor(module_string + "." + actor_name);
            }
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("VERY BAD ERROR. Could not get type for actor being compiled. This should never happen.", behavior_declaration);
            throw new RuntimeException(snfe);
        }

		String extendsName = getExtendsName().name;
		if (System.getProperty("local") != null || System.getProperty("wwc") != null) {
			if (extendsName.equals("LocalActor") || extendsName.equals("WWCActor")) {
				extendsName += "Reference";
			}
		}

		code += "public class " + actor_name;

		code += " extends " + extendsName;

		String implementsNames = behavior_declaration.getImplementsNames();
		if (implementsNames != null) code += " implements " + implementsNames;

		code += " {\n";

		CIndent.increaseIndent();
		/********
		 * Create constructors which will create references.
		 ********/
		if (System.getProperty("local") != null) {
			code += CIndent.getIndent() + "public " + actor_name + "(long identifier) { super(identifier); }\n\n";
		} else if (System.getProperty("wwc") != null) {
			code += CIndent.getIndent() + "public " + actor_name + "(String identifier) { super(identifier); }\n\n";
		}

		if (behavior_declaration != null) {
			/********
			 * Create this actor then send a message which will invoke construct
			 * on the actor.
			 ********/

			if (System.getProperty("local") != null) {
				code += CIndent.getIndent() + "public static " + actor_name + " construct(int construct_message_id, Object[] arguments) {\n";
				code += CIndent.getIndent() + "\t" + actor_name + "State actor = new " + actor_name + "State(StageService.generateUniqueId());\n";
				code += CIndent.getIndent() + "\t" + actor_name + " target = new " + actor_name + "(actor.getId());\n";
				code += CIndent.getIndent() + "\tactor.self = target;\n";
				code += CIndent.getIndent() + "\tStageService.createActor(actor, construct_message_id, arguments);\n";
				code += CIndent.getIndent() + "\treturn target;\n";
				code += CIndent.getIndent() + "}\n\n";
			} else if (System.getProperty("wwc") != null) {
				code += CIndent.getIndent() + "public static " + actor_name + " construct(int construct_message_id, Object[] arguments) {\n";
				code += CIndent.getIndent() + "\t" + actor_name + "State actor = new " + actor_name + "State(StageService.generateUniqueId());\n";
				code += CIndent.getIndent() + "\t" + actor_name + " target = new " + actor_name + "(actor.getUniqueId());\n";
				code += CIndent.getIndent() + "\tactor.self = target;\n";
				code += CIndent.getIndent() + "\tStageService.createActor(actor, construct_message_id, arguments);\n";
				code += CIndent.getIndent() + "\treturn target;\n";
				code += CIndent.getIndent() + "}\n\n";
			}

			/********
			 * Create this actor then send a message which will invoke act on
			 * the actor.
			 ********/
			int act_constructor = behavior_declaration.getActConstructor();
			if (act_constructor >= 0) {
				code += CIndent.getIndent() + "public static void main(String[] arguments) {\n";
				code += CIndent.getIndent() + "\tconstruct(" + act_constructor + ", new Object[]{arguments});\n";
				code += CIndent.getIndent() + "}\n";
			}
		}
		CIndent.decreaseIndent();
		code += "}\n";
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
		if (System.getProperty("local_fcs") != null) {
			code += "import salsa_lite.local_fcs.Acknowledgement;\n";
			code += "import salsa_lite.local_fcs.SynchronousMailboxStage;\n";
			code += "import salsa_lite.local_fcs.LocalActor;\n";
			code += "import salsa_lite.local_fcs.Message;\n";
			code += "import salsa_lite.local_fcs.StageService;\n";
            if (module_string == null || !module_string.equals("salsa_lite.local_fcs.language.")) {
                code += "import salsa_lite.local_fcs.language.JoinDirector;\n";
                code += "import salsa_lite.local_fcs.language.MessageDirector;\n";
                code += "import salsa_lite.local_fcs.language.ContinuationDirector;\n";
                code += "import salsa_lite.local_fcs.language.TokenDirector;\n";
            }
			code += "\n";
			code += "import salsa_lite.local_fcs.language.exceptions.ContinuationPassException;\n";
			code += "import salsa_lite.local_fcs.language.exceptions.TokenPassException;\n";
			code += "import salsa_lite.local_fcs.language.exceptions.MessageHandlerNotFoundException;\n";
			code += "import salsa_lite.local_fcs.language.exceptions.ConstructorNotFoundException;\n";
			code += "\n";

		} else if (System.getProperty("wwc") != null) {
			code += "import salsa_lite.wwc.WWCActorReference;\n";
			code += "import salsa_lite.wwc.WWCActorState;\n";
			code += "import salsa_lite.wwc.Message;\n";
			code += "import salsa_lite.wwc.StageService;\n";
			code += "import salsa_lite.wwc.language.JoinDirector;\n";
			code += "import salsa_lite.wwc.language.ContinuationDirector;\n";
			code += "import salsa_lite.wwc.language.TokenDirector;\n";
			code += "\n";
			code += "import salsa_lite.wwc.language.exceptions.ContinuationPassException;\n";
			code += "import salsa_lite.wwc.language.exceptions.TokenPassException;\n";
			code += "import salsa_lite.wwc.language.exceptions.MessageHandlerNotFoundException;\n";
			code += "import salsa_lite.wwc.language.exceptions.ConstructorNotFoundException;\n";
			code += "\n";
		}


		code += "/****** END SALSA LANGUAGE IMPORTS ******/\n";
		code += "\n";

		if (java_statement != null) code += java_statement.toJavaCode() + "\n";

		code += getImportDeclarationCode() + "\n";

		String extendsName = getExtendsName().name;

        String actor_name = getName().name;
        try {
            if (module_string == null) {
                SymbolTable.loadActor(actor_name);
            } else {
                SymbolTable.loadActor(module_string + "." + actor_name);
            }
            SymbolTable.addVariableType("self", actor_name, false, false);
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("VERY BAD ERROR. Could not get type for actor being compiled. This should never happen.", behavior_declaration);
            throw new RuntimeException(snfe);
        }

		if (System.getProperty("local") != null || System.getProperty("wwc") != null) {
			actor_name += "State";
			extendsName += "State";
		}

		code += "public class " + actor_name;

		code += " extends " + extendsName;

		String implementsNames = behavior_declaration.getImplementsNames();
		if (implementsNames != null) code += " implements " + implementsNames;

		code += " {\n";
		CIndent.increaseIndent();


		if (behavior_declaration != null && behavior_declaration.java_statement != null) code += CIndent.getIndent() + behavior_declaration.java_statement.toJavaCode() + "\n";

//		System.err.println("ATTEMPTING TO ADD CONTAINED MESSAGE HANDLERS");
		Vector<CMessageHandler> containedMessageHandlers = new Vector<CMessageHandler>();
		int i = 0;
		for (CConstructor constructor : getConstructors()) {
//			System.err.println("C : " + (i++));
			constructor.addContainedMessageHandlers(containedMessageHandlers);
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
            at.message_handlers.put( ms.getLongSignature(), ms );
		}

		code += getFieldCode() + "\n";
		code += getEnumerationCode() + "\n";

		code += getInvokeMessageCode() + "\n";
		code += getInvokeConstructorCode() + "\n";

		code += getConstructorCode() + "\n";
		code += getMessageCode() + "\n";

        if (actor_name.contains("<")) actor_name = actor_name.substring(0, actor_name.indexOf('<'));

		if (System.getProperty("local") != null) {
			code += CIndent.getIndent() + "public " + actor_name + "(long identifier) { super(identifier); }\n\n";
		} else if (System.getProperty("wwc") != null) {
			code += CIndent.getIndent() + "public " + actor_name + "(String identifier) { super(identifier); }\n\n";
		} else if (System.getProperty("local_fcs") != null) {
			code += CIndent.getIndent() + "public " + actor_name + "() { super(); }\n\n";
			code += CIndent.getIndent() + "public " + actor_name + "(SynchronousMailboxStage stage) { super(stage); }\n\n";
		}

		if (System.getProperty("local_noref") != null || System.getProperty("local_fcs") != null) {
			if (behavior_declaration != null) {
				int act_constructor = behavior_declaration.getActConstructor();
				if (act_constructor >= 0) {
					code += CIndent.getIndent() + "public static void main(String[] arguments) {\n";
					if (System.getProperty("local_fcs") != null) {
						code += CIndent.getIndent() + "\tconstruct(" + act_constructor + ", new Object[]{arguments}, StageService.getStage(0));\n";
					} else {
						code += CIndent.getIndent() + "\tStageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, new " + actor_name + "(), " + act_constructor + ", new Object[]{arguments}));\n";
					}
					code += CIndent.getIndent() + "}\n\n";
				}
			}

//			if (System.getProperty("local_fcs") == null) {
				code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {\n";
				code += CIndent.getIndent() + "\t" + actor_name + " actor = new " + actor_name + "();\n";
				code += CIndent.getIndent() + "\tTokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);\n";
				code += CIndent.getIndent() + "\tMessage input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);\n";
				code += CIndent.getIndent() + "\tMessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length});\n";
				code += CIndent.getIndent() + "\tTokenDirector argument_token;\n";
				code += CIndent.getIndent() + "\tfor (int i = 0; i < token_positions.length; i++) {\n";
				code += CIndent.getIndent() + "\t\targument_token = (TokenDirector)arguments[token_positions[i]];\n";
				code += CIndent.getIndent() + "\t\tStageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));\n";
				code += CIndent.getIndent() + "\t}\n";
				code += CIndent.getIndent() + "\treturn output_continuation;\n";
				code += CIndent.getIndent() + "}\n\n";

				code += CIndent.getIndent() + "public static " + actor_name + " construct(int constructor_id, Object[] arguments) {\n";
				code += CIndent.getIndent() + "\t" + actor_name + " actor = new " + actor_name + "();\n";
				code += CIndent.getIndent() + "\tStageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));\n";
				code += CIndent.getIndent() + "\treturn actor;\n";
				code += CIndent.getIndent() + "}\n";
//			}

			if (System.getProperty("local_fcs") != null) {
				code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {\n";
				code += CIndent.getIndent() + "\t" + actor_name + " actor = new " + actor_name + "(target_stage);\n";
				code += CIndent.getIndent() + "\tTokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage);\n";
				code += CIndent.getIndent() + "\tMessage input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);\n";
				code += CIndent.getIndent() + "\tMessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length}, target_stage);\n";
				code += CIndent.getIndent() + "\tTokenDirector argument_token;\n";
				code += CIndent.getIndent() + "\tfor (int i = 0; i < token_positions.length; i++) {\n";
				code += CIndent.getIndent() + "\t\targument_token = (TokenDirector)arguments[token_positions[i]];\n";
				code += CIndent.getIndent() + "\t\targument_token.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));\n";
				code += CIndent.getIndent() + "\t}\n";
				code += CIndent.getIndent() + "\treturn output_continuation;\n";
				code += CIndent.getIndent() + "}\n\n";

				code += CIndent.getIndent() + "public static " + actor_name + " construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {\n";
				code += CIndent.getIndent() + "\t" + actor_name + " actor = new " + actor_name + "(target_stage);\n";
				code += CIndent.getIndent() + "\ttarget_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));\n";
				code += CIndent.getIndent() + "\treturn actor;\n";
				code += CIndent.getIndent() + "}\n";
			}
		}
	
		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";

		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
