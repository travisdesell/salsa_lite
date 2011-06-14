package salsa_lite.examples.fibonacci;

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


public class Fibonacci extends salsa_lite.local_fcs.LocalActor {
	int n;


	public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			case 0: return compute();
			case 1: finish( (Integer)arguments[0] ); return null;
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

	public void construct(int n) {
		this.n = n;
	}

	public void construct(String[] arguments) {
		n = Integer.parseInt(arguments[0]);
		StageService.sendMessage(this, 1 /*finish*/, new Object[]{StageService.sendImplicitTokenMessage(this, 0 /*compute*/, null)}, new int[]{0});
	}



	public int compute() throws TokenPassException {
		if (n == 0) {
			return 0;
		} else if (n <= 2) {
			return 1;
		} else {
			class ExpressionDirector1 extends LocalActor {
				public ExpressionDirector1(SynchronousMailboxStage stage) { super(stage); }
				public void invokeConstructor(int id, Object[] arguments) {}
				public Object invokeMessage(int messageId, Object[] arguments) {
					return (Integer)arguments[0] + (Integer)arguments[1];
				}
			}
			StageService.sendPassMessage(new ExpressionDirector1(this.stage), 0, new Object[]{StageService.sendImplicitTokenMessage(Fibonacci.construct(0, new Object[]{n - 1}), 0 /*compute*/, null), StageService.sendImplicitTokenMessage(Fibonacci.construct(0, new Object[]{n - 2}), 0 /*compute*/, null)}, new int[]{0, 1}, this.stage.message.continuationDirector);
			throw new TokenPassException();
		}
	}

	public void finish(int value) {
		System.out.println(value);
		System.exit(0);
	}


	public Fibonacci() { super(); }

	public Fibonacci(SynchronousMailboxStage stage) { super(stage); }

	public static void main(String[] arguments) {
		construct(1, new Object[]{arguments}, StageService.getStage(0));
	}

	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
		Fibonacci actor = new Fibonacci();
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

	public static Fibonacci construct(int constructor_id, Object[] arguments) {
		Fibonacci actor = new Fibonacci();
		StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {
		Fibonacci actor = new Fibonacci(target_stage);
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

	public static Fibonacci construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
		Fibonacci actor = new Fibonacci(target_stage);
		target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
}
