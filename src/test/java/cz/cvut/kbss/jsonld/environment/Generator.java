package cz.cvut.kbss.jsonld.environment;

import java.util.Random;

public class Generator {

    private Generator() {
        throw new AssertionError();
    }

    private static final Random RAND = new Random();

    /**
     * Returns a (pseudo)random positive integer between 1 (inclusive) and {@code max} (exclusive).
     *
     * @param max Upper bound
     * @return random integer
     */
    public static int randomCount(int max) {
        assert max > 1;
        int res;
        do {
            res = RAND.nextInt(max);
        } while (res < 1);
        return res;
    }

    public static boolean randomBoolean() {
        return RAND.nextBoolean();
    }
}
