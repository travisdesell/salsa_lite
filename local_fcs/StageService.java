package salsa_lite.local_fcs;

import java.util.Hashtable;
import java.util.Vector;

import salsa_lite.local_fcs.language.ContinuationDirector;
import salsa_lite.local_fcs.language.Director;
import salsa_lite.local_fcs.language.MessageDirector;
import salsa_lite.local_fcs.language.ImplicitTokenDirector;
import salsa_lite.local_fcs.language.TokenDirector;

public class StageService {
	public final static int number_stages;
	public final static SynchronousMailboxStage[] stages;

	static {
		System.err.println("created local fcs stage service");

		if (System.getProperty("nstages") != null)  number_stages = Integer.parseInt(System.getProperty("nstages"));
		else number_stages = 1;
	
		stages = new SynchronousMailboxStage[number_stages];
		for (int i = 0; i < number_stages; i++) {
			stages[i] = new SynchronousMailboxStage(i);
			stages[i].start();
			System.err.println("created stage[" + i + "]");
		}
	}

	public final static SynchronousMailboxStage getStage(int stage) {
		return stages[stage];
	}

	public final static SynchronousMailboxStage getStage() {
		return stages[Math.abs((int)Math.random() % stages.length)];
	}

	public final static SynchronousMailboxStage getStage(LocalActor target) {
		return target.stage;
	}

	public final static SynchronousMailboxStage getNewStage() {
		SynchronousMailboxStage newStage = new SynchronousMailboxStage(-1);
		newStage.start();
		return newStage;
	}

	/**
	 *	Send a previously generated message.
	 */
	public final static void sendMessage(Message message) {
		message.target.stage.putMessageInMailbox(message);
	}

	/**
	 *	Create a message to be used in a pass statement (first class continuation)
	 */
	public final static void sendPassMessage(LocalActor target, int message_id, Object[] arguments, Director currentContinuation) {
        Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, currentContinuation);
		target.stage.putMessageInMailbox(input_message);
	}

	public final static void sendPassMessage(LocalActor target, int message_id, Object[] arguments, ContinuationDirector input_continuation, Director currentContinuation) {
        Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, currentContinuation);
		input_continuation.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{input_message}));
	}

	public final static void sendPassMessage(LocalActor target, int message_id, Object[] arguments, Director[] input_continuations, Director currentContinuation) {
        Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, input_continuations}, target.stage);
	}

	public final static void sendPassMessage(LocalActor target, int message_id, Object[] arguments, int[] token_positions, Director currentContinuation) {
		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target.stage);
	}

	public final static void sendPassMessage(LocalActor target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation, Director currentContinuation) {
		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(2, new Object[]{input_message, arguments, token_positions, input_continuation}, target.stage);
	}

	public final static void sendPassMessage(LocalActor target, int message_id, Object[] arguments, int[] token_positions, Director[] input_continuations, Director currentContinuation) {
		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(4, new Object[]{input_message, arguments, token_positions, input_continuations}, target.stage);
	}


	/**
	 *	Send messages which are not followed by a continuation and do not output a named token
	 *	If token_positions are an argument, there are named (or implicit) tokens in the arguments
	 *	If input_continuation is an argument, it is waiting for a continuation
	 */
	public final static void sendMessage(LocalActor target, int message_id, Object[] arguments) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		target.stage.putMessageInMailbox(input_message);
	}

	public final static void sendMessage(LocalActor target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		input_continuation.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{input_message}));
	}

	public final static void sendMessage(LocalActor target, int message_id, Object[] arguments, int[] token_positions) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target.stage);
	}

	public final static void sendMessage(LocalActor target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(2, new Object[]{input_message, arguments, token_positions, input_continuation}, target.stage);
	}

	public final static void sendMessage(LocalActor target, int message_id, Object[] arguments, Director[] input_continuations) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, input_continuations}, target.stage);
	}

	public final static void sendMessage(LocalActor target, int message_id, Object[] arguments, int[] token_positions, Director[] input_continuations) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(4, new Object[]{input_message, arguments, token_positions, input_continuations}, target.stage);
	}


	/**
	 *	Send messages which are followed by a continuation
	 *	If token_positions are an argument, there are named (or implicit) tokens in the arguments
	 *	If input_continuation is an argument, it is waiting for a continuation
	 */
	public final static ContinuationDirector sendContinuationMessage(LocalActor target, int message_id, Object[] arguments) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		target.stage.putMessageInMailbox(input_message);
		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(LocalActor target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		input_continuation.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{input_message}));

		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(LocalActor target, int message_id, Object[] arguments, Director[] input_continuations) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, input_continuations}, target.stage);

		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(LocalActor target, int message_id, Object[] arguments, int[] token_positions) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target.stage);

		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(LocalActor target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(2, new Object[]{input_message, arguments, token_positions, input_continuation}, target.stage);

		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(LocalActor target, int message_id, Object[] arguments, int[] token_positions, Director[] input_continuations) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(4, new Object[]{input_message, arguments, token_positions, input_continuations}, target.stage);

		return output_continuation;
	}


	/**
	 *	Send messages which output a named (or implicit) token
	 *	If token_positions are an argument, there are named (or implicit) tokens in the arguments
	 *	If input_continuation is an argument, it is waiting for a continuation
	 */
	public final static TokenDirector sendTokenMessage(LocalActor target, int message_id, Object[] arguments) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		target.stage.putMessageInMailbox(input_message);

		return output_continuation;
	}

	public final static TokenDirector sendTokenMessage(LocalActor target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		input_continuation.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{input_message}));

		return output_continuation;
	}

	public final static TokenDirector sendTokenMessage(LocalActor target, int message_id, Object[] arguments, Director[] input_continuations) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, input_continuations}, target.stage);

		return output_continuation;
	}


	public final static TokenDirector sendTokenMessage(LocalActor target, int message_id, Object[] arguments, int[] token_positions) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target.stage);

		return output_continuation;
	}

	public final static TokenDirector sendTokenMessage(LocalActor target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(2, new Object[]{input_message, arguments, token_positions, input_continuation}, target.stage);

		return output_continuation;
	}

	public final static TokenDirector sendTokenMessage(LocalActor target, int message_id, Object[] arguments, int[] token_positions, Director[] input_continuations) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(4, new Object[]{input_message, arguments, token_positions, input_continuations}, target.stage);

		return output_continuation;
	}


	public final static ImplicitTokenDirector sendImplicitTokenMessage(LocalActor target, int message_id, Object[] arguments) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		target.stage.putMessageInMailbox(input_message);

		return output_continuation;
	}

	public final static ImplicitTokenDirector sendImplicitTokenMessage(LocalActor target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		input_continuation.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{input_message}));

		return output_continuation;
	}

	public final static ImplicitTokenDirector sendImplicitTokenMessage(LocalActor target, int message_id, Object[] arguments, Director[] input_continuations) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, input_continuations}, target.stage);

		return output_continuation;
	}


	public final static ImplicitTokenDirector sendImplicitTokenMessage(LocalActor target, int message_id, Object[] arguments, int[] token_positions) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null, target.stage);

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target.stage);

		return output_continuation;
	}
	
}
