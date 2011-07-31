/****** SALSA LANGUAGE IMPORTS ******/
import salsa_lite.common.DeepCopy;
import salsa_lite.runtime.ActorRegistry;
import salsa_lite.runtime.Acknowledgement;
import salsa_lite.runtime.SynchronousMailboxStage;
import salsa_lite.runtime.Actor;
import salsa_lite.runtime.Message;
import salsa_lite.runtime.MobileActor;
import salsa_lite.runtime.StageService;
import salsa_lite.runtime.TransportService;
import salsa_lite.runtime.language.Director;
import salsa_lite.runtime.language.JoinDirector;
import salsa_lite.runtime.language.MessageDirector;
import salsa_lite.runtime.language.ContinuationDirector;
import salsa_lite.runtime.language.TokenDirector;

import salsa_lite.runtime.language.exceptions.RemoteMessageException;
import salsa_lite.runtime.language.exceptions.TokenPassException;
import salsa_lite.runtime.language.exceptions.MessageHandlerNotFoundException;
import salsa_lite.runtime.language.exceptions.ConstructorNotFoundException;

/****** END SALSA LANGUAGE IMPORTS ******/

import salsa_lite.runtime.language.JoinDirector;

public class ThreadRing extends MobileActor implements java.io.Serializable {

	public Object writeReplace() throws java.io.ObjectStreamException {
		int hashCode = this.hashCode();
		synchronized (ActorRegistry.getLock(hashCode)) {
			ActorRegistry.addEntry(hashCode, this);
		}
		return new SerializedThreadRing( this.hashCode(), TransportService.getHost(), TransportService.getPort() );
	}

	public static class ThreadRingRemoteReference extends ThreadRing {
		int hashCode;
		String host;
		int port;
		ThreadRingRemoteReference(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }

		public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {
			TransportService.sendMessage(host, port, this.stage.message);
			throw new RemoteMessageException();
		}

		public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {
			TransportService.sendMessage(host, port, this.stage.message);
			throw new RemoteMessageException();
		}

		public Object writeReplace() throws java.io.ObjectStreamException {
			return new SerializedThreadRing( this.hashCode(), TransportService.getHost(), TransportService.getPort() );
		}
	}

	public static class SerializedThreadRing implements java.io.Serializable {
		int hashCode;
		String host;
		int port;

		SerializedThreadRing(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }

		public Object readResolve() throws java.io.ObjectStreamException {
			synchronized (ActorRegistry.getLock(hashCode)) {
				ThreadRing actor = (ThreadRing)ActorRegistry.getEntry(hashCode);
				if (actor == null) {
					System.err.println("DESERIALIZING A REMOTE REFERENCE TO A LOCAL ACTOR");
					ThreadRingRemoteReference remoteReference = new ThreadRingRemoteReference(hashCode, host, port);
					ActorRegistry.addEntry(hashCode, remoteReference);
					return remoteReference;
				} else {
					return actor;
				}
			}
		}
	}

	public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {
		State state;
		synchronized (ActorRegistry.getLock(this.hashCode())) {
			state = (State)ActorRegistry.getEntry(this.hashCode());
		}
		if (state == null) {
			TransportService.sendMessage(host, port, this.stage.message);
			throw new RemoteMessageException();
		} else {
			return state.invokeMessage(messageId, arguments);
		}
	}

	public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {
		State state;
		synchronized (ActorRegistry.getLock(this.hashCode())) {
			state = (State)ActorRegistry.getEntry(this.hashCode());
		}
		if (state == null) {
			TransportService.sendMessage(host, port, this.stage.message);
			throw new RemoteMessageException();
		} else {
			state.invokeConstructor(messageId, arguments);
		}
	}


	public ThreadRing() { super(); }
	public ThreadRing(SynchronousMailboxStage stage) { super(stage); }

	public static void main(String[] arguments) {
		ThreadRing.construct(1, new Object[]{arguments});
	}

	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
		ThreadRing actor = new ThreadRing();
		State state = new State(actor.stage);
		synchronized (ActorRegistry.getLock(actor.hashCode())) {
			ActorRegistry.addEntry(actor.hashCode(), state);
		}
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
		Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});
		return output_continuation;
	}

	public static ThreadRing construct(int constructor_id, Object[] arguments) {
		ThreadRing actor = new ThreadRing();
		State state = new State(actor.stage);
		synchronized (ActorRegistry.getLock(actor.hashCode())) {
			ActorRegistry.addEntry(actor.hashCode(), state);
		}
		StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {
		ThreadRing actor = new ThreadRing(target_stage);
		State state = new State(target_stage);
		synchronized (ActorRegistry.getLock(actor.hashCode())) {
			ActorRegistry.addEntry(actor.hashCode(), state);
		}
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage);
		Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage);
		return output_continuation;
	}

	public static ThreadRing construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
		ThreadRing actor = new ThreadRing(target_stage);
		State state = new State(target_stage);
		synchronized (ActorRegistry.getLock(actor.hashCode())) {
			ActorRegistry.addEntry(actor.hashCode(), state);
		}
		target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}


	public static class State extends MobileActor.State {
		public State() { super(); }
		public State(SynchronousMailboxStage stage) { super(stage); }

		public void migrate(String host, int port) {
		}

		ThreadRing next;
		int id;


		public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {
			switch(messageId) {
				case 0: migrate( (String)arguments[0], (Integer)arguments[1] ); return null;
				case 1: setNextThread( (ThreadRing)arguments[0] ); return null;
				case 2: forwardMessage( (Integer)arguments[0] ); return null;
				default: throw new MessageHandlerNotFoundException(messageId, arguments);
			}
		}

		public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {
			switch(messageId) {
				case 0: construct( (Integer)arguments[0] ); return;
				case 1: construct( (String[])arguments[0] ); return;
				default: throw new ConstructorNotFoundException(messageId, arguments);
			}
		}

		public void construct(int id) {
			this.id = id;
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
				ContinuationDirector continuation_token = StageService.sendContinuationMessage(previous, 1 /*setNextThread*/, new Object[]{next});
				StageService.sendMessage(jd, 0 /*join*/, null, continuation_token);
				previous = next;
			}

			ContinuationDirector continuation_token = StageService.sendContinuationMessage(next, 1 /*setNextThread*/, new Object[]{first});
			StageService.sendMessage(jd, 0 /*join*/, null, continuation_token);
			continuation_token = StageService.sendContinuationMessage(jd, 1 /*resolveAfter*/, new Object[]{threadCount});
			StageService.sendMessage(first, 2 /*forwardMessage*/, new Object[]{hopCount}, continuation_token);
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
				StageService.sendMessage(next, 2 /*forwardMessage*/, new Object[]{value});
			}

		}


	}
}
