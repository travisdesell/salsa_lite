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

import salsa_lite.local_fcs.io.StandardOutput;

public class InheritanceTest extends salsa_lite.local_fcs.LocalActor {


	public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			default: throw new MessageHandlerNotFoundException(messageId, arguments);
		}
	}

	public void invokeConstructor(int messageId, Object[] arguments) throws ConstructorNotFoundException {
		switch(messageId) {
			case 0: construct( (String[])arguments[0] ); return;
			case 1: construct(); return;
			default: throw new ConstructorNotFoundException(messageId, arguments);
		}
	}

	public void construct() {}

	public void construct(String[] arguments) {
		A a = A.construct(0, null);
		B b = B.construct(0, null);
		C c = C.construct(0, null);
		A c_as_a = C.construct(0, null);
		StandardOutput standardOutput = StandardOutput.construct(0, null);
		ContinuationDirector continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"a<-m1()"});
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Should print out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"A.m1()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"A.m3()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Printing out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(a, 0 /*m1*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 20 /*println*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"a<-m2()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Should print out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"A.m2()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Printing out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(a, 1 /*m2*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 20 /*println*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"a<-m3()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Should print out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"A.m3()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Printing out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(a, 2 /*m3*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 20 /*println*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"b<-m1()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Should print out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"A.m1()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"A.m3()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Printing out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(b, 0 /*m1*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 20 /*println*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"b<-m2()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Should print out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"B.m2()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"A.m2()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Printing out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(b, 1 /*m2*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 20 /*println*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"b<-m3()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Should print out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"A.m3()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Printing out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(b, 2 /*m3*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 20 /*println*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"c<-m1()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Should print out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"A.m1()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"C.m3()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Printing out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(c, 0 /*m1*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 20 /*println*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"c<-m2()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Should print out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"C.m2()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"B.m2()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"A.m2()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Printing out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(c, 1 /*m2*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 20 /*println*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"c<-m3()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Should print out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"C.m3()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Printing out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(c, 2 /*m3*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 20 /*println*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"c_as_a<-m1()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Should print out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"A.m1()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"C.m3()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Printing out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(c_as_a, 0 /*m1*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 20 /*println*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"c_as_a<-m2()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Should print out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"C.m2()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"B.m2()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"A.m2()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Printing out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(c_as_a, 1 /*m2*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 20 /*println*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"c_as_a<-m3()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Should print out:"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"C.m3()"}, continuation_token);
		continuation_token = StageService.sendContinuationMessage(standardOutput, 10 /*println*/, new Object[]{"Printing out:"}, continuation_token);
		StageService.sendMessage(c_as_a, 2 /*m3*/, null, continuation_token);
	}




	public InheritanceTest() { super(); }

	public InheritanceTest(SynchronousMailboxStage stage) { super(stage); }

	public static void main(String[] arguments) {
		construct(0, new Object[]{arguments}, StageService.getStage(0));
	}

	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
		InheritanceTest actor = new InheritanceTest();
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
		Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});
		return output_continuation;
	}

	public static InheritanceTest construct(int constructor_id, Object[] arguments) {
		InheritanceTest actor = new InheritanceTest();
		StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {
		InheritanceTest actor = new InheritanceTest(target_stage);
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage);
		Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage);
		return output_continuation;
	}

	public static InheritanceTest construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
		InheritanceTest actor = new InheritanceTest(target_stage);
		target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
}
