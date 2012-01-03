package salsa_lite.runtime;

import java.util.HashMap;

import salsa_lite.common.HashCodeBuilder;

public class Hashing {
    protected static final int numberRegistries;
    private static final Object[] idLocks;
    private static final int[] uniqueIdGenerators;

    static {
        if (System.getProperty("nregistries") != null) numberRegistries = Integer.parseInt(System.getProperty("nregistries"));
        else numberRegistries = 1;

        idLocks = new Object[numberRegistries];
        uniqueIdGenerators = new int[numberRegistries];
        for (int i = 0; i < numberRegistries; i++) {
            idLocks[i] = new Object();
            uniqueIdGenerators[i] = i;
        }

    }

    public final static int generateUniqueHashCode(int nonUniqueHashCode) {
        int value;
        synchronized (idLocks[nonUniqueHashCode % numberRegistries]) {
            value = uniqueIdGenerators[nonUniqueHashCode % numberRegistries];
            uniqueIdGenerators[nonUniqueHashCode % numberRegistries] += numberRegistries;
        }
        return value;
    }

    public final static int getHashCodeFor(int hashcode /*a hashcode from a remote reference to a local actor*/, String host, int port) {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(hashcode);
        hcb.append(host);
        hcb.append(port);

//        System.err.println("hashCode for [" + host + ":" + port + " - " + hashcode + "] resolved to: " + hcb.toHashCode());

        return hcb.toHashCode();
    }


    public final static int getHashCodeFor(String name, String host, int port) {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(name);
        hcb.append(host);
        hcb.append(port);

//        System.err.println("hashCode for [" + host + ":" + port + "/" + name + "] resolved to: " + hcb.toHashCode());

        return hcb.toHashCode();
    }

    public final static int getHashCodeFor(String name, String nameserverName, String nameserverHost, int nameserverPort) {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(name);
        hcb.append(nameserverName);
        hcb.append(nameserverHost);
        hcb.append(nameserverPort);

//        System.err.println("hashCode for [" + name + " at " + nameserverHost + ":" + nameserverPort + "/" + nameserverName + "] resolved to: " + hcb.toHashCode());
        return hcb.toHashCode();
    }
}
