package salsa_lite.runtime;

import java.util.HashMap;

public class ActorRegistry {

    private static final int numberRegistries;
    private static final HashMap<Integer,Actor>[] serializedActors;

    static {
        if (System.getProperty("nregistries") != null) numberRegistries = Integer.parseInt(System.getProperty("nregistries"));
        else numberRegistries = 1;

        serializedActors = (HashMap<Integer,Actor>[])new HashMap[numberRegistries];
        for (int i = 0; i < numberRegistries; i++) {
            serializedActors[i] = new HashMap<Integer,Actor>();
        }
    }

    public static final Object getLock(int hashCode) {
        return serializedActors[hashCode % numberRegistries];
    }

    public static final Actor getEntry(int hashCode) {
        System.err.println("getting entry[" + hashCode + "]: " + serializedActors[hashCode % numberRegistries].get(hashCode));
        return serializedActors[hashCode % numberRegistries].get(hashCode);
    }

    public static final Actor removeEntry(int hashCode) {
        System.err.println("removing entry[" + hashCode + "]: " + serializedActors[hashCode % numberRegistries].get(hashCode));
        return serializedActors[hashCode % numberRegistries].remove(hashCode);
    }

    public static final void addEntry(int hashCode, Actor actor) {
        System.err.println("adding entry[" + hashCode + "]: " + actor);
        serializedActors[hashCode % numberRegistries].put(hashCode, actor);
    }
}
