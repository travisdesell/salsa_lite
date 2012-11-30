public class Fork implements java.io.Serializable {
    //Forks don't do anything, philosophers just hold them.

    //Each fork has a number, so you know which one it is in
    //the array (although this is mostly just useful for
    //debugging).

    public int number;

    public Fork(int n) {
        this.number = n;
    }
}
