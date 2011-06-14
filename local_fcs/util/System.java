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


public class System extends LocalActor {


	public Object invokeImmutableMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			case 0: System.exit( (Integer)arguments[0] ); return null;
			case 1: return System.currentTimeMillis();
			default: throw new MessageHandlerNotFoundException(messageId, arguments);
		}
	}

	public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			case 0: System.exit( (Integer)arguments[0] ); return null;
			case 1: return System.currentTimeMillis();
			default: throw new MessageHandlerNotFoundException(messageId, arguments);
		}
	}

	public void invokeConstructor(int messageId, Object[] arguments) throws ConstructorNotFoundException {
		switch(messageId) {
			default: throw new ConstructorNotFoundException(messageId, arguments);
		}
	}



	public static void exit(int status) {
		java.lang.System.exit(0);

	}

	public static long currentTimeMillis() {
		return java.lang.System.currentTimeMillis();

	}


	public System() { super(); }

	public System(SynchronousMailboxStage stage) { super(stage); }

	public static System getImmutableReference(SynchronousMailboxStage stage) {
		return new System(stage);
	}

	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
		System actor = new System();
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

	public static System construct(int constructor_id, Object[] arguments) {
		System actor = new System();
		StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {
		System actor = new System(target_stage);
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

	public static System construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
		System actor = new System(target_stage);
		target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
}
