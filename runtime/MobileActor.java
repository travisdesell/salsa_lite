package salsa_lite.runtime;


public abstract class MobileActor extends Actor {

    private String name = null;
    private String origin_host;
    private int origin_port;

    private String host;
    private int port;

    public String getName() { return name; }
    public String getHost() { return host; }
    public int getPort() { return port; }

    public MobileActor() { super(); }
    public MobileActor(SynchronousMailboxStage stage) { super(stage); }

    public abstract static class State extends Actor implements java.io.Serializable {
        public State() { super(); }
        public State(SynchronousMailboxStage stage) { super(stage); }
    }
}
