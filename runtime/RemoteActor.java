package salsa_lite.runtime;


public abstract class RemoteActor extends Actor {

    private String name;
    private String host;
    private int port;

    public String getName() { return name; }
    public String getHost() { return host; }
    public int getPort() { return port; }

    public String toString() { return "Remote Actor[type: " + getClass().getName() + ", stage: " + stage.getStageId() + ", " + host + ":" + port + "/" + name + "]"; }

    public RemoteActor(int hashCode, String name, String host, int port) {
        super(hashCode);
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public RemoteActor(String name) {
        super( ActorRegistry.getHashCodeFor(name, TransportService.getHost(), TransportService.getPort()) );
        this.name = name;
        this.host = TransportService.getHost();
        this.port = TransportService.getPort();
    }

    public RemoteActor(String name, SynchronousMailboxStage stage) {
        super( ActorRegistry.getHashCodeFor(name, TransportService.getHost(), TransportService.getPort()), stage );
        this.name = name;
        this.host = TransportService.getHost();
        this.port = TransportService.getPort();
    }
}
