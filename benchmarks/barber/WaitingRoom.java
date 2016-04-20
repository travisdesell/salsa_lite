package salsa_lite.benchmarks.barber;

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


public class WaitingRoom extends salsa_lite.runtime.Actor implements java.io.Serializable {


    public Object writeReplace() throws java.io.ObjectStreamException {
        int hashCode = Hashing.getHashCodeFor(this.hashCode(), TransportService.getHost(), TransportService.getPort());
        synchronized (LocalActorRegistry.getLock(hashCode)) {
            LocalActorRegistry.addEntry(hashCode, this);
        }
        return new SerializedWaitingRoom( this.hashCode(), TransportService.getHost(), TransportService.getPort() );
    }

    public static class RemoteReference extends WaitingRoom {
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
            return new SerializedWaitingRoom( hashCode, host, port);
        }
    }

    public static class SerializedWaitingRoom implements java.io.Serializable {
        int hashCode;
        String host;
        int port;

        SerializedWaitingRoom(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }

        public Object readResolve() throws java.io.ObjectStreamException {
            int hashCode = Hashing.getHashCodeFor(this.hashCode, this.host, this.port);
            synchronized (LocalActorRegistry.getLock(hashCode)) {
                WaitingRoom actor = (WaitingRoom)LocalActorRegistry.getEntry(hashCode);
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
    		case 3: return "ack [salsa_lite.benchmarks.barber.WaitingRoom].addCustomer(salsa_lite.benchmarks.barber.Customer)";
    		case 4: return "ack [salsa_lite.benchmarks.barber.WaitingRoom].next()";
    	}
    	return "No message with specified id.";
    }

    public WaitingRoom() { super(); }
    public WaitingRoom(int stage_id) { super(stage_id); }

    Customer[] waitingRoomChairs;
    int waitingRoomChairsFull;
    Barber theBarber;
    boolean sleepingBarber;


    public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {
        switch(messageId) {
            case 0: return toString();
            case 1: return hashCode();
            case 2: return getStageId();
            case 3: addCustomer( (Customer)arguments[0] ); return null;
            case 4: next(); return null;
            default: throw new MessageHandlerNotFoundException(messageId, arguments);
        }
    }

    public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {
        switch(messageId) {
            case 0: construct( (Integer)arguments[0], (Barber)arguments[1] ); return;
            default: throw new ConstructorNotFoundException(messageId, arguments);
        }
    }

    public void construct(int numChairs, Barber b) {
        waitingRoomChairsFull = 0;
        waitingRoomChairs = new Customer[numChairs];
        theBarber = b;
        sleepingBarber = true;
    }



    public void addCustomer(Customer c) {
        if (waitingRoomChairsFull == waitingRoomChairs.length) {
            StageService.sendMessage(c, 3 /*leave*/, null);
        }
        else {
            waitingRoomChairs[waitingRoomChairsFull] = c;
            waitingRoomChairsFull++;
            if (sleepingBarber) {
                sleepingBarber = false;
                StageService.sendMessage(((salsa_lite.benchmarks.barber.WaitingRoom)this), 4 /*next*/, null);
            }
            else {
                StageService.sendMessage(c, 4 /*sit*/, null);
            }

        }

    }

    public void next() {
        Customer temp;
        if (waitingRoomChairsFull != 0) {
            waitingRoomChairsFull--;
            temp = waitingRoomChairs[waitingRoomChairsFull];
            waitingRoomChairs[waitingRoomChairsFull] = null;
            StageService.sendMessage(theBarber, 5 /*cutHair*/, new Object[]{temp});
        }
        else {
            StageService.sendMessage(theBarber, 6 /*sleep*/, null);
            sleepingBarber = true;
        }

    }


    public static WaitingRoom construct(int constructor_id, Object[] arguments) {
        WaitingRoom actor = new WaitingRoom();
        StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }

    public static WaitingRoom construct(int constructor_id, Object[] arguments, int target_stage_id) {
        WaitingRoom actor = new WaitingRoom(target_stage_id);
        actor.getStage().putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }

    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
        WaitingRoom actor = new WaitingRoom();
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
        Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});
        return output_continuation;
    }

    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, int target_stage_id) {
        WaitingRoom actor = new WaitingRoom(target_stage_id);
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage_id);
        Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage_id);
        return output_continuation;
    }

}
