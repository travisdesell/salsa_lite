package salsa.local_fcs.util;

/****** SALSA LANGUAGE IMPORTS ******/
import salsa.local_fcs.SynchronousMailboxStage;
import salsa.local_fcs.LocalActor;
import salsa.local_fcs.Message;
import salsa.local_fcs.StageService;
import salsa.local_fcs.language.JoinDirector;
import salsa.local_fcs.language.MessageDirector;
import salsa.local_fcs.language.ContinuationDirector;
import salsa.local_fcs.language.TokenDirector;

import salsa.local_fcs.language.exceptions.ContinuationPassException;
import salsa.local_fcs.language.exceptions.TokenPassException;
import salsa.local_fcs.language.exceptions.MessageHandlerNotFoundException;
import salsa.local_fcs.language.exceptions.ConstructorNotFoundException;

/****** END SALSA LANGUAGE IMPORTS ******/


public class StringUtil extends LocalActor {


	public Object invokeImmutableMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			case 0: return StringUtil.charAt( (String)arguments[0], (Integer)arguments[1] );
			case 1: return StringUtil.substring( (String)arguments[0], (Integer)arguments[1], (Integer)arguments[2] );
			case 2: return StringUtil.getNumericValue( (Character)arguments[0] );
			case 3: return StringUtil.lengthOf( (String)arguments[0] );
			case 4: return StringUtil.valueOf( (Integer)arguments[0] );
			case 5: return StringUtil.valueOf( (Long)arguments[0] );
			case 6: return StringUtil.valueOf( (Float)arguments[0] );
			case 7: return StringUtil.valueOf( (Double)arguments[0] );
			case 8: return StringUtil.valueOf( (Boolean)arguments[0] );
			case 9: return StringUtil.valueOf( (char[])arguments[0] );
			case 10: return StringUtil.valueOf( (char[])arguments[0], (Integer)arguments[1], (Integer)arguments[2] );
			default: throw new MessageHandlerNotFoundException(messageId, arguments);
		}
	}

	public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			case 0: return StringUtil.charAt( (String)arguments[0], (Integer)arguments[1] );
			case 1: return StringUtil.substring( (String)arguments[0], (Integer)arguments[1], (Integer)arguments[2] );
			case 2: return StringUtil.getNumericValue( (Character)arguments[0] );
			case 3: return StringUtil.lengthOf( (String)arguments[0] );
			case 4: return StringUtil.valueOf( (Integer)arguments[0] );
			case 5: return StringUtil.valueOf( (Long)arguments[0] );
			case 6: return StringUtil.valueOf( (Float)arguments[0] );
			case 7: return StringUtil.valueOf( (Double)arguments[0] );
			case 8: return StringUtil.valueOf( (Boolean)arguments[0] );
			case 9: return StringUtil.valueOf( (char[])arguments[0] );
			case 10: return StringUtil.valueOf( (char[])arguments[0], (Integer)arguments[1], (Integer)arguments[2] );
			default: throw new MessageHandlerNotFoundException(messageId, arguments);
		}
	}

	public void invokeConstructor(int messageId, Object[] arguments) throws ConstructorNotFoundException {
		switch(messageId) {
			default: throw new ConstructorNotFoundException(messageId, arguments);
		}
	}



	public static char charAt(String string, int position) {
		return string.charAt(position);

	}

	public static String substring(String s, int start, int finish) {
		return s.substring(start, finish);

	}

	public static int getNumericValue(char c) {
		return Character.getNumericValue(c);

	}

	public static int lengthOf(String s) {
		return s.length();

	}

	public static String valueOf(int i) {
		return String.valueOf(i);

	}

	public static String valueOf(long l) {
		return String.valueOf(l);

	}

	public static String valueOf(float f) {
		return String.valueOf(f);

	}

	public static String valueOf(double d) {
		return String.valueOf(d);

	}

	public static String valueOf(boolean b) {
		return String.valueOf(b);

	}

	public static String valueOf(char[] data) {
		return String.valueOf(data);

	}

	public static String valueOf(char[] data, int offset, int count) {
		return String.valueOf(data, offset, count);

	}


	public StringUtil() { super(); }

	public StringUtil(SynchronousMailboxStage stage) { super(stage); }

	public static StringUtil getImmutableReference(SynchronousMailboxStage stage) {
		return new StringUtil(stage);
	}

	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
		StringUtil actor = new StringUtil();
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

	public static StringUtil construct(int constructor_id, Object[] arguments) {
		StringUtil actor = new StringUtil();
		StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {
		StringUtil actor = new StringUtil(target_stage);
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

	public static StringUtil construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
		StringUtil actor = new StringUtil(target_stage);
		target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
}
