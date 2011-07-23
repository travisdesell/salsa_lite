package salsa_lite.common;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import java.util.Arrays;

public class DeepCopy {

    public static final short[]     deepCopy(short[] array)     { return Arrays.copyOf(array, array.length); }
    public static final int[]       deepCopy(int[] array)       { return Arrays.copyOf(array, array.length); }
    public static final long[]      deepCopy(long[] array)      { return Arrays.copyOf(array, array.length); }
    public static final float[]     deepCopy(float[] array)     { return Arrays.copyOf(array, array.length); }
    public static final double[]    deepCopy(double[] array)    { return Arrays.copyOf(array, array.length); }
    public static final boolean[]   deepCopy(boolean[] array)   { return Arrays.copyOf(array, array.length); }
    public static final char[]      deepCopy(char[] array)      { return Arrays.copyOf(array, array.length); }
    public static final byte[]      deepCopy(byte[] array)      { return Arrays.copyOf(array, array.length); }

    /**
     * Returns a copy of the object, or null if the object cannot
     * be serialized.
     */
    public static final Object deepCopy(Object object) {
        Object obj = null;
        try {
            // Write the object out to a byte array
            FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fbos);
            out.writeObject(object);
            out.flush();
            out.close();

            // Retrieve an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(fbos.getInputStream());
            obj = in.readObject();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return obj;
    }
}
