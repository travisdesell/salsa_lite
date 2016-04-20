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

import salsa_lite.runtime.language.JoinDirector;

public class Barber extends salsa_lite.runtime.Actor implements java.io.Serializable {


    public Object writeReplace() throws java.io.ObjectStreamException {
        int hashCode = Hashing.getHashCodeFor(this.hashCode(), TransportService.getHost(), TransportService.getPort());
        synchronized (LocalActorRegistry.getLock(hashCode)) {
            LocalActorRegistry.addEntry(hashCode, this);
        }
        return new SerializedBarber( this.hashCode(), TransportService.getHost(), TransportService.getPort() );
    }

    public static class RemoteReference extends Barber {
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
            return new SerializedBarber( hashCode, host, port);
        }
    }

    public static class SerializedBarber implements java.io.Serializable {
        int hashCode;
        String host;
        int port;

        SerializedBarber(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }

        public Object readResolve() throws java.io.ObjectStreamException {
            int hashCode = Hashing.getHashCodeFor(this.hashCode, this.host, this.port);
            synchronized (LocalActorRegistry.getLock(hashCode)) {
                Barber actor = (Barber)LocalActorRegistry.getEntry(hashCode);
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
    		case 3: return "ack [salsa_lite.benchmarks.barber.Barber].Barber(int, int, salsa_lite.runtime.language.JoinDirector)";
    		case 4: return "ack [salsa_lite.benchmarks.barber.Barber].setWaitingRoom(salsa_lite.benchmarks.barber.WaitingRoom)";
    		case 5: return "ack [salsa_lite.benchmarks.barber.Barber].cutHair(salsa_lite.benchmarks.barber.Customer)";
    		case 6: return "ack [salsa_lite.benchmarks.barber.Barber].sleep()";
    		case 7: return "ack [salsa_lite.benchmarks.barber.Barber].exit()";
    	}
    	return "No message with specified id.";
    }

    public Barber() { super(); }
    public Barber(int stage_id) { super(stage_id); }

    WaitingRoom theWaitingRoom;
    JoinDirector customerJoin;
    JoinDirector jd;


    public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {
        switch(messageId) {
            case 0: return toString();
            case 1: return hashCode();
            case 2: return getStageId();
            case 3: Barber( (Integer)arguments[0], (Integer)arguments[1], (JoinDirector)arguments[2] ); return null;
            case 4: setWaitingRoom( (WaitingRoom)arguments[0] ); return null;
            case 5: cutHair( (Customer)arguments[0] ); return null;
            case 6: sleep(); return null;
            case 7: exit(); return null;
            default: throw new MessageHandlerNotFoundException(messageId, arguments);
        }
    }

    public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {
        switch(messageId) {
            case 0: construct(); return;
            case 1: construct( (String[])arguments[0] ); return;
            default: throw new ConstructorNotFoundException(messageId, arguments);
        }
    }

    public void construct() {
    }

    public void construct(String[] args) {
        int n = Integer.parseInt(args[0]);
        int w = Integer.parseInt(args[1]);
        Barber b = Barber.construct(0, null);
        JoinDirector j = JoinDirector.construct(0, null);
        StageService.sendMessage(b, 3 /*Barber*/, new Object[]{n, w, j});
        ContinuationDirector continuation_token = StageService.sendContinuationMessage(j, 4 /*resolveAfter*/, new Object[]{1});
        StageService.sendMessage(((salsa_lite.benchmarks.barber.Barber)this), 7 /*exit*/, null, continuation_token);
    }



    public void Barber(int n, int w, JoinDirector j) {
        jd = j;
        Barber b = ((salsa_lite.benchmarks.barber.Barber)this);
        customerJoin = JoinDirector.construct(0, null);
        WaitingRoom wr = WaitingRoom.construct(0, new Object[]{w, b});
        StageService.sendMessage(b, 4 /*setWaitingRoom*/, new Object[]{wr});
        Customer[] tempC = new Customer[n];
        for (int i = 0; i < n; i++) {
            tempC[i] = Customer.construct(0, new Object[]{i, wr, customerJoin});
            StageService.sendMessage(tempC[i], 3 /*leave*/, null);
        }

        ContinuationDirector continuation_token = StageService.sendContinuationMessage(customerJoin, 4 /*resolveAfter*/, new Object[]{n});
        StageService.sendMessage(jd, 3 /*join*/, null, continuation_token);
    }

    public void setWaitingRoom(WaitingRoom w) {
        theWaitingRoom = w;
    }

    public void cutHair(Customer c) {
        StageService.sendMessage(c, 5 /*done*/, null);
        StageService.sendMessage(theWaitingRoom, 4 /*next*/, null);
    }

    public void sleep() {
    }

    public void exit() {
        System.exit(0);
    }


    public static void main(String[] arguments) {
        Barber.construct(1, new Object[]{arguments});
    }

    public static Barber construct(int constructor_id, Object[] arguments) {
        Barber actor = new Barber();
        StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }

    public static Barber construct(int constructor_id, Object[] arguments, int target_stage_id) {
        Barber actor = new Barber(target_stage_id);
        actor.getStage().putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }

    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
        Barber actor = new Barber();
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
        Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});
        return output_continuation;
    }

    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, int target_stage_id) {
        Barber actor = new Barber(target_stage_id);
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage_id);
        Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage_id);
        return output_continuation;
    }

}
