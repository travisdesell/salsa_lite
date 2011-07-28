package salsa_lite.runtime.language;

/****** SALSA LANGUAGE IMPORTS ******/
import salsa_lite.common.DeepCopy;
import salsa_lite.runtime.Acknowledgement;
import salsa_lite.runtime.SynchronousMailboxStage;
import salsa_lite.runtime.Actor;
import salsa_lite.runtime.Message;
import salsa_lite.runtime.StageService;
import salsa_lite.runtime.language.Director;
import salsa_lite.runtime.language.JoinDirector;
import salsa_lite.runtime.language.MessageDirector;
import salsa_lite.runtime.language.ContinuationDirector;
import salsa_lite.runtime.language.TokenDirector;

import salsa_lite.runtime.language.exceptions.ContinuationPassException;
import salsa_lite.runtime.language.exceptions.TokenPassException;
import salsa_lite.runtime.language.exceptions.MessageHandlerNotFoundException;
import salsa_lite.runtime.language.exceptions.ConstructorNotFoundException;

/****** END SALSA LANGUAGE IMPORTS ******/

import java.util.LinkedList;

public class ContinuationDirector extends Director {
	boolean unresolved = true;
	LinkedList<Message> messages = new LinkedList<Message>(  );
	ContinuationDirector currentContinuation = null;


	public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			case 0: resolve(); return null;
			case 1: setMessage( (Message)arguments[0] ); return null;
			case 2: forwardTo( (Director)arguments[0] ); return null;
			default: throw new MessageHandlerNotFoundException(messageId, arguments);
		}
	}

	public void invokeConstructor(int messageId, Object[] arguments) throws ConstructorNotFoundException {
		switch(messageId) {
			case 0: construct(); return;
			default: throw new ConstructorNotFoundException(messageId, arguments);
		}
	}

	public void construct() {
	}



	public void resolve() {
		if (unresolved) {
			unresolved = false;
		} 
		while (messages.size() > 0) {
			Message message = messages.removeFirst();
			StageService.sendMessage(message);
		}

		if (currentContinuation != null) {
			StageService.sendMessage(currentContinuation, 0 /*resolve*/, null);
		} 
	}

	public void setMessage(Message message) {
		if (unresolved) {
			messages.add(message);
		}
		else {
			StageService.sendMessage(message);
		}

	}

	public void forwardTo(Director director) {
		System.err.println("ContinuationDirector forwarding value to: " + director);
		if (unresolved) {
			currentContinuation = (ContinuationDirector)director;
		}
		else {
			StageService.sendMessage(((ContinuationDirector)director), 0 /*resolve*/, null);
		}

	}


	public ContinuationDirector() { super(); }

	public ContinuationDirector(SynchronousMailboxStage stage) { super(stage); }

	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
		ContinuationDirector actor = new ContinuationDirector();
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
		Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});
		return output_continuation;
	}

	public static ContinuationDirector construct(int constructor_id, Object[] arguments) {
		ContinuationDirector actor = new ContinuationDirector();
		StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {
		ContinuationDirector actor = new ContinuationDirector(target_stage);
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage);
		Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage);
		return output_continuation;
	}

	public static ContinuationDirector construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
		ContinuationDirector actor = new ContinuationDirector(target_stage);
		target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
}
