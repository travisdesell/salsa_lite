package salsa_lite.runtime;


public class TheaterReference {

    public final int port;
    public final String host;

    public TheaterReference(String host, int port) {
        this.port = port;
        this.host = host;
    }


    public String toString() {
        return host + ":" + port;
    }
}
