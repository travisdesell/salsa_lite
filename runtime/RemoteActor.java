package salsa_lite.runtime;


public abstract class RemoteActor extends Actor {

    private String name;
    private String host;
    private int port;

    public String getName() { return name; }
    public String getHost() { return host; }
    public int getPort() { return port; }

    public String toString() { return "Remote Actor[type: " + getClass().getName() + ", stage: " + stage.getStageId() + ", " + host + ":" + port + "/" + name + "]"; }

    public RemoteActor(String name, String host, int port) {
        super(false);
        this.name = name;

        if (host.equals("127.0.0.1") || host.equals("localhost")) {
            host = TransportService.getHost();
        }
        this.host = host;
        this.port = port;
        this.hashCode = Hashing.getHashCodeFor(name, host, port);
        this.stage = StageService.stages[Math.abs(hashCode % StageService.number_stages)];

//        System.err.println("Created a remote actor at with pre-specified host: " + host + " and port: " + port);
    }

    public RemoteActor(String name) {
        super(false);
        this.name = name;
        this.host = TransportService.getHost();
        this.port = TransportService.getPort();
        this.hashCode = Hashing.getHashCodeFor(name, host, port);
        this.stage = StageService.stages[Math.abs(hashCode % StageService.number_stages)];

        Object lock = RemoteActorRegistry.getLock(hashCode);
        synchronized (lock) {
            RemoteActorRegistry.addEntry(hashCode, this);
        }

//        System.err.println("Created a remote actor at local theater with host: " + host + " and port: " + port);
    }

    public RemoteActor(String name, SynchronousMailboxStage stage) {
        super(false);
        this.name = name;
        this.host = TransportService.getHost();
        this.port = TransportService.getPort();
        this.hashCode = Hashing.getHashCodeFor(name, host, port);
        this.stage = stage;

        Object lock = RemoteActorRegistry.getLock(hashCode);
        synchronized (lock) {
            RemoteActorRegistry.addEntry(hashCode, this);
        }

//        System.err.println("Created a remote actor at local theater with host: " + host + " and port: " + port);
    }
}
