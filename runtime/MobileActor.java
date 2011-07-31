package salsa_lite.runtime;


public abstract class MobileActor extends Actor {

    public String host;
    public int port;

    public MobileActor() { super(); }
    public MobileActor(SynchronousMailboxStage stage) { super(stage); }

    public abstract static class State extends Actor implements java.io.Serializable {
        public State() { super(); }
        public State(SynchronousMailboxStage stage) { super(stage); }
    }
}
