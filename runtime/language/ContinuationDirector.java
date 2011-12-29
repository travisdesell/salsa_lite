package salsa_lite.runtime.language;

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

import java.util.LinkedList;

public class ContinuationDirector extends Director implements java.io.Serializable {

    public static ContinuationDirector getRemoteReference(String name, String host, int port) {
        int hashCode = ActorRegistry.getHashCodeFor(name, host, port);
        synchronized (ActorRegistry.getLock(hashCode)) {
            ContinuationDirector entry = (ContinuationDirector)ActorRegistry.getEntry(hashCode);
            if (entry == null) {
                RemoteReference reference = new RemoteReference(hashCode, host, port);
                ActorRegistry.addEntry(hashCode, reference);
                return reference;
            } else {
                return entry;
            }
        }
    }

    public Object writeReplace() throws java.io.ObjectStreamException {
        int hashCode = this.hashCode();
        synchronized (ActorRegistry.getLock(hashCode)) {
            ActorRegistry.addEntry(hashCode, this);
        }
        return new SerializedContinuationDirector( hashCode, TransportService.getHost(), TransportService.getPort() );
    }

    public static class RemoteReference extends ContinuationDirector {
        private int hashCode;
        private String host;
        private int port;
        RemoteReference(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }

        public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {
            TransportService.sendMessageToRemote(host, port, this.stage.message);
            throw new RemoteMessageException();
        }

        public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {
            TransportService.sendMessageToRemote(host, port, this.stage.message);
            throw new RemoteMessageException();
        }

        public Object writeReplace() throws java.io.ObjectStreamException {
            return new SerializedContinuationDirector( hashCode, TransportService.getHost(), TransportService.getPort() );
        }
    }

    public static class SerializedContinuationDirector implements java.io.Serializable {
        int hashCode;
        String host;
        int port;

        SerializedContinuationDirector(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }

        public Object readResolve() throws java.io.ObjectStreamException {
            synchronized (ActorRegistry.getLock(hashCode)) {
                ContinuationDirector actor = (ContinuationDirector)ActorRegistry.getEntry(hashCode);
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

    public ContinuationDirector() { super(); }
    public ContinuationDirector(SynchronousMailboxStage stage) { super(stage); }

    boolean unresolved = true;
    LinkedList<Message> messages = new LinkedList<Message>(  );
    ContinuationDirector currentContinuation = null;


    public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {
        switch(messageId) {
            case 0: return toString();
            case 1: resolve(); return null;
            case 2: setMessage( (Message)arguments[0] ); return null;
            case 3: forwardTo( (Director)arguments[0] ); return null;
            default: throw new MessageHandlerNotFoundException(messageId, arguments);
        }
    }

    public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {
        switch(messageId) {
            case 0: construct(); return;
            default: throw new ConstructorNotFoundException(messageId, arguments);
        }
    }

    public void construct() {
    }



    public void resolve() {
        if (unresolved) {
            unresolved = false;
        } 
        while (messages.size() > 0) {
            Message message = messages.removeFirst();
            StageService.sendMessage(message);
        }

        if (currentContinuation != null) {
            StageService.sendMessage(currentContinuation, 1 /*resolve*/, null);
        } 
    }

    public void setMessage(Message message) {
        if (unresolved) {
            messages.add(message);
        }
        else {
            StageService.sendMessage(message);
        }

    }

    public void forwardTo(Director director) {
        if (unresolved) {
            currentContinuation = (ContinuationDirector)director;
        }
        else {
            StageService.sendMessage(((ContinuationDirector)director), 1 /*resolve*/, null);
        }

    }


    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
        ContinuationDirector actor = new ContinuationDirector();
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
        Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});
        return output_continuation;
    }

    public static ContinuationDirector construct(int constructor_id, Object[] arguments) {
        ContinuationDirector actor = new ContinuationDirector();
        StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }
    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {
        ContinuationDirector actor = new ContinuationDirector(target_stage);
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage);
        Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage);
        return output_continuation;
    }

    public static ContinuationDirector construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
        ContinuationDirector actor = new ContinuationDirector(target_stage);
        target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }
}
