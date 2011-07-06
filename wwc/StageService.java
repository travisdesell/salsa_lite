package salsa_lite.wwc;

import java.util.Hashtable;
import java.util.Vector;

import java.util.concurrent.atomic.AtomicLong;


import salsa_lite.wwc.language.ContinuationDirector;
import salsa_lite.wwc.language.MessageDirector;
import salsa_lite.wwc.language.ImplicitTokenDirector;
import salsa_lite.wwc.language.TokenDirector;

public class StageService {
	final static int number_stages;
	final static StageActor[] stages;

	static {
		System.err.println("created WWC stage service");

		if (System.getProperty("nstages") != null)  number_stages = Integer.parseInt(System.getProperty("nstages"));
		else number_stages = 1;
	
		stages = new StageActor[number_stages];
		for (int i = 0; i < number_stages; i++) {
			stages[i] = new StageActor(i);
			stages[i].start();
			System.err.println("created stage[" + i + "]");
		}

		id_base = TransportService.getHost() + "/" + TransportService.getPort() + "/" + System.currentTimeMillis() + "/";
		generated_ids = new AtomicLong(0);
	}

	private static final String id_base;
	private static AtomicLong generated_ids;

	public final static String generateUniqueId() {
		return id_base + generated_ids.getAndIncrement();
	}


	/**
	 * Direct messages and actors to the appropriate stage, based on a hash function.
	 ********/
	public static void createActor(WWCActorState actor_state, int constructor_id, Object[] arguments) {
		stages[Math.abs(actor_state.getHashcode() % number_stages)].putActorOnStage(actor_state, constructor_id, arguments);
	}

	public static void createActor(WWCActorState actor_state, int constructor_id, Object[] arguments, int[] token_positions) {
	}

	/**
	 *	Send a previously generated message.
	 */
	public static void sendMessage(Message message) {
		stages[Math.abs(message.target_reference.getHashcode() % number_stages)].putMessageInMailbox(message);
	}

	/**
	 *	Code for sending non-immutable messages.
	 */


	/**
	 *	Create a message to be used in a pass statement (first class continuation)
	 */

	public static ContinuationDirector getCurrentContinuationDirector(WWCActorReference currentActor) {
		return stages[Math.abs(currentActor.getHashcode() % number_stages)].message.continuationDirector;
	}

	public static void sendPassMessage(WWCActorReference target, int message_id, Object[] arguments, ContinuationDirector currentContinuation) {
		StageService.sendMessage(new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, currentContinuation));
	}

	public static void sendPassMessage(WWCActorReference target, int message_id, Object[] arguments, ContinuationDirector input_continuation, ContinuationDirector currentContinuation) {
		StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, currentContinuation)}));
	}

	public static void sendPassMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector currentContinuation) {
		//make MessageDirector
		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
	}

	public static void sendPassMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation, ContinuationDirector currentContinuation) {
		//make MessageDirector
		//send setMessage message to input_continuation
		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, token_positions.length, input_continuation});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
	}


	public static void sendPassImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, ContinuationDirector currentContinuation) {
		StageService.sendMessage(new Message(Message.IMMUTABLE_TOKEN_MESSAGE, target, message_id, arguments, currentContinuation));
	}

	public static void sendPassImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, ContinuationDirector input_continuation, ContinuationDirector currentContinuation) {
		StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{new Message(Message.IMMUTABLE_TOKEN_MESSAGE, target, message_id, arguments, currentContinuation)}));
	}

	public static void sendPassImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector currentContinuation) {
		//make MessageDirector
		Message input_message = new Message(Message.IMMUTABLE_TOKEN_MESSAGE, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
	}

	public static void sendPassImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation, ContinuationDirector currentContinuation) {
		//make MessageDirector
		//send setMessage message to input_continuation
		Message input_message = new Message(Message.IMMUTABLE_TOKEN_MESSAGE, target, message_id, arguments, currentContinuation);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, token_positions.length, input_continuation});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
	}


	/**
	 *	Send messages which are not followed by a continuation and do not output a named token
	 *	If token_positions are an argument, there are named (or implicit) tokens in the arguments
	 *	If input_continuation is an argument, it is waiting for a continuation
	 */
	public static void sendMessage(WWCActorReference target, int message_id, Object[] arguments) {
		sendMessage(new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments));
	}

	public static void sendMessage(WWCActorReference target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		sendMessage(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments)}));
	}

	public static void sendMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions) {
		//make MessageDirector
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
	}

	public static void sendMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation) {
		//make MessageDirector
		//send setMessage message to input_continuation
		Message input_message = new Message(Message.SIMPLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, token_positions.length, input_continuation});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
	}

	/**
	 *	Send messages which are followed by a continuation
	 *	If token_positions are an argument, there are named (or implicit) tokens in the arguments
	 *	If input_continuation is an argument, it is waiting for a continuation
	 */
	public static ContinuationDirector sendContinuationMessage(WWCActorReference target, int message_id, Object[] arguments) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null);

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		sendMessage(input_message);
		return output_continuation;
	}

	public static ContinuationDirector sendContinuationMessage(WWCActorReference target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null);

		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		sendMessage(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{input_message}));

		return output_continuation;
	}

	public static ContinuationDirector sendContinuationMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null);
		//make MessageDirector
		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
		return output_continuation;
	}

	public static ContinuationDirector sendContinuationMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null);
		//make MessageDirector
		//send setMessage message to input_continuation
		Message input_message = new Message(Message.CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, token_positions.length, input_continuation});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
		return output_continuation;
	}

	/**
	 *	Send messages which output a named (or implicit) token
	 *	If token_positions are an argument, there are named (or implicit) tokens in the arguments
	 *	If input_continuation is an argument, it is waiting for a continuation
	 */
	public static TokenDirector sendTokenMessage(WWCActorReference target, int message_id, Object[] arguments) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		sendMessage(input_message);
		return output_continuation;
	}

	public static TokenDirector sendTokenMessage(WWCActorReference target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		sendMessage(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{input_message}));

		return output_continuation;
	}

	public static TokenDirector sendTokenMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
		//make MessageDirector
		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
		return output_continuation;
	}

	public static TokenDirector sendTokenMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
		//make MessageDirector
		//send setMessage message to input_continuation
		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, token_positions.length, input_continuation});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
		return output_continuation;
	}

	public static ImplicitTokenDirector sendImplicitTokenMessage(WWCActorReference target, int message_id, Object[] arguments) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null);

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		sendMessage(input_message);
		return output_continuation;
	}

	public static ImplicitTokenDirector sendImplicitTokenMessage(WWCActorReference target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null);

		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		sendMessage(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{input_message}));

		return output_continuation;
	}

	public static ImplicitTokenDirector sendImplicitTokenMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null);
		//make MessageDirector
		Message input_message = new Message(Message.TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
		return output_continuation;
	}
	

	/**
	 *	Code for sending immutable messages.
	 */

	/**
	 *	Send immutable messages which are not followed by a continuation and do not output a named token
	 *	If token_positions are an argument, there are named (or implicit) tokens in the arguments
	 *	If input_continuation is an argument, it is waiting for a continuation
	 */
	public static void sendImmutableMessage(WWCActorReference target, int message_id, Object[] arguments) {
		sendMessage(new Message(Message.IMMUTABLE_MESSAGE, target, message_id, arguments));
	}

	public static void sendImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		sendMessage(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{new Message(Message.IMMUTABLE_MESSAGE, target, message_id, arguments)}));
	}

	public static void sendImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions) {
		//make MessageDirector
		Message input_message = new Message(Message.IMMUTABLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
	}

	public static void sendImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation) {
		//make MessageDirector
		//send setMessage message to input_continuation
		Message input_message = new Message(Message.IMMUTABLE_MESSAGE, target, message_id, arguments);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, token_positions.length, input_continuation});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
	}


	/**
	 *	Send messages which are followed by a continuation
	 *	If token_positions are an argument, there are named (or implicit) tokens in the arguments
	 *	If input_continuation is an argument, it is waiting for a continuation
	 */
	public static ContinuationDirector sendContinuationImmutableMessage(WWCActorReference target, int message_id, Object[] arguments) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null);

		Message input_message = new Message(Message.IMMUTABLE_CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		sendMessage(input_message);
		return output_continuation;
	}

	public static ContinuationDirector sendContinuationImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null);

		Message input_message = new Message(Message.IMMUTABLE_CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		sendMessage(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{input_message}));

		return output_continuation;
	}

	public static ContinuationDirector sendContinuationImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null);
		//make MessageDirector
		Message input_message = new Message(Message.IMMUTABLE_CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
		return output_continuation;
	}

	public static ContinuationDirector sendContinuationImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation) {
		ContinuationDirector output_continuation = ContinuationDirector.construct(0 /*construct()*/, null);
		//make MessageDirector
		//send setMessage message to input_continuation
		Message input_message = new Message(Message.IMMUTABLE_CONTINUATION_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, token_positions.length, input_continuation});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
		return output_continuation;
	}

	/**
	 *	Send messages which output a named (or implicit) token
	 *	If token_positions are an argument, there are named (or implicit) tokens in the arguments
	 *	If input_continuation is an argument, it is waiting for a continuation
	 */
	public static TokenDirector sendTokenImmutableMessage(WWCActorReference target, int message_id, Object[] arguments) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);

		Message input_message = new Message(Message.IMMUTABLE_TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		sendMessage(input_message);
		return output_continuation;
	}

	public static TokenDirector sendTokenImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);

		Message input_message = new Message(Message.IMMUTABLE_TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		sendMessage(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{input_message}));

		return output_continuation;
	}

	public static TokenDirector sendTokenImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
		//make MessageDirector
		Message input_message = new Message(Message.IMMUTABLE_TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
		return output_continuation;
	}

	public static TokenDirector sendTokenImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions, ContinuationDirector input_continuation) {
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
		//make MessageDirector
		//send setMessage message to input_continuation
		Message input_message = new Message(Message.IMMUTABLE_TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(1, new Object[]{input_message, token_positions.length, input_continuation});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
		return output_continuation;
	}

	public static ImplicitTokenDirector sendImplicitTokenImmutableMessage(WWCActorReference target, int message_id, Object[] arguments) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null);

		Message input_message = new Message(Message.IMMUTABLE_TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		sendMessage(input_message);
		return output_continuation;
	}

	public static ImplicitTokenDirector sendImplicitTokenImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, ContinuationDirector input_continuation) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null);

		Message input_message = new Message(Message.IMMUTABLE_TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		sendMessage(new Message(Message.SIMPLE_MESSAGE, input_continuation, 1 /*setMessage()*/, new Object[]{input_message}));

		return output_continuation;
	}

	public static ImplicitTokenDirector sendImplicitTokenImmutableMessage(WWCActorReference target, int message_id, Object[] arguments, int[] token_positions) {
		ImplicitTokenDirector output_continuation = ImplicitTokenDirector.construct(0 /*construct()*/, null);
		//make MessageDirector
		Message input_message = new Message(Message.IMMUTABLE_TOKEN_MESSAGE, target, message_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length});
		//send addMessageDirector messages to TokenDirectors
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
		return output_continuation;
	}
}
