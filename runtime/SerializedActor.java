package salsa_lite.runtime;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class SerializedActor implements Serializable {

    private int hashCode;

    public SerializedActor(int hashCode) {
        this.hashCode = hashCode;
    }

    public Object readResolve() throws ObjectStreamException {
        /**
         *  Return and remove the temporary entry in the registry used by serialization in a deep copy, so the actor doens't get copied.
         */
        return ActorRegistry.removeEntry(hashCode);
    }

}
