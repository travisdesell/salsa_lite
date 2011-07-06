package salsa_lite.wwc;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;

import java.util.Hashtable;

public class TransportService extends Thread {

//	private Hashtable<String, InetAddress> cached_actor_locations;
//	private Hashtable<String, UAN> cached_actor_uans;

	public static void migrateActor(WWCActorState actor_state, Object[] arguments) {
		String host = (String)arguments[0];
		int port = (Integer)arguments[1];
	}

	public static void sendMessage(Message message) {
	}


	public static void receiveActor(WWCActorState actor_state) {
	}

	public static void receiveMessage(Message message) {
	}

//	public static String getUAN(WWCActorReference actor_reference) {
//		return cached_actor_uans.get(actor_reference.getUniqueId());
//	}

//	public static String getUAL(WWCActorReference actor_reference) {
//		return "";
//	}


	private static int port;
	public static int getPort() { return port; }

	private static String host;
	public static String getHost() { return host; }

	static {
//		cached_actor_locations = new Hashtable<String, InetAddress>();
//		cached_actor_uans = new Hashtable<String, UAN>();

		String portProperty = System.getProperty("port");
		if (portProperty == null) port = 4040;
		else port = Integer.parseInt(portProperty);

		try {
			host = InetAddress.getLocalHost().getHostAddress();
//			host = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (Exception e) {
			System.err.println("Error determining localhost: " + e);
			e.printStackTrace();
			System.exit(0);
		}

		ServerSocket server = null;
		try {
			server = new ServerSocket( port );
		} catch (IOException e) {
			System.err.println("Theater Service error:");
			System.err.println("\tCould not start theater.");
			System.err.println("\tException: " + e);
			System.exit(0);
		}
		// Check to see what the port actually is.
		port = server.getLocalPort();

		System.out.println("WWCReceptionService listening on: " + host + ":" + port);

		new TransportService(server).start();
	}

	private ServerSocket server;
	private TransportService(ServerSocket server) {
		this.server = server;
	}

	public void run() {
		while (true) {
			try {
				Socket incoming = server.accept();
			} catch (IOException e) {
				System.err.println("Reception Service error: ");
				System.err.println("\tFailed to accept connection.");
				System.err.println("\teException: " + e);
			}
		}
	}
}
