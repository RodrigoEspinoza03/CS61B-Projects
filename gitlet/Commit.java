package gitlet;

import java.io.Serializable;
import java.util.TreeMap;

import static gitlet.Utils.sha1;

/** Creates a commit that stores various
 * pieces of information.
 * @author Rodrigo Espinoza
 */
public class Commit implements Serializable {

    /** Hash of the current commit.
     */
    private String _ownID;

    /** Hash of the parent commit.
     */
    private String _parentID;

    /** Time when the commit was made.
     */
    private String _timeStamp;

    /** Commit message.
     */
    private String _message;

    /** Contains blobs as
     *  filename and hash.
     *  Hash being the pathname to
     *  the blob.
     */
    private TreeMap<String, String> _blobs;

    public Commit(String msg, String pID,
                  TreeMap<String, String> blobs, String date) {
        this._timeStamp = date;
        this._parentID = pID;
        this._message = msg;
        this._ownID = sha1(msg, pID, this._timeStamp);
        this._blobs = blobs;

    }

    public String getOwnID() {
        return this._ownID;
    }

    public String getParentID() {
        return this._parentID;
    }

    public String getMsg() {
        return this._message;
    }

    public String getTimeStamp() {
        return this._timeStamp;
    }

    public TreeMap<String, String> getBlobs() {
        return this._blobs;
    }

    public void changeOwnID(String hash) {
        this._ownID = hash;
    }

    public String toString() {
        String msg = "=== \n";
        msg += "commit " + getOwnID() + "\n";
        msg += "Date: " + getTimeStamp() + "\n";
        msg += getMsg() + "\n";
        return msg;
    }

}


