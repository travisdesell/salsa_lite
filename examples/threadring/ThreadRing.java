/****** SALSA LANGUAGE IMPORTS ******/
import salsa_lite.common.DeepCopy;
import salsa_lite.runtime.ActorRegistry;
import salsa_lite.runtime.Acknowledgement;
import salsa_lite.runtime.SynchronousMailboxStage;
import salsa_lite.runtime.Actor;
import salsa_lite.runtime.Message;
import salsa_lite.runtime.RemoteActor;
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

public class ThreadRing extends salsa_lite.runtime.Actor implements java.io.Serializable {

    public Object writeReplace() throws java.io.ObjectStreamException {
        int hashCode = this.hashCode();
        synchronized (ActorRegistry.getLock(hashCode)) {
            ActorRegistry.addEntry(hashCode, this);
        }
        return new SerializedThreadRing( hashCode, TransportService.getHost(), TransportService.getPort() );
    }

    public static class RemoteReference extends ThreadRing {
        private int hashCode;
        private String host;
        private int port;
        RemoteReference(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }

        public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {
            TransportService.sendMessage(host, port, this.stage.message);
            throw new RemoteMessageException();
        }

        public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {
            TransportService.sendMessage(host, port, this.stage.message);
            throw new RemoteMessageException();
        }

        public Object writeReplace() throws java.io.ObjectStreamException {
            return new SerializedThreadRing( hashCode, TransportService.getHost(), TransportService.getPort() );
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
                    RemoteReference remoteReference = new RemoteReference(hashCode, host, port);
                    ActorRegistry.addEntry(hashCode, remoteReference);
                    return remoteReference;
                } else {
                    return actor;
                }
            }
        }
    }

    public ThreadRing() { super(); }
    public ThreadRing(SynchronousMailboxStage stage) { super(stage); }

    ThreadRing next;
    int id;


    public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {
        switch(messageId) {
            case 0: return toString();
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
            StageService.sendMessage(jd, 1 /*join*/, null, continuation_token);
            previous = next;
        }

        ContinuationDirector continuation_token = StageService.sendContinuationMessage(next, 1 /*setNextThread*/, new Object[]{first});
        StageService.sendMessage(jd, 1 /*join*/, null, continuation_token);
        continuation_token = StageService.sendContinuationMessage(jd, 2 /*resolveAfter*/, new Object[]{threadCount});
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


    public static void main(String[] arguments) {
        ThreadRing.construct(1, new Object[]{arguments});
    }

    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
        ThreadRing actor = new ThreadRing();
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
        Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});
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
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage);
        return output_continuation;
    }

    public static ThreadRing construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
        ThreadRing actor = new ThreadRing(target_stage);
        target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }
}
