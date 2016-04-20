package salsa_lite.benchmarks.Big;

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

public class Big extends salsa_lite.runtime.Actor implements java.io.Serializable {


    public Object writeReplace() throws java.io.ObjectStreamException {
        int hashCode = Hashing.getHashCodeFor(this.hashCode(), TransportService.getHost(), TransportService.getPort());
        synchronized (LocalActorRegistry.getLock(hashCode)) {
            LocalActorRegistry.addEntry(hashCode, this);
        }
        return new SerializedBig( this.hashCode(), TransportService.getHost(), TransportService.getPort() );
    }

    public static class RemoteReference extends Big {
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
            return new SerializedBig( hashCode, host, port);
        }
    }

    public static class SerializedBig implements java.io.Serializable {
        int hashCode;
        String host;
        int port;

        SerializedBig(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }

        public Object readResolve() throws java.io.ObjectStreamException {
            int hashCode = Hashing.getHashCodeFor(this.hashCode, this.host, this.port);
            synchronized (LocalActorRegistry.getLock(hashCode)) {
                Big actor = (Big)LocalActorRegistry.getEntry(hashCode);
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
    		case 3: return "ack [salsa_lite.benchmarks.Big.Big].Big(int, int, salsa_lite.runtime.language.JoinDirector)";
    		case 4: return "ack [salsa_lite.benchmarks.Big.Big].setOther(Big[])";
    		case 5: return "ack [salsa_lite.benchmarks.Big.Big].send(int)";
    		case 6: return "ack [salsa_lite.benchmarks.Big.Big].pongMessage(int, int)";
    		case 7: return "ack [salsa_lite.benchmarks.Big.Big].pingMessage(int, int)";
    		case 8: return "ack [salsa_lite.benchmarks.Big.Big].finished()";
    		case 9: return "ack [salsa_lite.benchmarks.Big.Big].exit()";
    	}
    	return "No message with specified id.";
    }

    public Big() { super(); }
    public Big(int stage_id) { super(stage_id); }

    int n_others;
    int id;
    Big[] others;
    Big source;
    JoinDirector jd_runner;


    public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {
        switch(messageId) {
            case 0: return toString();
            case 1: return hashCode();
            case 2: return getStageId();
            case 3: Big( (Integer)arguments[0], (Integer)arguments[1], (JoinDirector)arguments[2] ); return null;
            case 4: setOther( (Big[])arguments[0] ); return null;
            case 5: send( (Integer)arguments[0] ); return null;
            case 6: pongMessage( (Integer)arguments[0], (Integer)arguments[1] ); return null;
            case 7: pingMessage( (Integer)arguments[0], (Integer)arguments[1] ); return null;
            case 8: finished(); return null;
            case 9: exit(); return null;
            default: throw new MessageHandlerNotFoundException(messageId, arguments);
        }
    }

    public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {
        switch(messageId) {
            case 0: construct( (Integer)arguments[0], (Big)arguments[1] ); return;
            case 1: construct( (String[])arguments[0] ); return;
            default: throw new ConstructorNotFoundException(messageId, arguments);
        }
    }

    public void construct(int id, Big source) {
        ((salsa_lite.benchmarks.Big.Big)this).id = id;
        ((salsa_lite.benchmarks.Big.Big)this).source = source;
    }

    public void construct(String[] args) {
        JoinDirector j = JoinDirector.construct(0, null);
        int p = Integer.parseInt(args[1]);
        int n = Integer.parseInt(args[0]);
        StageService.sendMessage(((salsa_lite.benchmarks.Big.Big)this), 3 /*Big*/, new Object[]{n, p, j});
        ContinuationDirector continuation_token = StageService.sendContinuationMessage(j, 4 /*resolveAfter*/, new Object[]{1});
        StageService.sendMessage(((salsa_lite.benchmarks.Big.Big)this), 9 /*exit*/, null, continuation_token);
    }



    public void Big(int n, int p, JoinDirector j) {
        Big[] actors = new Big[p];
        ((salsa_lite.benchmarks.Big.Big)this).jd_runner = j;
        n_others = p;
        for (int i = 0; i < p; i++) {
            actors[i] = Big.construct(0, new Object[]{i, ((salsa_lite.benchmarks.Big.Big)this)});
        }

        JoinDirector jd = JoinDirector.construct(0, null);
        for (int i = 0; i < p; i++) {
            ContinuationDirector continuation_token = StageService.sendContinuationMessage(actors[i], 4 /*setOther*/, new Object[]{(Big[])DeepCopy.deepCopy( actors )});
            StageService.sendMessage(jd, 3 /*join*/, null, continuation_token);
        }

        ContinuationDirector finished = StageService.sendContinuationMessage(jd, 4 /*resolveAfter*/, new Object[]{p});
        for (int i = 0; i < p; i++) {
            StageService.sendMessage(actors[i], 5 /*send*/, new Object[]{n}, new Director[]{finished});
        }

    }

    public void setOther(Big[] others) {
        ((salsa_lite.benchmarks.Big.Big)this).others = others;
    }

    public void send(int number) {
        int rNumber = (int)(Math.random() * others.length);
        StageService.sendMessage(others[rNumber], 7 /*pingMessage*/, new Object[]{((salsa_lite.benchmarks.Big.Big)this).id, number});
    }

    public void pongMessage(int id, int number) {
        number--;
        if (number > 0) {
            int rNumber = (int)(Math.random() * others.length);
            StageService.sendMessage(others[rNumber], 7 /*pingMessage*/, new Object[]{((salsa_lite.benchmarks.Big.Big)this).id, number});
        }
        else {
            StageService.sendMessage(source, 8 /*finished*/, null);
        }

    }

    public void pingMessage(int id, int number) {
        StageService.sendMessage(others[id], 6 /*pongMessage*/, new Object[]{((salsa_lite.benchmarks.Big.Big)this).id, number});
    }

    public void finished() {
        n_others--;
        if (n_others == 0) {
            StageService.sendMessage(jd_runner, 3 /*join*/, null);
        }
        
    }

    public void exit() {
        System.exit(0);
    }


    public static void main(String[] arguments) {
        Big.construct(1, new Object[]{arguments});
    }

    public static Big construct(int constructor_id, Object[] arguments) {
        Big actor = new Big();
        StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }

    public static Big construct(int constructor_id, Object[] arguments, int target_stage_id) {
        Big actor = new Big(target_stage_id);
        actor.getStage().putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
        return actor;
    }

    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
        Big actor = new Big();
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
        Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});
        return output_continuation;
    }

    public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, int target_stage_id) {
        Big actor = new Big(target_stage_id);
        TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage_id);
        Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);
        MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage_id);
        return output_continuation;
    }

}
