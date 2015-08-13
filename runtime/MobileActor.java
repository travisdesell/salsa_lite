package salsa_lite.runtime;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import salsa_lite.runtime.wwc.NameServer;

public abstract class MobileActor extends Actor {

    private final int hashCode;
    public final int hashCode() {
        return hashCode;
    }

    private String name;
    private String lastKnownHost;
    private int lastKnownPort;

    //private NameServer nameserver;


    private String nameserverName;
    private String nameserverHost;
    private int nameserverPort;

    public String getNameServerName() { return nameserverName; }
    public String getNameServerHost() { return nameserverHost; }
    public int getNameServerPort() { return nameserverPort; }
    public NameServer getNameServer() { return NameServer.getRemoteReference(nameserverName, nameserverHost, nameserverPort); }

    public String getName() { return name; }
    public String getLastKnownHost() { return lastKnownHost; }
    public int getLastKnownPort() { return lastKnownPort; }


    public String getHost() { throw new RuntimeException("EXCEPTION: cannot get the host of a MobileActor (as it may have moved).  Use the getLastKnownHost() method to get it's last known host."); }
    public int getPort() { throw new RuntimeException("EXCEPTION: cannot get the port of a MobileActor (as it may have moved).  Use the getLastKnownPort() method to get it's last known port."); }

    public String toString() { return "Mobile Actor[type: " + getClass().getName() + ", name: " + name + ", stage: " + stage.getStageId() + ", lastKnownHost: " + lastKnownHost + ":" + lastKnownPort + ", name: " + name + ", nameserver: " + nameserverHost + ":" + nameserverPort + "/" + nameserverName  + "]"; }


    public MobileActor(String name, String nameserverName, String nameserverHost, int nameserverPort) {
        super(false);
        this.name = name;
        this.nameserverName = nameserverName;
        this.nameserverHost = nameserverHost;
        this.nameserverPort = nameserverPort;
        this.lastKnownHost = TransportService.getHost();
        this.lastKnownPort = TransportService.getPort();

        this.hashCode = Hashing.getHashCodeFor(name, nameserverName, nameserverHost, nameserverPort);
        this.stage = StageService.stages[Math.abs(hashCode % StageService.number_stages)];
        this.stage_id = stage.getStageId();

        Object lock = MobileActorRegistry.getReferenceLock(hashCode);
        synchronized (lock) {
            MobileActorRegistry.addReferenceEntry(hashCode, this);
        }

//        System.err.println("Created a mobile actor at local theater with lastKnownHost: " + lastKnownHost + " and lastKnownPort: " + lastKnownPort);
    }

    public MobileActor(String name, String nameserverName, String nameserverHost, int nameserverPort, int stage_id) {
        super(false);
        this.name = name;
        this.nameserverName = nameserverName;
        this.nameserverHost = nameserverHost;
        this.nameserverPort = nameserverPort;
        this.lastKnownHost = TransportService.getHost();
        this.lastKnownPort = TransportService.getPort();

        this.hashCode = Hashing.getHashCodeFor(name, nameserverName, nameserverHost, nameserverPort);
        this.stage = StageService.getStage(stage_id);
        this.stage_id = stage_id;

        Object lock = MobileActorRegistry.getReferenceLock(hashCode);
        synchronized (lock) {
            MobileActorRegistry.addReferenceEntry(hashCode, this);
        }

//        System.err.println("Created a mobile actor at local theater with lastKnownHost: " + lastKnownHost + " and lastKnownPort: " + lastKnownPort);
    }

    public MobileActor(String name, String nameserverName, String nameserverHost, int nameserverPort, String lastKnownHost, int lastKnownPort) { 
        super(false);
        this.name = name;
        this.nameserverName = nameserverName;
        this.nameserverHost = nameserverHost;
        this.nameserverPort = nameserverPort;
        this.lastKnownHost = lastKnownHost;
        this.lastKnownPort = lastKnownPort;

        this.hashCode = Hashing.getHashCodeFor(name, nameserverName, nameserverHost, nameserverPort);
        this.stage = StageService.stages[Math.abs(hashCode % StageService.number_stages)];
        this.stage_id = stage_id;

//        System.err.println("Created a mobile actor with pre-specified lastKnownHost: " + lastKnownHost + " and lastKnownPort: " + lastKnownPort);
    }

    public MobileActor(String name, String nameserverName, String nameserverHost, int nameserverPort, String lastKnownHost, int lastKnownPort, int stage_id) { 
        super(false);
        this.name = name;
        this.nameserverName = nameserverName;
        this.nameserverHost = nameserverHost;
        this.nameserverPort = nameserverPort;
        this.lastKnownHost = lastKnownHost;
        this.lastKnownPort = lastKnownPort;

        this.hashCode = Hashing.getHashCodeFor(name, nameserverName, nameserverHost, nameserverPort);
        this.stage = StageService.getStage(stage_id);
        this.stage_id = stage_id;

//        System.err.println("Created a mobile actor with pre-specified lastKnownHost: " + lastKnownHost + " and lastKnownPort: " + lastKnownPort);
    }



    public abstract static class State extends Actor implements java.io.Serializable {
        public final int hashCode() {
            return Hashing.getHashCodeFor(name, nameserverName, nameserverHost, nameserverPort);
        }

        private String nameserverName;
        private String nameserverHost;
        private int nameserverPort;

        private String name = null;
        private String originHost;
        private int originPort;
        protected String host;
        protected int port;

        public NameServer getNameServer() { return NameServer.getRemoteReference(nameserverName, nameserverHost, nameserverPort); }
        public String getNameServerName() { return nameserverName; }
        public String getNameServerHost() { return nameserverHost; }
        public int getNameServerPort() { return nameserverPort; }

        public String getName() { return name; }

        public String getOriginHost() { return originHost; }
        public int getOriginPort() { return originPort; }

        public String getHost() { return host; }
        public int getPort() { return port; }

        public String getLastKnownHost() { return host; }
        public int getLastKnownPort() { return port; }

        public State(String name, String nameserverName, String nameserverHost, int nameserverPort) {
            super(false);
            this.name = name;
            this.originHost = TransportService.getHost();
            this.originPort = TransportService.getPort();
            this.host = originHost;
            this.port = originPort;
            this.nameserverName = nameserverName;
            this.nameserverHost = nameserverHost;
            this.nameserverPort = nameserverPort;

            int hashCode = hashCode();
            this.stage = StageService.stages[Math.abs(hashCode % StageService.number_stages)];
            this.stage_id = stage_id;

            synchronized (MobileActorRegistry.getStateLock(hashCode)) {
                MobileActorRegistry.addStateEntry(hashCode, this);
            }
        }

        public State(String name, String nameserverName, String nameserverHost, int nameserverPort, int stage_id) {
            super(false);
            this.name = name;
            this.originHost = TransportService.getHost();
            this.originPort = TransportService.getPort();
            this.host = originHost;
            this.port = originPort;
            this.nameserverName = nameserverName;
            this.nameserverHost = nameserverHost;
            this.nameserverPort = nameserverPort;
            this.stage = StageService.getStage(stage_id);
            this.stage_id = stage_id;

            int hashCode = hashCode();
            synchronized (MobileActorRegistry.getStateLock(hashCode)) {
                MobileActorRegistry.addStateEntry(hashCode, this);
            }
        }

        public State(String name, String nameserverName, String nameserverHost, int nameserverPort, String host, int port) {
            super(false);
            this.name = name;
            this.originHost = TransportService.getHost();
            this.originPort = TransportService.getPort();
            this.host = host;
            this.port = port;
            this.nameserverName = nameserverName;
            this.nameserverHost = nameserverHost;
            this.nameserverPort = nameserverPort;

            int hashCode = hashCode();
            this.stage = StageService.stages[Math.abs(hashCode % StageService.number_stages)];
            this.stage_id = stage_id;

            synchronized (MobileActorRegistry.getStateLock(hashCode)) {
                MobileActorRegistry.addStateEntry(hashCode, this);
            }
        }


        public State(String name, String nameserverName, String nameserverHost, int nameserverPort, String host, int port, int stage_id) {
            super(false);
            this.name = name;
            this.originHost = TransportService.getHost();
            this.originPort = TransportService.getPort();
            this.host = host;
            this.port = port;
            this.nameserverName = nameserverName;
            this.nameserverHost = nameserverHost;
            this.nameserverPort = nameserverPort;
            this.stage = StageService.getStage(stage_id);
            this.stage_id = stage_id;

            int hashCode = hashCode();
            synchronized (MobileActorRegistry.getStateLock(hashCode)) {
                MobileActorRegistry.addStateEntry(hashCode, this);
            }
        }


        private void writeObject(ObjectOutputStream out) throws IOException {
//            if (out instanceof salsa_lite.common.LocalObjectOutputStream) {
//                System.err.println("local write of mobile actor");
//            } else {
//                System.err.println("remote write of mobile actor");
//            }

            out.writeObject(nameserverName);
            out.writeObject(nameserverHost);
            out.writeInt(nameserverPort);
            out.writeObject(name);
            out.writeInt(originPort);
            out.writeObject(originHost);
            out.writeInt(port);
            out.writeObject(host);
            out.writeInt(stage_id);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            this.nameserverName = (String)in.readObject();
            this.nameserverHost = (String)in.readObject();
            this.nameserverPort = in.readInt();
            this.name = (String)in.readObject();
            this.originPort = in.readInt();
            this.originHost = (String)in.readObject();
            this.port = in.readInt();
            this.host = (String)in.readObject();
            this.stage_id = in.readInt();

            this.stage = StageService.getStage(stage_id);
        }
    }
}
