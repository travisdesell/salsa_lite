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


public class GenericCell<T extends Object> extends salsa_lite.local_fcs.LocalActor {
	T value;


	public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			case 0: return get();
			case 1: set( (T)arguments[0] ); return null;
			case 2: print(); return null;
			default: throw new MessageHandlerNotFoundException(messageId, arguments);
		}
	}

	public void invokeConstructor(int messageId, Object[] arguments) throws ConstructorNotFoundException {
		switch(messageId) {
			case 0: construct( (String[])arguments[0] ); return;
			case 1: construct( (T)arguments[0] ); return;
			default: throw new ConstructorNotFoundException(messageId, arguments);
		}
	}

	public void construct(String[] arguments) {
		GenericCell<String> gcs = GenericCell.construct(1, new Object[]{"string cell message"});
		GenericCell<Object> gco = GenericCell.construct(1, new Object[]{"object cell message"});
		ContinuationDirector continuation_token = StageService.sendContinuationMessage(gcs, 2 /*print*/, null);
		continuation_token = StageService.sendContinuationMessage(gco, 2 /*print*/, null, continuation_token);
		continuation_token = StageService.sendContinuationMessage(gco, 1 /*set*/, new Object[]{StageService.sendImplicitTokenMessage(gcs, 0 /*get*/, null, continuation_token)}, new int[]{0}, continuation_token);
		StageService.sendMessage(gco, 2 /*print*/, null, continuation_token);
	}

	public void construct(T value) {
		this.value = value;
	}



	public T get() {
		System.err.println("getting: " + value);
		return (T)DeepCopy.deepCopy( value );
	}

	public void set(T value) {
		System.err.println("setting: " + value);
		this.value = value;
	}

	public void print() {
		System.out.println(value);
	}


	public GenericCell() { super(); }

	public GenericCell(SynchronousMailboxStage stage) { super(stage); }

	public static void main(String[] arguments) {
		construct(0, new Object[]{arguments}, StageService.getStage(0));
	}

	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
		GenericCell actor = new GenericCell();
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
		Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});
		return output_continuation;
	}

	public static GenericCell construct(int constructor_id, Object[] arguments) {
		GenericCell actor = new GenericCell();
		StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {
		GenericCell actor = new GenericCell(target_stage);
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage);
		Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage);
		return output_continuation;
	}

	public static GenericCell construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
		GenericCell actor = new GenericCell(target_stage);
		target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
}
