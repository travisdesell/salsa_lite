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
        this.lastKnownHost = TransportService.getHost();
        this.lastKnownPort = TransportService.getPort();
        this.hashCode = MobileActorRegistry.getHashCodeFor(name, nameserver.getName(), nameserver.getHost(), nameserver.getPort());
        this.stage = StageService.stages[Math.abs(this.hashCode() % StageService.number_stages)];
    }

    public MobileActor(String name, NameServer nameserver, SynchronousMailboxStage stage) {
        this.lastKnownHost = TransportService.getHost();
        this.lastKnownPort = TransportService.getPort();
        this.hashCode = MobileActorRegistry.getHashCodeFor(name, nameserver.getName(), nameserver.getHost(), nameserver.getPort());
        this.stage = stage;
    }

    public abstract static class State extends Actor implements java.io.Serializable {
        public State() { super(); }
        public State(SynchronousMailboxStage stage) { super(stage); }

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
