package salsa_lite.local_fcs.io;

/****** SALSA LANGUAGE IMPORTS ******/
import salsa_lite.local_fcs.SynchronousMailboxStage;
import salsa_lite.local_fcs.LocalActor;
import salsa_lite.local_fcs.Message;
import salsa_lite.local_fcs.StageService;
import salsa_lite.local_fcs.language.JoinDirector;
import salsa_lite.local_fcs.language.MessageDirector;
import salsa_lite.local_fcs.language.ContinuationDirector;
import salsa_lite.local_fcs.language.TokenDirector;

import salsa_lite.local_fcs.language.exceptions.ContinuationPassException;
import salsa_lite.local_fcs.language.exceptions.TokenPassException;
import salsa_lite.local_fcs.language.exceptions.MessageHandlerNotFoundException;
import salsa_lite.local_fcs.language.exceptions.ConstructorNotFoundException;

/****** END SALSA LANGUAGE IMPORTS ******/

import java.lang.System;

public class StandardOutput extends LocalActor {


	public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			case 0: print( (String)arguments[0] ); return null;
			case 1: print( (Object)arguments[0] ); return null;
			case 2: print( (Boolean)arguments[0] ); return null;
			case 3: print( (Character)arguments[0] ); return null;
			case 4: print( (Byte)arguments[0] ); return null;
			case 5: print( (Short)arguments[0] ); return null;
			case 6: print( (Integer)arguments[0] ); return null;
			case 7: print( (Long)arguments[0] ); return null;
			case 8: print( (Float)arguments[0] ); return null;
			case 9: print( (Double)arguments[0] ); return null;
			case 10: println( (String)arguments[0] ); return null;
			case 11: println( (Object)arguments[0] ); return null;
			case 12: println( (Boolean)arguments[0] ); return null;
			case 13: println( (Character)arguments[0] ); return null;
			case 14: println( (Byte)arguments[0] ); return null;
			case 15: println( (Short)arguments[0] ); return null;
			case 16: println( (Integer)arguments[0] ); return null;
			case 17: println( (Long)arguments[0] ); return null;
			case 18: println( (Float)arguments[0] ); return null;
			case 19: println( (Double)arguments[0] ); return null;
			case 20: println(); return null;
			default: throw new MessageHandlerNotFoundException(messageId, arguments);
		}
	}

	public void invokeConstructor(int messageId, Object[] arguments) throws ConstructorNotFoundException {
		switch(messageId) {
			default: throw new ConstructorNotFoundException(messageId, arguments);
		}
	}



	public void print(String s) {
		System.out.print(s);
	}

	public void print(Object p) {
		System.out.print(p);
	}

	public void print(boolean p) {
		System.out.print(p);
	}

	public void print(char p) {
		System.out.print(p);
	}

	public void print(byte p) {
		System.out.print(p);
	}

	public void print(short p) {
		System.out.print(p);
	}

	public void print(int p) {
		System.out.print(p);
	}

	public void print(long p) {
		System.out.print(p);
	}

	public void print(float p) {
		System.out.print(p);
	}

	public void print(double p) {
		System.out.print(p);
	}

	public void println(String s) {
		System.out.println(s);
	}

	public void println(Object p) {
		System.out.println(p);
	}

	public void println(boolean p) {
		System.out.println(p);
	}

	public void println(char p) {
		System.out.println(p);
	}

	public void println(byte p) {
		System.out.println(p);
	}

	public void println(short p) {
		System.out.println(p);
	}

	public void println(int p) {
		System.out.println(p);
	}

	public void println(long p) {
		System.out.println(p);
	}

	public void println(float p) {
		System.out.println(p);
	}

	public void println(double p) {
		System.out.println(p);
	}

	public void println() {
		System.out.println();
	}


	public StandardOutput() { super(); }

	public StandardOutput(SynchronousMailboxStage stage) { super(stage); }

	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
		StandardOutput actor = new StandardOutput();
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
		Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length});
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
		return output_continuation;
	}

	public static StandardOutput construct(int constructor_id, Object[] arguments) {
		StandardOutput actor = new StandardOutput();
		StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {
		StandardOutput actor = new StandardOutput(target_stage);
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage);
		Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length}, target_stage);
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			argument_token.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
		return output_continuation;
	}

	public static StandardOutput construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
		StandardOutput actor = new StandardOutput(target_stage);
		target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
}
