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


public class FibonacciSimple extends salsa_lite.local_fcs.LocalActor {
	int n;
	int result = 0;
	int received = 0;
	FibonacciSimple sender;


	public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			case 0: setResult( (Integer)arguments[0] ); return null;
			case 1: compute(); return null;
			default: throw new MessageHandlerNotFoundException(messageId, arguments);
		}
	}

	public void invokeConstructor(int messageId, Object[] arguments) throws ConstructorNotFoundException {
		switch(messageId) {
			case 0: construct( (salsa_lite.examples.fibonacci.FibonacciSimple)arguments[0], (Integer)arguments[1] ); return;
			case 1: construct( (java.lang.String[])arguments[0] ); return;
			default: throw new ConstructorNotFoundException(messageId, arguments);
		}
	}

	public void construct(FibonacciSimple sender, int n) {
		this.sender = sender;
		this.n = n;
	}

	public void construct(String[] arguments) {
		this.n = Integer.parseInt(arguments[0]);
		StageService.sendMessage(this, 1 /*compute*/, null);
	}



	public void setResult(int value) {
		received++;
		result += value;
		if (received == 2) {
			if (sender == null) {
				System.out.println(result);
				System.exit(0);
			}
			else {
				StageService.sendMessage(sender, 0 /*setResult*/, new Object[]{result});
			}

		}
		
	}

	public void compute() {
		if (n == 0) {
			StageService.sendMessage(sender, 0 /*setResult*/, new Object[]{0});
		}
		else if (n <= 2) {
			StageService.sendMessage(sender, 0 /*setResult*/, new Object[]{1});
		}
		else {
			StageService.sendMessage(FibonacciSimple.construct(0, new Object[]{this, n - 1}), 1 /*compute*/, null);
			StageService.sendMessage(FibonacciSimple.construct(0, new Object[]{this, n - 2}), 1 /*compute*/, null);
		}

	}


	public FibonacciSimple() { super(); }

	public FibonacciSimple(SynchronousMailboxStage stage) { super(stage); }

	public static void main(String[] arguments) {
		construct(1, new Object[]{arguments}, StageService.getStage(0));
	}

	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
		FibonacciSimple actor = new FibonacciSimple();
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

	public static FibonacciSimple construct(int constructor_id, Object[] arguments) {
		FibonacciSimple actor = new FibonacciSimple();
		StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {
		FibonacciSimple actor = new FibonacciSimple(target_stage);
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

	public static FibonacciSimple construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
		FibonacciSimple actor = new FibonacciSimple(target_stage);
		target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
}
