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


public class A extends salsa_lite.local_fcs.LocalActor {


	public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			case 0: m1(); return null;
			case 1: m2(); return null;
			case 2: m3(); return null;
			default: throw new MessageHandlerNotFoundException(messageId, arguments);
		}
	}

	public void invokeConstructor(int messageId, Object[] arguments) throws ConstructorNotFoundException {
		switch(messageId) {
			case 0: construct(); return;
			default: throw new ConstructorNotFoundException(messageId, arguments);
		}
	}

	public void construct() {}



	public void m1() {
		System.out.println("A.m1()");
		StageService.sendMessage(this, 2 /*m3*/, null);
	}

	public void m2() {
		System.out.println("A.m2()");
	}

	public void m3() {
		System.out.println("A.m3()");
	}


	public A() { super(); }

	public A(SynchronousMailboxStage stage) { super(stage); }

	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
		A actor = new A();
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
		Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});
		return output_continuation;
	}

	public static A construct(int constructor_id, Object[] arguments) {
		A actor = new A();
		StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {
		A actor = new A(target_stage);
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage);
		Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage);
		return output_continuation;
	}

	public static A construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
		A actor = new A(target_stage);
		target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
}
