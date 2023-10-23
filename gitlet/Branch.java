package gitlet;

import java.io.Serializable;

/** Creates a Branch object that tracts the last commit
 * in the branch.
 * @author Rodrigo Espinoza
 */
public class Branch implements Serializable {

    /** Contains the name of the branch.
     */
    private String _name;

    /**Contains the last Commit of the branch.
     */
    private Commit _lastCommit;

    public Branch(String name, Commit lastCommit) {
        this._name = name;
        this._lastCommit = lastCommit;
    }

    public void changeLastCommit(Commit last) {
        this._lastCommit = last;
    }

    public String getName() {
        return this._name;
    }

    public Commit getLastCommit() {
        return this._lastCommit;
    }
}
