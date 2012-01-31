package salsa_lite.runtime;

import java.util.HashMap;

public class MobileActorRegistry {
    private static final HashMap<Integer,MobileActor>[] serializedReferences;
    private static final HashMap<Integer,Actor>[] serializedStates;

    static {
        serializedReferences = (HashMap<Integer,MobileActor>[])new HashMap[Hashing.numberRegistries];
        serializedStates = (HashMap<Integer,Actor>[])new HashMap[Hashing.numberRegistries];

        for (int i = 0; i < Hashing.numberRegistries; i++) {
            serializedReferences[i] = new HashMap<Integer,MobileActor>();
            serializedStates[i] = new HashMap<Integer,Actor>();
        }
    }

    public final static Object getReferenceLock(int hashCode) {
        return serializedReferences[hashCode % Hashing.numberRegistries];
    }

    public final static MobileActor getReferenceEntry(int hashCode) {
        return serializedReferences[hashCode % Hashing.numberRegistries].get(hashCode);
    }

    public final static MobileActor removeReferenceEntry(int hashCode) {
        System.err.println("removing mobile reference entry[" + hashCode + "]: " + serializedReferences[hashCode % Hashing.numberRegistries].get(hashCode));
        return serializedReferences[hashCode % Hashing.numberRegistries].remove(hashCode);
    }

    public final static void addReferenceEntry(int hashCode, MobileActor actor) {
        if (serializedReferences[hashCode % Hashing.numberRegistries].get(hashCode) != null) {
            System.err.println("mobile actor regsitry reference error, overwriting registry entry: " + hashCode);
        }
        serializedReferences[hashCode % Hashing.numberRegistries].put(hashCode, actor);
    }

    public final static Object getStateLock(int hashCode) {
        return serializedStates[hashCode % Hashing.numberRegistries];
    }

    public final static Actor getStateEntry(int hashCode) {
        return serializedStates[hashCode % Hashing.numberRegistries].get(hashCode);
    }

    public final static Actor removeStateEntry(int hashCode) {
        System.err.println("removing mobile state entry[" + hashCode + "]: " + serializedStates[hashCode % Hashing.numberRegistries].get(hashCode));
        return serializedStates[hashCode % Hashing.numberRegistries].remove(hashCode);
    }

    public final static void addStateEntry(int hashCode, Actor actor) {
        if (serializedStates[hashCode % Hashing.numberRegistries].get(hashCode) != null) {
            System.err.println("mobile actor regsitry state error, overwriting registry entry: " + hashCode);
        }
        serializedStates[hashCode % Hashing.numberRegistries].put(hashCode, actor);
        System.err.println("added mobile state entry[" + hashCode + "]: " + serializedStates[hashCode % Hashing.numberRegistries].get(hashCode));
    }

    public final static void updateStateEntry(int hashCode, Actor actor) {
        serializedStates[hashCode % Hashing.numberRegistries].put(hashCode, actor);
        System.err.println("updated mobile state entry[" + hashCode + "]: " + serializedStates[hashCode % Hashing.numberRegistries].get(hashCode));
    }


}
