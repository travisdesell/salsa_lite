package salsa_lite.benchmarks.cigsmok;

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


public class Arbiter extends salsa_lite.runtime.Actor implements java.io.Serializable {


    public Object writeReplace() throws java.io.ObjectStreamException {
        int hashCode = Hashing.getHashCodeFor(this.hashCode(), TransportService.getHost(), TransportService.getPort());
        synchronized (LocalActorRegistry.getLock(hashCode)) {
            LocalActorRegistry.addEntry(hashCode, this);
        }
        return new SerializedArbiter( this.hashCode(), TransportService.getHost(), TransportService.getPort() );
    }

    public static class RemoteReference extends Arbiter {
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
            return new SerializedArbiter( hashCode, host, port);
        }
    }

    public static class SerializedArbiter implements java.io.Serializable {
        int hashCode;
        String host;
        int port;

        SerializedArbiter(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }

        public Object readResolve() throws java.io.ObjectStreamException {
            int hashCode = Hashing.getHashCodeFor(this.hashCode, this.host, this.port);
            synchronized (LocalActorRegistry.getLock(hashCode)) {
                Arbiter actor = (Arbiter)LocalActorRegistry.getEntry(hashCode);
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
    		case 3: return "ack [salsa_lite.benchmarks.cigsmok.Arbiter].StartedSmoking()";
    		case 4: return "ack [salsa_lite.benchmarks.cigsmok.Arbiter].nextRound()";
    	}
    	return "No message with specified id.";
    }

    public Arbiter() { super(); }
    public Arbiter(int stage_id) { super(stage_id); }

    int totalSmoked = 0;
    int numRounds;
    int numSmokers = 0;
    Smoker[] smokers;


    public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {
        switch(messageId) {
            case 0: return toString();
            case 1: return hashCode();
            case 2: return getStageId();
            case 3: StartedSmoking(); return null;
            case 4: nextRound(); return null;
            default: throw new MessageHandlerNotFoundException(messageId, arguments);
        }
    }

    public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {
        switch(messageId) {
            case 0: construct( (String[])arguments[0] ); return;
            default: throw new ConstructorNotFoundException(messageId, arguments);
        }
    }

    public void construct() {}

    public void construct(String[] args) {
        int n = Integer.parseInt(args[0]);
        int p = Integer.parseInt(args[1]);
        numRounds = p;
        numSmokers = n;
        smokers = new Smoker[n];
        for (int i = 0; i < n; i++) {
            smokers[i] = Smoker.construct(0, new Object[]{((salsa_lite.benchmarks.cigsmok.Arbiter)this), i});
        }

        StageService.sendMessage(((salsa_lite.benchmarks.cigsmok.Arbiter)this), 3 /*StartedSmoking*/, null);
    }



    public void StartedSmoking() {
        if (totalSmoked++ == numRounds) {
            System.exit(0);
        }
        
        StageService.sendMessage(((salsa_lite.benchmarks.cigsmok.Arbiter)this), 4 /*nextRound*/, null);
    }

    public void nextRound() {
        long r = new Long( 0 );
        int rSmoker = (int)(Math.random() * numSmokers);
        StageService.sendMessage(smokers[rSmoker], 3 /*StartSmoke*/, new Object[]{r});
    }


    public static void main(String[] arguments) {
        Arbiter.construct(0, new Object[]{arguments});
    }

    public static Arbiter construct(int constructor_id, Object[] arguments) {
        Arbiter actor = new Arbiter();
        StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }

    public static Arbiter construct(int constructor_id, Object[] arguments, int target_stage_id) {
        Arbiter actor = new Arbiter(target_stage_id);
        actor.getStage().putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }

    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
        Arbiter actor = new Arbiter();
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
        Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});
        return output_continuation;
    }

    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, int target_stage_id) {
        Arbiter actor = new Arbiter(target_stage_id);
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage_id);
        Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage_id);
        return output_continuation;
    }

}
