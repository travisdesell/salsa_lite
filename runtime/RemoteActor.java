package salsa_lite.runtime;


public abstract class RemoteActor extends Actor {

    public final int hashCode() {
        return Hashing.getHashCodeFor(name, host, port);
    }

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

        int hashCode = hashCode();
        this.stage = StageService.stages[Math.abs(hashCode % StageService.number_stages)];
        this.stage_id = stage.getStageId();

//        System.err.println("Created a remote actor at with pre-specified host: " + host + " and port: " + port);
    }

    public RemoteActor(String name) {
        super(false);
        this.name = name;
        this.host = TransportService.getHost();
        this.port = TransportService.getPort();

        int hashCode = hashCode();
        this.stage = StageService.stages[Math.abs(hashCode % StageService.number_stages)];
        this.stage_id = stage.getStageId();

        Object lock = RemoteActorRegistry.getLock(hashCode);
        synchronized (lock) {
            RemoteActorRegistry.addEntry(hashCode, this);
        }

//        System.err.println("Created a remote actor at local theater with host: " + host + " and port: " + port + ", and hashcode: " + hashCode);
    }

    public RemoteActor(String name, int stage_id) {
        super(false);
        this.name = name;
        this.host = TransportService.getHost();
        this.port = TransportService.getPort();

        int hashCode = hashCode();
        this.stage = StageService.getStage(stage_id);
        this.stage_id = stage_id;

        Object lock = RemoteActorRegistry.getLock(hashCode);
        synchronized (lock) {
            RemoteActorRegistry.addEntry(hashCode, this);
        }

//        System.err.println("Created a remote actor at local theater with host: " + host + " and port: " + port + ", and hashcode: " + hashCode);
    }
}
