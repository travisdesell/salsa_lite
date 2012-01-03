package salsa_lite.runtime;

import salsa_lite.runtime.wwc.NameServer;

public abstract class MobileActor extends Actor {

    private String name;
    private String lastKnownHost;
    private int lastKnownPort;

    private NameServer nameserver;

    public NameServer getNameServer() { return nameserver; }

    public String getName() { return name; }
    public String getLastKnownHost() { return lastKnownHost; }
    public int getLastKnownPort() { return lastKnownPort; }


    public String getHost() { throw new RuntimeException("EXCEPTION: cannot get the host of a MobileActor (as it may have moved).  Use the getLastKnownHost() method to get it's last known host."); }
    public int getPort() { throw new RuntimeException("EXCEPTION: cannot get the port of a MobileActor (as it may have moved).  Use the getLastKnownPort() method to get it's last known port."); }

    public String toString() { return "Mobile Actor[type: " + getClass().getName() + ", name: " + name + ", stage: " + stage.getStageId() + ", lastKnownHost: " + lastKnownHost + ":" + lastKnownPort + ", name: " + name + ", nameserver: " + nameserver + "]"; }


    public MobileActor(String name, NameServer nameserver) {
        super(false);
        this.name = name;
        this.nameserver = nameserver;
        this.lastKnownHost = TransportService.getHost();
        this.lastKnownPort = TransportService.getPort();
        this.hashCode = Hashing.getHashCodeFor(name, nameserver.getName(), nameserver.getHost(), nameserver.getPort());
        this.stage = StageService.stages[Math.abs(hashCode % StageService.number_stages)];

        Object lock = MobileActorRegistry.getReferenceLock(hashCode);
        synchronized (lock) {
            MobileActorRegistry.addReferenceEntry(hashCode, this);
        }

        System.err.println("Created a mobile actor at local theater with lastKnownHost: " + lastKnownHost + " and lastKnownPort: " + lastKnownPort);
    }

    public MobileActor(String name, NameServer nameserver, SynchronousMailboxStage stage) {
        super(false);
        this.name = name;
        this.nameserver = nameserver;
        this.lastKnownHost = TransportService.getHost();
        this.lastKnownPort = TransportService.getPort();
        this.hashCode = Hashing.getHashCodeFor(name, nameserver.getName(), nameserver.getHost(), nameserver.getPort());
        this.stage = stage;

        Object lock = MobileActorRegistry.getReferenceLock(hashCode);
        synchronized (lock) {
            MobileActorRegistry.addReferenceEntry(hashCode, this);
        }

        System.err.println("Created a mobile actor at local theater with lastKnownHost: " + lastKnownHost + " and lastKnownPort: " + lastKnownPort);
    }

    public MobileActor(String name, NameServer nameserver, String lastKnownHost, int lastKnownPort) { 
        super(false);
        this.name = name;
        this.nameserver = nameserver;
        this.lastKnownHost = lastKnownHost;
        this.lastKnownPort = lastKnownPort;
        this.hashCode = Hashing.getHashCodeFor(name, nameserver.getName(), nameserver.getHost(), nameserver.getPort());
        this.stage = StageService.stages[Math.abs(hashCode % StageService.number_stages)];

        System.err.println("Created a mobile actor with pre-specified lastKnownHost: " + lastKnownHost + " and lastKnownPort: " + lastKnownPort);
    }


    public abstract static class State extends Actor implements java.io.Serializable {
        public State(String name, NameServer nameserver) {
            super(false);
            this.host = TransportService.getHost();
            this.port = TransportService.getPort();
            this.hashCode = Hashing.getHashCodeFor(name, nameserver.getName(), nameserver.getHost(), nameserver.getPort());
            this.stage = StageService.stages[Math.abs(hashCode % StageService.number_stages)];
        }

        public State(String name, NameServer nameserver, SynchronousMailboxStage stage) {
            super(false);
            this.host = TransportService.getHost();
            this.port = TransportService.getPort();
            this.hashCode = Hashing.getHashCodeFor(name, nameserver.getName(), nameserver.getHost(), nameserver.getPort());
            this.stage = stage;
        }

        private String name = null;
        private String host;
        private int port;

        public String getName() { return name; }

        public String getHost() { return host; }
        public int getPort() { return port; }

        public String getLastKnownHost() { return host; }
        public int getLastKnownPort() { return port; }
    }
}
