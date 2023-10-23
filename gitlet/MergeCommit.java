package gitlet;

import java.util.TreeMap;

import static gitlet.Utils.sha1;

/** Creates a Merge Commit, same as
 * main commit class but with a second parent.
 * @author Rodrigo Espinoza
 */
class MergeCommit extends Commit {

    /** Contains hash of parent commit.
     */
    private String _parent2;

    MergeCommit(String msg, String parent1,
                        String parent2, TreeMap<String,
            String> blobs, String date) {
        super(msg, parent1, blobs, date);
        this._parent2 = parent2;
        changeOwnID(sha1(msg, parent1, parent2, date));
    }

    public String getParent2() {
        return _parent2;
    }

    public String toString() {
        String msg = "=== \n";
        msg += "commit " + getOwnID() + "\n";
        msg += "Merge: " + getParentID().substring(0, 7)
                + " " + getParent2().substring(0, 7) + "\n";
        msg += "Date: " + getTimeStamp() + "\n";
        msg += getMsg() + "\n";
        return msg;
    }

}
