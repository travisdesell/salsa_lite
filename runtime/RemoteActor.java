package salsa_lite.runtime;


public abstract class RemoteActor extends Actor {

    private String name;
    private String host;
    private int port;

    public String getName() { return name; }
    public String getHost() { return host; }
    public int getPort() { return port; }

    public RemoteActor() { super(); }
    public RemoteActor(SynchronousMailboxStage stage) { super(stage); }
}
