package salsa_lite.runtime;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashMap;

import salsa_lite.common.HashCodeBuilder;

import salsa_lite.runtime.wwc.OutgoingTheaterConnection;
import salsa_lite.runtime.wwc.Theater;
import salsa_lite.runtime.wwc.NameServer;

public class TransportService {

    private static final int port;
    private static final String host;

    public static final int getPort() { return port; }
    public static final String getHost() { return host; }
    public static ServerSocket serverSocket;
    public static NameServer nameServer;

    private static final HashMap<Integer,OutgoingTheaterConnection> outgoingSockets = new HashMap<Integer,OutgoingTheaterConnection>();

    static {
        String portString = System.getProperty("port");
        if (portString != null) {
            port = Integer.parseInt(portString);
        } else {
            port = 4040;
        }

        InetAddress localHost = null;
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException uhe) {
            StringBuffer sb = new StringBuffer();
            sb.append("Error in TransportService, UknownHostException when finding local host: " + uhe + "\n"); 
            for (StackTraceElement ste : uhe.getStackTrace()) {
                sb.append("\t" + ste.toString() + "\n");
            }
            System.err.println(sb.toString());
            System.exit(0);
        }

        host = localHost.getHostAddress();
    }

    public static final void initialize() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ioe) {
            StringBuffer sb = new StringBuffer();
            sb.append("Error in Theater, IOException: " + ioe + "\n");
            for (StackTraceElement ste : ioe.getStackTrace()) {
                sb.append("\t" + ste.toString() + "\n");
            }   
            System.err.println(sb.toString());
        }

        System.err.println("TransportService started on host [" + host + "] and port [" + port + "]");

        Theater.construct(0, new Object[]{}, "runtime/theater");
        nameServer = NameServer.construct(0, new Object[]{}, "runtime/nameserver");
    }

    public synchronized static final NameServer getNameServer() {
        return nameServer;
    }

    public synchronized static final OutgoingTheaterConnection getSocket(String host, int port) {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(host);
        hcb.append(port);

        int hashCode = hcb.toHashCode();
        OutgoingTheaterConnection out = outgoingSockets.get(hashCode);

        if (out == null) {
            out = OutgoingTheaterConnection.construct(0, new Object[]{host, port});
            outgoingSockets.put(hashCode, out);
        }

        return out;
    }

    public static final void sendMessageToRemote(String host, int port, Message message) {
        OutgoingTheaterConnection out = getSocket(host, port);
        StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, out, 3 /*send*/, new Object[]{message}));

//        System.err.println("sending remote message to [" + host + " : " + port + "]: " + message);
    }

    public static final void createRemotely(String host, int port, Object remoteCreator) {
        OutgoingTheaterConnection out = getSocket(host, port);
        StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, out, 5 /*createRemotely*/, new Object[]{remoteCreator}));

//        System.err.println("sending createRemotely to [" + host + " : " + port + "]: " + remoteCreator);
    }

    public static final void migrateActor(String host, int port, MobileActor.State actor) {
        actor.host = host;
        actor.port = port;

        OutgoingTheaterConnection out = getSocket(host, port);
        StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, out, 4 /*migrate*/, new Object[]{actor}));

//        System.err.println("migrating actor to [" + host + " : " + port + "]: " + actor.hashCode());
    }
}
