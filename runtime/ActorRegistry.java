package salsa_lite.runtime;

import java.util.HashMap;

public class ActorRegistry {

    private static int numberRegistries;
    private static HashMap<Integer,Actor>[] serializedActors;

    static {
        if (System.getProperty("nregistries") != null) numberRegistries = Integer.parseInt(System.getProperty("nregistries"));
        else numberRegistries = 1;

        serializedActors = (HashMap<Integer,Actor>[])new HashMap[numberRegistries];
        for (int i = 0; i < numberRegistries; i++) {
            serializedActors[i] = new HashMap<Integer,Actor>();
        }
    }

    public static Object getLock(int hashCode) {
        return serializedActors[hashCode % numberRegistries];
    }

    public static Actor getEntry(int hashCode) {
//        System.err.println("getting entry[" + hashCode + "]: " + serializedActors[hashCode % numberRegistries].get(hashCode));
        return serializedActors[hashCode % numberRegistries].get(hashCode);
    }

    public static Actor removeEntry(int hashCode) {
        System.err.println("removing entry[" + hashCode + "]: " + serializedActors[hashCode % numberRegistries].get(hashCode));
        return serializedActors[hashCode % numberRegistries].remove(hashCode);
    }

    public static void addEntry(int hashCode, Actor actor) {
        System.err.println("adding entry[" + hashCode + "]");
//        : " + actor);

        if (serializedActors[hashCode % numberRegistries].get(hashCode) != null) {
            System.err.println("error, overwriting registry entry: " + hashCode);
        }
        serializedActors[hashCode % numberRegistries].put(hashCode, actor);
    }
}
