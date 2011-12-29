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
        super( MobileActorRegistry.getHashCodeFor(name, nameserver.getName(), nameserver.getHost(), nameserver.getPort()) );
        this.lastKnownHost = TransportService.getHost();
        this.lastKnownPort = TransportService.getPort();
    }

    public MobileActor(String name, NameServer nameserver, SynchronousMailboxStage stage) {
        super( MobileActorRegistry.getHashCodeFor(name, nameserver.getName(), nameserver.getHost(), nameserver.getPort()), stage );
        this.lastKnownHost = TransportService.getHost();
        this.lastKnownPort = TransportService.getPort();
    }

    public abstract static class State extends Actor implements java.io.Serializable {
        public State(String name, NameServer nameserver) {
            super( MobileActorRegistry.getHashCodeFor(name, nameserver.getName(), nameserver.getHost(), nameserver.getPort()) );
            this.host = TransportService.getHost();
            this.port = TransportService.getPort();
        }

        public State(String name, NameServer nameserver, SynchronousMailboxStage stage) {
            super( MobileActorRegistry.getHashCodeFor(name, nameserver.getName(), nameserver.getHost(), nameserver.getPort()), stage );
            this.host = TransportService.getHost();
            this.port = TransportService.getPort();
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
