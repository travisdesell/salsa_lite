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


public class PingPong extends salsa_lite.runtime.Actor implements java.io.Serializable {


    public Object writeReplace() throws java.io.ObjectStreamException {
        int hashCode = Hashing.getHashCodeFor(this.hashCode(), TransportService.getHost(), TransportService.getPort());
        synchronized (LocalActorRegistry.getLock(hashCode)) {
            LocalActorRegistry.addEntry(hashCode, this);
        }
        return new SerializedPingPong( this.hashCode(), TransportService.getHost(), TransportService.getPort() );
    }

    public static class RemoteReference extends PingPong {
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
            return new SerializedPingPong( hashCode, host, port);
        }
    }

    public static class SerializedPingPong implements java.io.Serializable {
        int hashCode;
        String host;
        int port;

        SerializedPingPong(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }

        public Object readResolve() throws java.io.ObjectStreamException {
            int hashCode = Hashing.getHashCodeFor(this.hashCode, this.host, this.port);
            synchronized (LocalActorRegistry.getLock(hashCode)) {
                PingPong actor = (PingPong)LocalActorRegistry.getEntry(hashCode);
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
    		case 0: return "java.lang.String [salsa_lite.runtime.Actor].toString()";
    		case 1: return "int [salsa_lite.runtime.Actor].hashCode()";
    		case 2: return "int [salsa_lite.runtime.Actor].getStageId()";
    		case 3: return "ack [PingPong].volley(int)";
    		case 4: return "ack [PingPong].setOppenent(PingPong)";
    	}
    	return "No message with specified id.";
    }

    public PingPong() { super(); }
    public PingPong(int stage_id) { super(stage_id); }

    PingPong opponent;
    int id;


    public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {
        switch(messageId) {
            case 0: return toString();
            case 1: return hashCode();
            case 2: return getStageId();
            case 3: volley( (Integer)arguments[0] ); return null;
            case 4: setOppenent( (PingPong)arguments[0] ); return null;
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

    public void construct(int x) {
        ((PingPong)this).id = x;
    }

    public void construct(String[] args) {
        int n = Integer.parseInt(args[0]);
        PingPong ping = PingPong.construct(0, new Object[]{1});
        PingPong pong = PingPong.construct(0, new Object[]{2});
        StageService.sendMessage(ping, 4 /*setOppenent*/, new Object[]{pong});
        StageService.sendMessage(pong, 4 /*setOppenent*/, new Object[]{ping});
        StageService.sendMessage(ping, 3 /*volley*/, new Object[]{n});
    }



    public void volley(int x) {
        if (x == 0) {
            System.exit(0);
        }
        else {
            System.out.println("Player " + id + ":" + x);
            StageService.sendMessage(opponent, 3 /*volley*/, new Object[]{x - 1});
        }

    }

    public void setOppenent(PingPong o) {
        opponent = o;
    }


    public static void main(String[] arguments) {
        PingPong.construct(1, new Object[]{arguments});
    }

    public static PingPong construct(int constructor_id, Object[] arguments) {
        PingPong actor = new PingPong();
        StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }

    public static PingPong construct(int constructor_id, Object[] arguments, int target_stage_id) {
        PingPong actor = new PingPong(target_stage_id);
        actor.getStage().putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }

    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
        PingPong actor = new PingPong();
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
        Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});
        return output_continuation;
    }

    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, int target_stage_id) {
        PingPong actor = new PingPong(target_stage_id);
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage_id);
        Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage_id);
        return output_continuation;
    }

}
