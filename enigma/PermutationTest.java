package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test(expected = EnigmaException.class)
    public void checkConstructor() {
        Alphabet basic = new Alphabet();
        perm = new Permutation("(ABC) (ABC)", basic);
    }

    @Test
    public void checkPermutations() {
        Alphabet basic = new Alphabet();
        perm = new Permutation("(CBD) (AFG) (LPH)", basic);
        assertEquals(5, perm.permute(0));
        assertEquals(6, perm.invert(0));
        assertEquals('F', perm.permute('A'));
        assertEquals('G', perm.invert('A'));
    }

    @Test
    public void checkHardPermutations() {
        Alphabet weird = new Alphabet("aAbBcCdD3024?!");
        perm = new Permutation("(aA) (bB) (cC) (dD) (30) (24) (?!)", weird);
        assertTrue(perm.derangement());
        for (int i = 0; i < weird.size(); i++) {
            if (i % 2 == 0) {
                assertEquals(i + 1, perm.permute(i));
                assertEquals(i, perm.invert(i + 1));
                assertEquals(weird.toChar(i + 1), perm.invert(weird.toChar(i)));
                assertEquals(weird.toChar(i), perm.invert(weird.toChar(i + 1)));
            } else {
                assertEquals(i - 1, perm.permute(i));
                assertEquals(i, perm.invert(i - 1));
                assertEquals(weird.toChar(i - 1), perm.invert(weird.toChar(i)));
                assertEquals(weird.toChar(i), perm.invert(weird.toChar(i - 1)));
            }

        }
    }


}
