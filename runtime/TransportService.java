package salsa_lite.runtime;

import salsa_lite.common.HashCodeBuilder;

import java.util.HashMap;

import salsa_lite.runtime.wwc.OutgoingTheaterConnection;
import salsa_lite.runtime.wwc.Theater;

public class TransportService {

    HashMap<Integer,OutgoingTheaterConnection> outgoingSockets = new HashMap<Integer,OutgoingTheaterConnection>();

//    Theater theater = Theater.construct(;

    public static final int getPort() {
        return theater.getPort();
    }

    public static final String getHost() {
        return theater.getHost();
    }

    public static final OutgoingTheaterConnection getSocket(String host, int port) {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(host);
        hcb.append(port);
        return outgoingSockets.get(hcb.toHashCode());
    }

    public static final void sendMessage(String host, int port, Message message) {
        OutgoingTheaterConnection out = getSocket(host, port);

        System.err.println("sending remote message to [" + host + " : " + port + "]: " + message);
    }

    public static final void migrateActor(String host, int port, Actor actor) {
        OutgoingTheaterConnection out = getSocket(host, port);

        System.err.println("migrating actor to [" + host + " : " + port + "]: " + actor.hashCode());
    }
}
