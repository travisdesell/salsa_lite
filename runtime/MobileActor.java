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

    public String getOriginHost() { return origin_host; }
    public int getOriginPort() { return origin_port; }

    public String toString() { return "Mobile Actor[type: " + getClass().getName() + ", stage: " + stage.getStageId() + ", origin: " + origin_host + ":" + origin_port + ", current: " + host + ":" + port + ", name: " + name + "]"; }

    public MobileActor() { super(); }
    public MobileActor(SynchronousMailboxStage stage) { super(stage); }

    public abstract static class State extends Actor implements java.io.Serializable {
        public State() { super(); }
        public State(SynchronousMailboxStage stage) { super(stage); }
    }
}
