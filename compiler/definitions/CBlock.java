package salsa_lite.compiler.definitions;

import salsa_lite.compiler.symbol_table.SymbolTable;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.SimpleNode;

import java.util.Vector;

public class CBlock extends CStatement {

	public Vector<CStatement> statements = new Vector<CStatement>();

	public void addContainedMessageHandlers(Vector<CMessageHandler> containedMessageHandlers) {
		for (CStatement statement : statements) {
			if (statement instanceof CBlock) {
				((CBlock)statement).addContainedMessageHandlers(containedMessageHandlers);
			} else if (statement instanceof CIfStatement) {
				((CIfStatement)statement).addContainedMessageHandlers(containedMessageHandlers);
			} else if (statement instanceof CSwitchStatement) {
				((CSwitchStatement)statement).addContainedMessageHandlers(containedMessageHandlers);
			}
		}
	}

	public String toJavaCode() {
		SymbolTable.messageContinues = continues;

		String join_director = "";
		String code = "";
		if (continues) {
			SymbolTable.newJoinDirector();
			join_director = SymbolTable.getJoinDirector();
			code += "ContinuationDirector " + join_director + "_continuation = ContinuationDirector.construct(0, null";
			if (System.getProperty("local_fcs") != null) code += ", this.stage";
			code += ");\n";
			code += CIndent.getIndent() + "JoinDirector " + join_director + " = JoinDirector.construct(0, new Object[]{" + join_director + "_continuation}";
			if (System.getProperty("local_fcs") != null) code += ", this.stage";
			code += ");\n";
			code += CIndent.getIndent() + "int " + join_director + "_message_count = 0;\n";
			code +=	CIndent.getIndent();
		}

		code += "{\n";
		CIndent.increaseIndent();

		SymbolTable.openScope();

		for (int i = 0; i < statements.size(); i++) {
			CStatement statement = statements.get(i);
			if (i+1 < statements.size()) {
				statement.next_statement = statements.get(i+1);
				if (statement.next_statement instanceof CPassStatement) {
//					System.err.println("NEXT STATEMENT IS A PASS STATEMENT");
					CPassStatement cps = (CPassStatement)(statement.next_statement);
					if (cps.expression == null) {
//						System.err.println("STATEMENT CONTINUES TO EMPTY PASS");
						SymbolTable.continuesToPass = true;
					}
					if (statement.continues) {
						cps.continuedFromToken = true;
					}
				}
			}

			if (statement instanceof CPassStatement) {
				code += CIndent.getIndent() + statement.toJavaCode() + "\n";
			} else if (statement instanceof CForStatement) {
				code += CIndent.getIndent() + statement.toJavaCode() + "\n";
			} else if (statement instanceof CWhileStatement) {
				code += CIndent.getIndent() + statement.toJavaCode() + "\n";
			} else if (statement instanceof CIfStatement) {
				code += CIndent.getIndent() + statement.toJavaCode() + "\n";
			} else if (statement instanceof CBlock) {
				code += CIndent.getIndent() + statement.toJavaCode() + "\n";
			} else if (statement instanceof CLocalVariableDeclaration) {
				code += CIndent.getIndent() + statement.toJavaCode() + "\n";
			} else if (statement instanceof CStatementExpression) {
				code += CIndent.getIndent() + statement.toJavaCode() + ";\n";
			} else if (statement instanceof CJavaStatement) {
				code += CIndent.getIndent() + statement.toJavaCode() + "\n";
			} else if (statement instanceof CSwitchStatement) {
				code += CIndent.getIndent() + statement.toJavaCode() + "\n";
			} else if (statement instanceof CCaseStatement) {
				code += CIndent.getIndent() + statement.toJavaCode() + "\n";
			} else if (statement instanceof CLabelStatement) {
				code += CIndent.getIndent() + statement.toJavaCode() + "\n";
			} else if (statement instanceof CBreakStatement) {
				code += CIndent.getIndent() + statement.toJavaCode() + "\n";
			}

			if (statement.next_statement != null && statement.next_statement instanceof CPassStatement) {
				CPassStatement cps = (CPassStatement)(statement.next_statement);
				if (cps.expression == null) SymbolTable.continuesToPass = false;
			}
		}

		SymbolTable.closeScope();
		
        SymbolTable.messageRequiresContinuation = continues;

		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";

		if (continues) {
			code += CIndent.getIndent() + "StageService.sendMessage(" + join_director + ", 1 /*resolveAfter*/, new Object[]{" + join_director + "_message_count});\n";
			code += CIndent.getIndent();
			if (SymbolTable.firstContinuation()) {
				code += "ContinuationDirector ";
				SymbolTable.initializedFirstContinuation();
			}
			code += "continuation_token = " + join_director + "_continuation;\n";
			SymbolTable.closeJoinDirector();
		}

		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
