package salsa_lite.runtime;

import java.util.Hashtable;
import java.util.Vector;

import salsa_lite.runtime.language.ContinuationDirector;
import salsa_lite.runtime.language.Director;
import salsa_lite.runtime.language.MessageDirector;
import salsa_lite.runtime.language.ImplicitTokenDirector;
import salsa_lite.runtime.language.TokenDirector;
import salsa_lite.runtime.language.ValueDirector;

public class StageService {
    public final static int CONTINUATION_SET_MESSAGE_ID = 4;
    public final static int DIRECTOR_SET_VALUE_ID = 3;

	public final static int number_stages;
	public final static SynchronousMailboxStage[] stages;

	static {
		if (System.getProperty("nstages") != null)  number_stages = Integer.parseInt(System.getProperty("nstages"));
		else number_stages = 1;
	
		stages = new SynchronousMailboxStage[number_stages];
		for (int i = 0; i < number_stages; i++) {
			stages[i] = new SynchronousMailboxStage(i);
			stages[i].start();
		}
	}

	public final static SynchronousMailboxStage getStage(int stage) {
        if (stage < 0) return getNewStage();
        else return stages[stage];
	}

	public final static SynchronousMailboxStage getStage() {
		return stages[Math.abs((int)(Math.random() % stages.length))];
	}

	public final static SynchronousMailboxStage getStage(Actor target) {
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
        if (message == null) System.err.println("MESSAGE == NULL!");
        if (message.target == null) System.err.println("MESSAGE.target == NULL!");
        if (message.target.stage == null) System.err.println("MESSAGE.target.stage == NULL!");
		message.target.stage.putMessageInMailbox(message);
	}

	/**
	 *	Send messages which are not followed by a continuation and do not output a named token
	 *	If token_positions are an argument, there are named (or implicit) tokens in the arguments
	 *	If input_continuation is an argument, it is waiting for a continuation
	 */
	public final static void sendMessage(Actor target, int message_id, Object[] arguments) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		target.stage.putMessageInMailbox(input_message);
	}

	public final static void sendMessage(Actor target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		input_continuation.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, input_continuation, CONTINUATION_SET_MESSAGE_ID, new Object[]{input_message}));
	}

	public final static void sendMessage(Actor target, int message_id, Object[] arguments, Director[] input_continuations) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, input_continuations}, target.getStageId());
	}

	public final static void sendMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(2, new Object[]{input_message, arguments, token_positions, input_continuation}, target.getStageId());
	}

	public final static void sendMessage(Actor target, int message_id, Object[] arguments, int[] token_positions) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target.getStageId());
	}

	public final static void sendMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, Director[] input_continuations) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(4, new Object[]{input_message, arguments, token_positions, input_continuations}, target.getStageId());
	}

	public final static void sendMessage(Actor target, int message_id, Object[] arguments, ValueDirector message_target) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(5, new Object[]{input_message, message_target}, message_target.getStageId());
	}

	public final static void sendMessage(Actor target, int message_id, Object[] arguments, ContinuationDirector input_continuation, ValueDirector message_target) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(6, new Object[]{input_message, input_continuation, message_target}, message_target.getStageId());
	}

	public final static void sendMessage(Actor target, int message_id, Object[] arguments, Director[] input_continuations, ValueDirector message_target) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(7, new Object[]{input_message, input_continuations, message_target}, message_target.getStageId());
	}

	public final static void sendMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation, ValueDirector message_target) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(8, new Object[]{input_message, arguments, token_positions, input_continuation, message_target}, message_target.getStageId());
	}

	public final static void sendMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, ValueDirector message_target) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(9, new Object[]{input_message, arguments, token_positions, message_target}, message_target.getStageId());
	}

	public final static void sendMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, Director[] input_continuations, ValueDirector message_target) {
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(10, new Object[]{input_message, arguments, token_positions, input_continuations, message_target}, message_target.getStageId());
	}


    /**
     *  Create a message to forward a token to the current continuation.
     *
     *  Used in first class continuations ie:
     *
     *  token ack t = standardOutput<-println();
     *  pass t;
     */

    public final static void passToken(TokenDirector token, Director currentContinuation) {
        if (currentContinuation == null) return;

//      token<-forwardTo(currentContinuation);
        token.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, token, 5 /*forwardTo*/, new Object[]{currentContinuation}, null));
    }

    public final static void passToken(ContinuationDirector token, Director currentContinuation) {
        if (currentContinuation == null) return;

//      token<-forwardTo(currentContinuation);
        token.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, token, 5 /*forwardTo*/, new Object[]{currentContinuation}, null));
    }

	/**
	 *	Create a message to be used in a pass statement (first class continuation)
	 */
	public final static void sendPassMessage(Actor target, int message_id, Object[] arguments, Director currentContinuation) {
        int messageType = (currentContinuation == null) ? Message.SIMPLE_MESSAGE : Message.TOKEN_MESSAGE;
        Message input_message = new Message(messageType, target, message_id, arguments, currentContinuation);
        target.stage.putMessageInMailbox(input_message);
	}

	public final static void sendPassMessage(Actor target, int message_id, Object[] arguments, ContinuationDirector input_continuation, Director currentContinuation) {
        int messageType = (currentContinuation == null) ? Message.SIMPLE_MESSAGE : Message.TOKEN_MESSAGE;
        Message input_message = new Message(messageType, target, message_id, arguments, currentContinuation);
		input_continuation.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, input_continuation, CONTINUATION_SET_MESSAGE_ID, new Object[]{input_message}));
	}

	public final static void sendPassMessage(Actor target, int message_id, Object[] arguments, Director[] input_continuations, Director currentContinuation) {
        int messageType = (currentContinuation == null) ? Message.SIMPLE_MESSAGE : Message.TOKEN_MESSAGE;
        Message input_message = new Message(messageType, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, input_continuations}, target.getStageId());
	}

	public final static void sendPassMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation, Director currentContinuation) {
        int messageType = (currentContinuation == null) ? Message.SIMPLE_MESSAGE : Message.TOKEN_MESSAGE;
        Message input_message = new Message(messageType, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(2, new Object[]{input_message, arguments, token_positions, input_continuation}, target.getStageId());
	}

	public final static void sendPassMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, Director currentContinuation) {
        int messageType = (currentContinuation == null) ? Message.SIMPLE_MESSAGE : Message.TOKEN_MESSAGE;
        Message input_message = new Message(messageType, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target.getStageId());
	}

	public final static void sendPassMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, Director[] input_continuations, Director currentContinuation) {
        int messageType = (currentContinuation == null) ? Message.SIMPLE_MESSAGE : Message.TOKEN_MESSAGE;
        Message input_message = new Message(messageType, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(4, new Object[]{input_message, arguments, token_positions, input_continuations}, target.getStageId());
	}

	public final static void sendPassMessage(Actor target, int message_id, Object[] arguments, Director currentContinuation, ValueDirector message_target) {
        int messageType = (currentContinuation == null) ? Message.SIMPLE_MESSAGE : Message.TOKEN_MESSAGE;
        Message input_message = new Message(messageType, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(5, new Object[]{input_message, message_target}, target.getStageId());
	}

	public final static void sendPassMessage(Actor target, int message_id, Object[] arguments, ContinuationDirector input_continuation, Director currentContinuation, ValueDirector message_target) {
        int messageType = (currentContinuation == null) ? Message.SIMPLE_MESSAGE : Message.TOKEN_MESSAGE;
        Message input_message = new Message(messageType, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(6, new Object[]{input_message, input_continuation, message_target}, target.getStageId());
	}

	public final static void sendPassMessage(Actor target, int message_id, Object[] arguments, Director[] input_continuations, Director currentContinuation, ValueDirector message_target) {
        int messageType = (currentContinuation == null) ? Message.SIMPLE_MESSAGE : Message.TOKEN_MESSAGE;
        Message input_message = new Message(messageType, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(7, new Object[]{input_message, input_continuations, message_target}, target.getStageId());
	}

	public final static void sendPassMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation, Director currentContinuation, ValueDirector message_target) {
        int messageType = (currentContinuation == null) ? Message.SIMPLE_MESSAGE : Message.TOKEN_MESSAGE;
        Message input_message = new Message(messageType, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(8, new Object[]{input_message, arguments, token_positions, input_continuation, message_target}, target.getStageId());
	}

	public final static void sendPassMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, Director currentContinuation, ValueDirector message_target) {
        int messageType = (currentContinuation == null) ? Message.SIMPLE_MESSAGE : Message.TOKEN_MESSAGE;
        Message input_message = new Message(messageType, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(9, new Object[]{input_message, arguments, token_positions, message_target}, target.getStageId());
	}

	public final static void sendPassMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, Director[] input_continuations, Director currentContinuation, ValueDirector message_target) {
        int messageType = (currentContinuation == null) ? Message.SIMPLE_MESSAGE : Message.TOKEN_MESSAGE;
        Message input_message = new Message(messageType, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(10, new Object[]{input_message, arguments, token_positions, input_continuations, message_target}, target.getStageId());
	}



	/**
	 *	Send messages which are followed by a continuation
	 *	If token_positions are an argument, there are named (or implicit) tokens in the arguments
	 *	If input_continuation is an argument, it is waiting for a continuation
	 */
	public final static ContinuationDirector sendContinuationMessage(Actor target, int message_id, Object[] arguments) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		target.stage.putMessageInMailbox(input_message);
		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(Actor target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		input_continuation.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, input_continuation, CONTINUATION_SET_MESSAGE_ID, new Object[]{input_message}));

		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(Actor target, int message_id, Object[] arguments, Director[] input_continuations) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, input_continuations}, target.getStageId());

		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(Actor target, int message_id, Object[] arguments, int[] token_positions) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target.getStageId());

		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(2, new Object[]{input_message, arguments, token_positions, input_continuation}, target.getStageId());

		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, Director[] input_continuations) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(4, new Object[]{input_message, arguments, token_positions, input_continuations}, target.getStageId());

		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(Actor target, int message_id, Object[] arguments, ValueDirector message_target) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(5, new Object[]{input_message, message_target}, message_target.getStageId());

		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(Actor target, int message_id, Object[] arguments, ContinuationDirector input_continuation, ValueDirector message_target) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(6, new Object[]{input_message, input_continuation, message_target}, message_target.getStageId());

		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(Actor target, int message_id, Object[] arguments, Director[] input_continuations, ValueDirector message_target) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(7, new Object[]{input_message, input_continuations, message_target}, message_target.getStageId());

		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation, ValueDirector message_target) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(8, new Object[]{input_message, arguments, token_positions, input_continuation, message_target}, message_target.getStageId());

		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, ValueDirector message_target) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(9, new Object[]{input_message, arguments, token_positions, message_target}, message_target.getStageId());

		return output_continuation;
	}

	public final static ContinuationDirector sendContinuationMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, Director[] input_continuations, ValueDirector message_target) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(10, new Object[]{input_message, arguments, token_positions, input_continuations, message_target}, message_target.getStageId());

		return output_continuation;
	}



	/**
	 *	Send messages which output a named token
	 *	If token_positions are an argument, there are named (or implicit) tokens in the arguments
	 *	If input_continuation is an argument, it is waiting for a continuation
	 */
	public final static TokenDirector sendTokenMessage(Actor target, int message_id, Object[] arguments) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		target.stage.putMessageInMailbox(input_message);

		return output_continuation;
	}

	public final static TokenDirector sendTokenMessage(Actor target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		input_continuation.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, input_continuation, CONTINUATION_SET_MESSAGE_ID, new Object[]{input_message}));

		return output_continuation;
	}

	public final static TokenDirector sendTokenMessage(Actor target, int message_id, Object[] arguments, Director[] input_continuations) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, input_continuations}, target.getStageId());

		return output_continuation;
	}

	public final static TokenDirector sendTokenMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(2, new Object[]{input_message, arguments, token_positions, input_continuation}, target.getStageId());

		return output_continuation;
	}

	public final static TokenDirector sendTokenMessage(Actor target, int message_id, Object[] arguments, int[] token_positions) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target.getStageId());

		return output_continuation;
	}

	public final static TokenDirector sendTokenMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, Director[] input_continuations) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(4, new Object[]{input_message, arguments, token_positions, input_continuations}, target.getStageId());

		return output_continuation;
	}

	public final static TokenDirector sendTokenMessage(Actor target, int message_id, Object[] arguments, ValueDirector message_target) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(5, new Object[]{input_message, message_target}, message_target.getStageId());

		return output_continuation;
	}

	public final static TokenDirector sendTokenMessage(Actor target, int message_id, Object[] arguments, ContinuationDirector input_continuation, ValueDirector message_target) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(6, new Object[]{input_message, input_continuation, message_target}, message_target.getStageId());

		return output_continuation;
	}

	public final static TokenDirector sendTokenMessage(Actor target, int message_id, Object[] arguments, Director[] input_continuations, ValueDirector message_target) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(7, new Object[]{input_message, input_continuations, message_target}, message_target.getStageId());

		return output_continuation;
	}

	public final static TokenDirector sendTokenMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation, ValueDirector message_target) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(8, new Object[]{input_message, arguments, token_positions, input_continuation, message_target}, message_target.getStageId());

		return output_continuation;
	}


	public final static TokenDirector sendTokenMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, ValueDirector message_target) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(9, new Object[]{input_message, arguments, token_positions, message_target}, message_target.getStageId());

		return output_continuation;
	}

	public final static TokenDirector sendTokenMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, Director[] input_continuations, ValueDirector message_target) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(10, new Object[]{input_message, arguments, token_positions, input_continuations, message_target}, message_target.getStageId());

		return output_continuation;
	}



	/**
	 *	Send messages which output an implicit (unnamed) token
	 *	If token_positions are an argument, there are named (or implicit) tokens in the arguments
	 *	If input_continuation is an argument, it is waiting for a continuation
	 */
	public final static ImplicitTokenDirector sendImplicitTokenMessage(Actor target, int message_id, Object[] arguments) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		target.stage.putMessageInMailbox(input_message);

		return output_continuation;
	}

	public final static ImplicitTokenDirector sendImplicitTokenMessage(Actor target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		input_continuation.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, input_continuation, CONTINUATION_SET_MESSAGE_ID, new Object[]{input_message}));

		return output_continuation;
	}

	public final static ImplicitTokenDirector sendImplicitTokenMessage(Actor target, int message_id, Object[] arguments, Director[] input_continuations) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, input_continuations}, target.getStageId());

		return output_continuation;
	}

	public final static ImplicitTokenDirector sendImplicitTokenMessage(Actor target, int message_id, Object[] arguments, int[] token_positions) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target.getStageId());

		return output_continuation;
	}

	public final static ImplicitTokenDirector sendImplicitTokenMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, Director[] input_continuations) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null, target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(4, new Object[]{input_message, arguments, token_positions, input_continuations}, target.getStageId());

		return output_continuation;
	}

	public final static ImplicitTokenDirector sendImplicitTokenMessage(Actor target, int message_id, Object[] arguments, ValueDirector message_target) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(5, new Object[]{input_message, message_target}, message_target.getStageId());

		return output_continuation;
	}

	public final static ImplicitTokenDirector sendImplicitTokenMessage(Actor target, int message_id, Object[] arguments, ContinuationDirector input_continuation, ValueDirector message_target) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(6, new Object[]{input_message, input_continuation, message_target}, message_target.getStageId());

		return output_continuation;
	}

	public final static ImplicitTokenDirector sendImplicitTokenMessage(Actor target, int message_id, Object[] arguments, Director[] input_continuations, ValueDirector message_target) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(7, new Object[]{input_message, input_continuations, message_target}, message_target.getStageId());

		return output_continuation;
	}

	public final static ImplicitTokenDirector sendImplicitTokenMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, ValueDirector message_target) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(9, new Object[]{input_message, arguments, token_positions, message_target}, message_target.getStageId());

		return output_continuation;
	}

	public final static ImplicitTokenDirector sendImplicitTokenMessage(Actor target, int message_id, Object[] arguments, int[] token_positions, Director[] input_continuations, ValueDirector message_target) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null, message_target.getStageId());

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(10, new Object[]{input_message, arguments, token_positions, input_continuations, message_target}, message_target.getStageId());

		return output_continuation;
	}

}
