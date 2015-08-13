package salsa_lite.runtime;

import java.util.HashMap;

import salsa_lite.common.HashCodeBuilder;

public class LocalActorRegistry {
    private final static HashMap<Integer,Actor>[] serializedActors;

    static {
        serializedActors = (HashMap<Integer,Actor>[])new HashMap[Hashing.numberRegistries];

        for (int i = 0; i < Hashing.numberRegistries; i++) {
            serializedActors[i] = new HashMap<Integer,Actor>();
        }

    }

    public final static Object getLock(int hashCode) {
        return serializedActors[Math.abs(hashCode % Hashing.numberRegistries)];
    }

    public final static Actor getEntry(int hashCode) {
//        System.err.println("getting entry[" + hashCode + "]: " + serializedActors[hashCode % numberRegistries].get(hashCode));
        return serializedActors[Math.abs(hashCode % Hashing.numberRegistries)].get(hashCode);
    }

    public final static Actor removeEntry(int hashCode) {
        System.err.println("removing entry[" + hashCode + "]: " + serializedActors[Math.abs(hashCode % Hashing.numberRegistries)].get(hashCode));
        return serializedActors[Math.abs(hashCode % Hashing.numberRegistries)].remove(hashCode);
    }

    public final static void addEntry(int hashCode, Actor actor) {
//        System.err.println("adding entry[" + hashCode + "]: " + actor);

        if (serializedActors[Math.abs(hashCode % Hashing.numberRegistries)].get(hashCode) != null) {
            System.err.println("actor registry error, overwriting registry entry: " + hashCode + " actor: " + actor);
        }
        serializedActors[Math.abs(hashCode % Hashing.numberRegistries)].put(hashCode, actor);
    }
}
