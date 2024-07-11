package clarin.cmdi.componentregistry;

public final class IdSequence {
    static long current = System.currentTimeMillis();

    static public synchronized long get() {
        return current++;
    }
}
