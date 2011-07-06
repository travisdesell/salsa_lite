package salsa_lite.local_fcs.language;

/****** SALSA LANGUAGE IMPORTS ******/
import salsa_lite.common.DeepCopy;
import salsa_lite.local_fcs.Acknowledgement;
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

import java.util.LinkedList;

public class ContinuationDirector extends salsa_lite.local_fcs.LocalActor {
	boolean unresolved = true;
	LinkedList<Message> messages = new LinkedList<Message>(  );


	public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			case 0: resolve(); return null;
			case 1: setMessage( (Message)arguments[0] ); return null;
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
		if (messages.size() > 0) {
			for (Message message : messages){
				StageService.sendMessage(message);
			}

		}
		else {
			unresolved = false;
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
