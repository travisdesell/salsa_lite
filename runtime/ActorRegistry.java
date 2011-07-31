package salsa_lite.runtime;

import java.util.HashMap;

public class ActorRegistry {

    private static final int numberRegistries;
    private static final Object[] idLocks;
    private static int[] uniqueIdGenerators;
    private static HashMap<Integer,Actor>[] serializedActors;

    static {
        if (System.getProperty("nregistries") != null) numberRegistries = Integer.parseInt(System.getProperty("nregistries"));
        else numberRegistries = 1;

        serializedActors = (HashMap<Integer,Actor>[])new HashMap[numberRegistries];
        idLocks = new Object[numberRegistries];
        uniqueIdGenerators = new int[numberRegistries];
        for (int i = 0; i < numberRegistries; i++) {
            serializedActors[i] = new HashMap<Integer,Actor>();
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

    public final static Object getLock(int hashCode) {
        return serializedActors[hashCode % numberRegistries];
    }

    public final static Actor getEntry(int hashCode) {
//        System.err.println("getting entry[" + hashCode + "]: " + serializedActors[hashCode % numberRegistries].get(hashCode));
        return serializedActors[hashCode % numberRegistries].get(hashCode);
    }

    public final static Actor removeEntry(int hashCode) {
        System.err.println("removing entry[" + hashCode + "]: " + serializedActors[hashCode % numberRegistries].get(hashCode));
        return serializedActors[hashCode % numberRegistries].remove(hashCode);
    }

    public final static void addEntry(int hashCode, Actor actor) {
//        System.err.println("adding entry[" + hashCode + "]: " + actor);

        if (serializedActors[hashCode % numberRegistries].get(hashCode) != null) {
            System.err.println("error, overwriting registry entry: " + hashCode);
        }
        serializedActors[hashCode % numberRegistries].put(hashCode, actor);
    }
}
