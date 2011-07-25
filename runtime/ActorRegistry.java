package salsa_lite.runtime;

import java.util.HashMap;

public class ActorRegistry {

    private static HashMap<Integer,Actor> serializedActors = new HashMap<Integer,Actor>();

    protected static synchronized final Actor removeEntry(int hashCode) {
        return serializedActors.remove(hashCode);
    }

    protected static synchronized final void addEntry(int hashCode, Actor actor) {
        serializedActors.put(hashCode, actor);
    }
}
