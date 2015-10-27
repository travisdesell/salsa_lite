/****** SALSA LANGUAGE IMPORTS ******/
import salsa_lite.common.DeepCopy;
import salsa_lite.runtime.LocalActorRegistry;
import salsa_lite.runtime.Hashing;
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


public class GenericCell<T extends Object> extends salsa_lite.runtime.Actor implements java.io.Serializable {


    public Object writeReplace() throws java.io.ObjectStreamException {
        int hashCode = Hashing.getHashCodeFor(this.hashCode(), TransportService.getHost(), TransportService.getPort());
        synchronized (LocalActorRegistry.getLock(hashCode)) {
            LocalActorRegistry.addEntry(hashCode, this);
        }
        return new SerializedGenericCell( this.hashCode(), TransportService.getHost(), TransportService.getPort() );
    }

    public static class RemoteReference extends GenericCell {
        private int hashCode;
        private String host;
        private int port;
        RemoteReference(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }

        public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {
            TransportService.sendMessageToRemote(host, port, this.getStage().message);
            throw new RemoteMessageException();
        }

        public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {
            TransportService.sendMessageToRemote(host, port, this.getStage().message);
            throw new RemoteMessageException();
        }

        public Object writeReplace() throws java.io.ObjectStreamException {
            return new SerializedGenericCell( hashCode, host, port);
        }
    }

    public static class SerializedGenericCell implements java.io.Serializable {
        int hashCode;
        String host;
        int port;

        SerializedGenericCell(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }

        public Object readResolve() throws java.io.ObjectStreamException {
            int hashCode = Hashing.getHashCodeFor(this.hashCode, this.host, this.port);
            synchronized (LocalActorRegistry.getLock(hashCode)) {
                GenericCell actor = (GenericCell)LocalActorRegistry.getEntry(hashCode);
                if (actor == null) {
                    RemoteReference remoteReference = new RemoteReference(this.hashCode, this.host, this.port);
                    LocalActorRegistry.addEntry(hashCode, remoteReference);
                    return remoteReference;
                } else {
                    return actor;
                }
            }
        }
    }

    public String getMessageInformation(int messageId) {
    	switch (messageId) {
    		case 0: return "java.lang.String [GenericCell<T extends Object>].toString()";
    		case 1: return "int [GenericCell<T extends Object>].hashCode()";
    		case 2: return "int [GenericCell<T extends Object>].getStageId()";
    		case 3: return "T extends Object [GenericCell<T extends Object>].get()";
    		case 4: return "ack [GenericCell<T extends Object>].set(T extends Object)";
    		case 5: return "ack [GenericCell<T extends Object>].print()";
    	}
    	return "No message with specified id.";
    }

    public GenericCell() { super(); }
    public GenericCell(int stage_id) { super(stage_id); }

    T value;


    public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {
        switch(messageId) {
            case 0: return toString();
            case 1: return hashCode();
            case 2: return getStageId();
            case 3: return get();
            case 4: set( (T)arguments[0] ); return null;
            case 5: print(); return null;
            default: throw new MessageHandlerNotFoundException(messageId, arguments);
        }
    }

    public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {
        switch(messageId) {
            case 0: construct( (String[])arguments[0] ); return;
            case 1: construct( (T)arguments[0] ); return;
            default: throw new ConstructorNotFoundException(messageId, arguments);
        }
    }

    public void construct(String[] arguments) {
        GenericCell<String> gcs = GenericCell.construct(1, new Object[]{"string cell message"});
        GenericCell<Object> gco = GenericCell.construct(1, new Object[]{"object cell message"});
        ContinuationDirector continuation_token = StageService.sendContinuationMessage(gcs, 5 /*print*/, null);
        continuation_token = StageService.sendContinuationMessage(gco, 5 /*print*/, null, continuation_token);
        continuation_token = StageService.sendContinuationMessage(gco, 4 /*set*/, new Object[]{StageService.sendImplicitTokenMessage(gcs, 3 /*get*/, null, continuation_token)}, new int[]{0});
        StageService.sendMessage(gco, 5 /*print*/, null, continuation_token);
    }

    public void construct(T value) {
        ((GenericCell<T extends Object>)this).value = value;
    }



    public T get() {
        System.err.println("getting: " + value);
        return (T)DeepCopy.deepCopy( value );
    }

    public void set(T value) {
        System.err.println("setting: " + value);
        ((GenericCell<T extends Object>)this).value = value;
    }

    public void print() {
        System.out.println(value);
    }


    public static void main(String[] arguments) {
        GenericCell.construct(0, new Object[]{arguments});
    }

    public static GenericCell construct(int constructor_id, Object[] arguments) {
        GenericCell actor = new GenericCell();
        StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }

    public static GenericCell construct(int constructor_id, Object[] arguments, int target_stage_id) {
        GenericCell actor = new GenericCell(target_stage_id);
        actor.getStage().putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }

    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
        GenericCell actor = new GenericCell();
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
        Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});
        return output_continuation;
    }

    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, int target_stage_id) {
        GenericCell actor = new GenericCell(target_stage_id);
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage_id);
        Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage_id);
        return output_continuation;
    }

}
