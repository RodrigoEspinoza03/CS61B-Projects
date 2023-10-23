package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

/**Creates Staging Area used when
 * creating a new commit in Gitlet.
 * @author Rodrigo Espinoza
 */
public class StagingArea implements Serializable {

    /** Contains files set for removal.
     */
    private ArrayList<String> _removeFiles;

    /**Contains files to be added.
     */
    private TreeMap<String, String> _addFiles;

    public StagingArea() {
        this._addFiles = new TreeMap<>();
        this._removeFiles = new ArrayList<>();
    }

    public void add(String fileName, String hash) {
        getAddFiles().put(fileName, hash);
    }

    public void addRemove(String fileName) {
        getRemoveFiles().add(fileName);
    }

    public TreeMap<String, String> getAddFiles() {
        return this._addFiles;
    }

    public ArrayList<String> getRemoveFiles() {
        return  this._removeFiles;
    }

    public void clear() {
        this._addFiles = new TreeMap<>();
        this._removeFiles = new ArrayList<>();
    }

}
