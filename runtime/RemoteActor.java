package salsa_lite.runtime;


public abstract class RemoteActor extends Actor {

    private String name;
    private String host;
    private int port;

    public String getName() { return name; }
    public String getHost() { return host; }
    public int getPort() { return port; }

    public String toString() { return "Remote Actor[type: " + getClass().getName() + ", stage: " + stage.getStageId() + ", " + host + ":" + port + "/" + name + "]"; }

    public RemoteActor() { super(); }
    public RemoteActor(SynchronousMailboxStage stage) { super(stage); }
}
