/****** SALSA LANGUAGE IMPORTS ******/
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

import salsa_lite.local_fcs.language.JoinDirector;
import java.lang.Integer;
import java.lang.System;

public class ThreadRing extends salsa_lite.local_fcs.LocalActor {
	ThreadRing next;
	int id;


	public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			case 0: setNextThread( (ThreadRing)arguments[0] ); return null;
			case 1: forwardMessage( (Integer)arguments[0] ); return null;
			default: throw new MessageHandlerNotFoundException(messageId, arguments);
		}
	}

	public void invokeConstructor(int messageId, Object[] arguments) throws ConstructorNotFoundException {
		switch(messageId) {
			case 0: construct( (Integer)arguments[0] ); return;
			case 1: construct( (java.lang.String[])arguments[0] ); return;
			default: throw new ConstructorNotFoundException(messageId, arguments);
		}
	}

	public void construct(int id) {
		this.id = id;
		System.out.println("created threadring: " + id);
	}

	public void construct(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java ThreadRing <threadCount> <hopCount>");
			return;
		}
		
		int threadCount = Integer.parseInt(args[0]);
		int hopCount = Integer.parseInt(args[1]);
		ThreadRing first = ThreadRing.construct(0, new Object[]{1});
		JoinDirector jd = JoinDirector.construct(0, null);
		ThreadRing next = null;
		ThreadRing previous = first;
		for (int i = 1; i < threadCount; i++) {
			next = ThreadRing.construct(0, new Object[]{i + 1});
			ContinuationDirector continuation_token = StageService.sendContinuationMessage(previous, 0 /*setNextThread*/, new Object[]{next});
			StageService.sendMessage(jd, 0 /*join*/, null, continuation_token);
			previous = next;
		}

		ContinuationDirector continuation_token = StageService.sendContinuationMessage(next, 0 /*setNextThread*/, new Object[]{first});
		StageService.sendMessage(jd, 0 /*join*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(jd, 1 /*resolveAfter*/, new Object[]{threadCount});
		StageService.sendMessage(first, 1 /*forwardMessage*/, new Object[]{hopCount}, continuation_token);
	}



	public void setNextThread(ThreadRing next) {
		this.next = next;
	}

	public void forwardMessage(int value) {
		if (value == 0) {
			System.out.println(id);
			System.exit(0);
		}
		else {
			value--;
			StageService.sendMessage(next, 1 /*forwardMessage*/, new Object[]{value});
		}

	}


	public ThreadRing() { super(); }

	public ThreadRing(SynchronousMailboxStage stage) { super(stage); }

	public static void main(String[] arguments) {
		construct(1, new Object[]{arguments}, StageService.getStage(0));
	}

	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
		ThreadRing actor = new ThreadRing();
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

	public static ThreadRing construct(int constructor_id, Object[] arguments) {
		ThreadRing actor = new ThreadRing();
		StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {
		ThreadRing actor = new ThreadRing(target_stage);
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

	public static ThreadRing construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
		ThreadRing actor = new ThreadRing(target_stage);
		target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
}
