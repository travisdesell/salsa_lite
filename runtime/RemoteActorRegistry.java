package salsa_lite.runtime;

import java.util.HashMap;

public class RemoteActorRegistry {
    private static final HashMap<Integer,RemoteActor>[] serializedActors;

    static {
        serializedActors = (HashMap<Integer,RemoteActor>[])new HashMap[Hashing.numberRegistries];

        for (int i = 0; i < Hashing.numberRegistries; i++) {
            serializedActors[i] = new HashMap<Integer,RemoteActor>();
        }
    }

    public final static Object getLock(int hashCode) {
        return serializedActors[Math.abs(hashCode % Hashing.numberRegistries)];
    }

    public final static RemoteActor getEntry(int hashCode) {
        return serializedActors[Math.abs(hashCode % Hashing.numberRegistries)].get(hashCode);
    }

    public final static RemoteActor removeEntry(int hashCode) {
        System.err.println("removing entry[" + hashCode + "]: " + serializedActors[hashCode %Hashing.numberRegistries].get(hashCode));
        return serializedActors[Math.abs(hashCode % Hashing.numberRegistries)].remove(hashCode);
    }

    public final static void addEntry(int hashCode, RemoteActor actor) {
        if (serializedActors[Math.abs(hashCode % Hashing.numberRegistries)].get(hashCode) != null) {
            System.err.println("remote actor regsitry error, overwriting registry entry: " + hashCode);
        }
        serializedActors[Math.abs(hashCode % Hashing.numberRegistries)].put(hashCode, actor);
    }
}
