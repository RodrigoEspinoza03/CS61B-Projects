package gitlet;



import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Date;
import java.util.ArrayList;
import java.util.Objects;

import static gitlet.Utils.*;


/** The repo of Gitlet where we store all the data,
 * and perform the commands.
 * @author Rodrigo Espinoza
 */
public class Archive implements Serializable {

    /** Pathname to the current working directory.
     */
    private final File _CWD;

    /** Pathname to the Gitlet directory.
     */
    static final File GITLET = new File(".gitlet");

    /** Pathname to the Commits directory.
     */
    static final File COMMITS = new File(GITLET
            + File.separator + "commits");

    /** Pathname to the Branches directory.
     */
    static final File BRANCHES = new File(GITLET
            + File.separator + "branches");

    /** Pathname to the Head Branch text file.
     */
    static final File HEAD_BRANCH = new File(BRANCHES
            + File.separator + "head.txt");

    /** Pathname to the Staging Area text file.
     */
    static final File STAGING_AREA = new File(GITLET
            + File.separator + "staging.txt");

    /** Pathname to the Blobs directory.
     */
    static final File BLOBS = new File(GITLET
            + File.separator + "blobs");

    /**Pathname to Global Log text file.
     */
    static final File GLOBAL_LOG = new File(GITLET
            + File.separator + "global.txt");

    /** Contains the current Head Branch.
     */
    private Branch _headBranch;

    /** Contains the current Head Commit.
     */
    private Commit _head;

    /** Contains the Staging Area.
     */
    private StagingArea _staging;

    public Archive() {
        this._CWD = new File(System.getProperty("user.dir"));
        if (HEAD_BRANCH.exists()) {
            this._headBranch = readObject(HEAD_BRANCH, Branch.class);
            File head = new File(COMMITS + File.separator
                    + this._headBranch.getLastCommit().getOwnID() + ".txt");
            this._head = readObject(head, Commit.class);
        }
        if (STAGING_AREA.exists()) {
            this._staging = readObject(STAGING_AREA, StagingArea.class);
        }

    }

    public void init(String...args) throws IOException {
        if (GITLET.exists()) {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
            return;
        }
        if (helper(1, args)) {
            return;
        }

        GITLET.mkdir();

        Commit initial = new Commit("initial commit", "", new TreeMap<>(),
                "Wed Dec 31 16:00:00 1969 -0800");
        Branch branch = new Branch("master", initial);
        this._head = initial;
        this._headBranch = branch;
        this._staging = new StagingArea();

        String log = initial.toString();
        GLOBAL_LOG.createNewFile();
        writeContents(GLOBAL_LOG, log + "\n");
        BLOBS.mkdir();
        Utils.join(GITLET, "blobs");
        COMMITS.mkdir();
        Utils.join(GITLET, ".commits");
        BRANCHES.mkdir();
        Utils.join(GITLET, ".branches");

        STAGING_AREA.createNewFile();
        writeObject(STAGING_AREA, this._staging);
        Utils.join(GITLET, ".staging");

        File masterBranch = new File(BRANCHES
                + File.separator + branch.getName() + ".txt");
        masterBranch.createNewFile();
        writeObject(masterBranch, branch);

        HEAD_BRANCH.createNewFile();
        writeObject(HEAD_BRANCH, branch);

        Utils.join(BRANCHES, File.separator + "head.txt");
        Utils.join(BRANCHES, File.separator + branch.getName() + ".txt");

        File initialCommit = new File(COMMITS
                + File.separator + initial.getOwnID() + ".txt");
        initialCommit.createNewFile();
        writeObject(initialCommit, initial);
        Utils.join(COMMITS, initial.getOwnID());

    }

    public void add(String...args) throws IOException {
        if (!gitletExists()) {
            return;
        }
        if (helper(2, args)) {
            return;
        }
        String fileName = args[1];
        File curr = new File(_CWD + File.separator + fileName);
        if (!curr.exists()) {
            System.out.println("File does not exist.");
        } else {
            byte[] contents = readContents(curr);
            String hash = sha1(contents);
            if (getHeadCommit().getBlobs().get(fileName) != null
                    && getHeadCommit().getBlobs().get(fileName).equals(hash)) {
                if (getStage().getRemoveFiles().contains(fileName)) {
                    getStage().getRemoveFiles().remove(fileName);
                    writeObject(STAGING_AREA, getStage());
                }
            } else {
                getStage().getRemoveFiles().remove(fileName);
                File blob = new File(BLOBS + File.separator + hash + ".txt");
                blob.createNewFile();
                writeContents(blob, contents);
                getStage().add(fileName, hash);
                writeObject(STAGING_AREA, getStage());
            }
        }
    }

    public void commit(String...args) throws IOException {
        if (!gitletExists()) {
            return;
        }
        if (helper(2, args)) {
            return;
        }
        if (getStage().getAddFiles().isEmpty()
                && getStage().getRemoveFiles().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        } else if (args[1].equals("")) {
            System.out.println("Please enter a commit message");
            return;
        }

        String msg = args[1];
        @SuppressWarnings("unchecked")
        TreeMap<String, String> newBlobs =
                (TreeMap<String, String>) getHeadCommit().getBlobs().clone();

        for (String x : _staging.getRemoveFiles()) {
            newBlobs.remove(x);
        }

        for (String x: getStage().getAddFiles().keySet()) {
            newBlobs.put(x, getStage().getAddFiles().get(x));
        }

        getStage().clear();
        writeObject(STAGING_AREA, getStage());

        SimpleDateFormat formatter =
                new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        Date time = new Date(System.currentTimeMillis());
        String date = formatter.format(time);

        Commit child =
                new Commit(msg, getBranch().getLastCommit().getOwnID(),
                        newBlobs, date);
        this._head = child;
        getBranch().changeLastCommit(child);

        File childCommit = new File(COMMITS
                + File.separator + child.getOwnID() + ".txt");
        childCommit.createNewFile();
        writeObject(childCommit, child);
        writeContents(GLOBAL_LOG,
                readContentsAsString(GLOBAL_LOG) + child + "\n");
        Utils.join(COMMITS, child.getOwnID());

        writeObject(HEAD_BRANCH, getBranch());
        writeObject(new File(BRANCHES + File.separator
                        + getBranch().getName() + ".txt"), getBranch());

    }

    public void checkout(String...args) throws IOException {
        if (!gitletExists()) {
            return;
        }
        if (args.length != 2 && args.length != 3 && args.length != 4) {
            System.out.println("Incorrect Operands.");
        } else if ((args.length == 3 && !args[1].equals("--"))
                || (args.length == 4 && !args[2].equals("--"))) {
            System.out.println("Incorrect Operands.");
            return;
        }

        if (args.length == 3) {
            checkout(args[2]);
        } else if (args.length == 4) {
            checkout(args[3], args[1]);
        } else if (args.length == 2) {
            String branchName = args[1] + ".txt";
            List<String> files = plainFilenamesIn(BRANCHES);
            File branch = new File(BRANCHES
                    + File.separator + branchName);
            if (!files.contains(branchName)) {
                System.out.println("No such branch exists.");
                return;
            }
            Branch newBranch = readObject(branch, Branch.class);
            if (newBranch.getName().equals(getBranch().getName())) {
                System.out.println("No need to checkout the current branch.");
                return;
            }
            checkout(newBranch);
        }
    }

    public void checkout(String filename) throws IOException {
        if (!gitletExists()) {
            return;
        }
        if ((getHeadCommit().getBlobs() != null)
                && !getHeadCommit().getBlobs().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File headVersion = new File(BLOBS + File.separator
                + getHeadCommit().getBlobs().get(filename) + ".txt");
        File curr = new File(_CWD + File.separator + filename);
        if (!curr.exists()) {
            curr.createNewFile();
        }
        writeContents(curr, readContents(headVersion));

    }

    public void checkout(String filename, String commitID) throws IOException {
        if (!gitletExists()) {
            return;
        }

        commitID = findCommitID(commitID);

        File commit = new File(COMMITS
                + File.separator + commitID);

        if (!commit.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit given = readObject(commit, Commit.class);
        Commit temp = getHeadCommit();
        this._head = given;
        checkout(filename);
        this._head = temp;
    }

    private String findCommitID(String commitID) {
        List<String> commits = plainFilenamesIn(COMMITS);
        commitID = commitID.substring(0, 5);
        assert commits != null;
        for (String file : commits) {
            String curr = file.substring(0, 5);
            if (curr.equals(commitID)) {
                return file;
            }
        }
        return "bad";
    }

    public void checkout(Branch givenBranch) throws IOException {
        if (!gitletExists()) {
            return;
        }
        Branch curr = getBranch();
        List<String> cwdFiles = plainFilenamesIn(_CWD);
        if (!checkIfUntracked(getHeadCommit(), givenBranch.getLastCommit())) {
            return;
        }
        Commit given = givenBranch.getLastCommit();
        assert cwdFiles != null;
        for (String filename : cwdFiles) {
            if (curr.getLastCommit().getBlobs().containsKey(filename)
                    && !given.getBlobs().containsKey(filename)) {
                File currFile = new File(_CWD + File.separator + filename);
                restrictedDelete(currFile);
            }
        }

        this._headBranch = givenBranch;
        writeObject(HEAD_BRANCH, getBranch());
        writeObject(new File(BRANCHES + File.separator
                + getBranch().getName() + ".txt"), getBranch());
        this._head = getBranch().getLastCommit();

        for (String filename: getHeadCommit().getBlobs().keySet()) {
            checkout(filename);
        }

        if (!curr.getName().equals(getBranch().getName())) {
            getStage().clear();
            writeObject(STAGING_AREA, getStage());
        }
    }

    public void remove(String...args) {
        if (!gitletExists()) {
            return;
        }
        if (helper(2, args)) {
            return;
        }
        String filename = args[1];
        File curr = new File(_CWD + File.separator + filename);

        if (getHeadCommit().getBlobs().containsKey(filename)) {
            restrictedDelete(curr);
            getStage().addRemove(filename);
            getStage().getAddFiles().remove(filename);
            writeObject(STAGING_AREA, getStage());
        } else if (getStage().getAddFiles().containsKey(filename)) {
            getStage().getAddFiles().remove(filename);
            writeObject(STAGING_AREA, getStage());
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    public void log(String...args) {
        if (!gitletExists()) {
            return;
        }
        if (helper(1, args)) {
            return;
        }
        Commit curr = getHeadCommit();
        while (curr != null) {
            System.out.println(curr);
            File nextCommit = new File(COMMITS + File.separator
                    + curr.getParentID() + ".txt");
            if (nextCommit.exists()) {
                curr = readObject(nextCommit, Commit.class);
            } else {
                curr = null;
            }
        }
    }

    public void globalLog(String...args) {
        if (!gitletExists()) {
            return;
        }
        if (helper(1, args)) {
            return;
        }
        System.out.println(readContentsAsString(GLOBAL_LOG));
    }

    public void find(String...args) {
        if (!gitletExists()) {
            return;
        }
        if (helper(2, args)) {
            return;
        }
        List<String> commitNames = plainFilenamesIn(COMMITS);
        int count = 0;
        String message = args[1];
        assert commitNames != null;
        for (String commit: commitNames) {
            Commit curr = readObject(new File(COMMITS + File.separator
                    + commit), Commit.class);
            if (curr.getMsg().equals(message)) {
                count++;
                System.out.println(curr.getOwnID());
            }
        }

        if (count == 0) {
            System.out.println("Found no commit with that message.");
        }

    }

    public void status(String...args) {
        if (!gitletExists()) {
            return;
        }
        if (helper(1, args)) {
            return;
        }
        List<String> branches = plainFilenamesIn(BRANCHES);
        Collections.sort(branches);

        List<String> addFiles = new ArrayList<>();
        addFiles.addAll(getStage().getAddFiles().keySet());
        Collections.sort(addFiles);

        List<String> removeFiles = new ArrayList<>();
        removeFiles.addAll(getStage().getRemoveFiles());
        Collections.sort(removeFiles);
        System.out.println("=== Branches ===");
        System.out.println("*" + getBranch().getName());
        for (String branch: branches) {
            Branch curr = readObject(new File(BRANCHES
                    + File.separator + branch), Branch.class);
            if (!curr.getName().equals(getBranch().getName())) {
                System.out.println(curr.getName());
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String filename: addFiles) {
            System.out.println(filename);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String filename: removeFiles) {
            System.out.println(filename);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public void branch(String...args) throws IOException {
        if (!gitletExists()) {
            return;
        }
        if (helper(2, args)) {
            return;
        }
        String name = args[1];
        File newBranchFile = new File(BRANCHES + File.separator
                + name + ".txt");
        if (newBranchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        newBranchFile.createNewFile();
        Branch newBranch = new Branch(name, getHeadCommit());
        writeObject(newBranchFile, newBranch);
    }

    public void removeBranch(String...args) {
        if (!gitletExists()) {
            return;
        }
        if (helper(2, args)) {
            return;
        }
        String filename = args[1] + ".txt";
        File branch = new File(BRANCHES
                + File.separator + filename);
        if (!branch.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        Branch thisBranch = readObject(branch, Branch.class);
        if (thisBranch.getName().equals(getBranch().getName())) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branch.delete();
    }

    public void reset(String...args) throws IOException {
        if (!gitletExists()) {
            return;
        }
        if (helper(2, args)) {
            return;
        }
        String commitID = args[1];
        File thisCommit = new File(COMMITS + File.separator
                + commitID + ".txt");
        if (!thisCommit.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit replacement = readObject(thisCommit, Commit.class);
        if (!checkIfUntracked(getHeadCommit(), replacement)) {
            return;
        }
        for (String file: Objects.requireNonNull(plainFilenamesIn(_CWD))) {
            if (getHeadCommit().getBlobs().containsKey(file)
                    && !replacement.getBlobs().containsKey(file)) {
                Utils.restrictedDelete(file);
            }
        }
        getBranch().changeLastCommit(replacement);
        writeObject(HEAD_BRANCH, getBranch());
        writeObject(new File(BRANCHES + File.separator
                + getBranch().getName() + ".txt"), getBranch());
        this._head = replacement;
        for (String filename : replacement.getBlobs().keySet()) {
            checkout(filename);
        }
        getStage().clear();
        writeObject(STAGING_AREA, getStage());
    }

    public void merge(String...args) throws IOException {
        if ((!gitletExists()) || helper(2, args)
                || mergeErrors(args[1])) {
            return;
        }
        File givenBranch = new File(BRANCHES
                + File.separator + args[1] + ".txt");
        Commit head = getHeadCommit();
        Commit given = readObject(givenBranch, Branch.class).getLastCommit();
        Commit split = splitPoint(head, given);
        Boolean conf = false;
        for (String file: allFiles(head, split, given)) {
            String hHash = head.getBlobs().get(file);
            String gHash = given.getBlobs().get(file);
            String sHash = split.getBlobs().get(file);
            boolean conflict = false;
            if (head.getBlobs().containsKey(file)
                    && given.getBlobs().containsKey(file)) {
                conflict = !hHash.equals(sHash) && !gHash.equals(sHash)
                        && !hHash.equals(gHash);
            }

            if (split.getBlobs().containsKey(file)) {
                if (conflict) {
                    mergeHelper1(hHash, file, gHash);
                    conf = true;
                } else if (head.getBlobs().containsKey(file)
                        && hHash.equals(sHash)
                        && !given.getBlobs().containsKey(file)) {
                    remove("remove", file);
                } else if (given.getBlobs().containsKey(file)
                        && !gHash.equals(sHash) && sHash.equals(hHash)) {
                    checkout(file, given.getOwnID());
                } else if (given.getBlobs().containsKey(file)
                        && gHash.equals(sHash)
                        && !head.getBlobs().containsKey(file)) {
                    getStage().addRemove(file);
                }
            } else {
                if (conflict) {
                    mergeHelper1(hHash, file, gHash);
                    conf = true;
                } else if (given.getBlobs().containsKey(file)
                        && !head.getBlobs().containsKey(file)) {
                    checkout(file, given.getOwnID());
                    add("add", file);
                }
            }
        }
        if (conf) {
            System.out.println("Encountered a merge conflict.");
            return;
        } else {
            String message = "Merged " + args[1]
                    + " into " + getBranch().getName() + ".";
            mergeCommit(message, given);
        }
    }

    public ArrayList<String> allFiles(Commit c, Commit s, Commit g) {
        ArrayList<String> allFiles = new ArrayList<>(c.getBlobs().keySet());

        for (String file : s.getBlobs().keySet()) {
            if (!allFiles.contains(file)) {
                allFiles.add(file);
            }
        }

        for (String file : g.getBlobs().keySet()) {
            if (!allFiles.contains(file)) {
                allFiles.add(file);
            }
        }

        return allFiles;
    }

    public ArrayList<String> p2Commits(String p2) {
        File parent = new File(COMMITS + File.separator
                + p2 + ".txt");
        Commit parent2 = readObject(parent, Commit.class);
        ArrayList<String> ancestors = new ArrayList<>();

        while (parent2 != null) {
            ancestors.add(parent2.getOwnID());
            parent = new File(COMMITS + File.separator
                    + parent2.getParentID() + ".txt");
            if (parent.exists()) {
                parent2 = readObject(parent, Commit.class);
            } else {
                parent2 = null;
            }
        }

        return ancestors;
    }

    public void mergeHelper1(String hBlob, String file, String gBlob) {
        File merge = new File(_CWD + File.separator + file);
        File head = new File(BLOBS + File.separator + hBlob + ".txt");
        File given = new File(BLOBS + File.separator + gBlob + ".txt");
        String contents = "<<<<<<< HEAD\n";
        if (head.exists()) {
            contents += readContentsAsString(head);
        }
        contents += "=======\n";
        if (given.exists()) {
            contents += readContentsAsString(given);
        }
        contents += ">>>>>>>";
        writeContents(merge, contents);
        System.out.println("Encountered a merge conflict.");
    }

    public boolean mergeErrors(String branch) throws IOException {
        File givenBranch = new File(BRANCHES
                + File.separator + branch + ".txt");
        if (!givenBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            return true;
        }
        Commit head = getHeadCommit();
        Commit given = readObject(givenBranch, Branch.class).getLastCommit();
        Commit split = splitPoint(head, given);
        ArrayList<String> ancestryHead = ancestorList(head);
        if (!checkIfUntracked(head, given)) {
            return true;
        }

        if (!getStage().getRemoveFiles().isEmpty()
                || !getStage().getAddFiles().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return true;
        }
        if (readObject(givenBranch, Branch.class).
                getName().equals(getBranch().getName())) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }

        if (ancestryHead.contains(given.getOwnID())) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            return true;
        } else if (split.equals(head)) {
            checkout(readObject(givenBranch, Branch.class));
            System.out.println("Current branch fast-forwarded.");
            return true;
        }

        return false;
    }

    public Commit splitPoint(Commit head, Commit given) {
        Commit split = null;
        ArrayList<String> commitList = ancestorList(head);

        for (String commit: ancestorList(given)) {
            if (commitList.contains(commit)) {
                File s = new File(COMMITS + File.separator
                        + commit + ".txt");
                split = readObject(s, Commit.class);
                break;
            }
        }

        return split;
    }

    public ArrayList<String> ancestorList(Commit current) {
        ArrayList<String> commitList = new ArrayList<>();

        while (current != null && readObject(new File(COMMITS
                + File.separator
                + current.getOwnID() + ".txt"), Commit.class) != null) {

            commitList.add(current.getOwnID());
            if (current instanceof MergeCommit) {
                File p2 = new File (COMMITS + File.separator
                        + ((MergeCommit) current).getParent2() + ".txt");
                Commit parent = readObject(p2, Commit.class);
                commitList.addAll(ancestorList(parent));
            }
            if (!current.getParentID().equals("")) {
                current = readObject(new File(COMMITS
                        + File.separator
                        + current.getParentID() + ".txt"), Commit.class);
            } else {
                current = null;
            }
        }

        return commitList;
    }


    public boolean checkIfUntracked(Commit head, Commit given) {
        List<String> cwdFiles = plainFilenamesIn(_CWD);
        assert cwdFiles != null;
        for (String filename : cwdFiles) {
            if (!head.getBlobs().containsKey(filename)
                    && given.getBlobs().containsKey(filename)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return false;
            }
        }
        return true;
    }

    public void mergeCommit(String msg, Commit parent2) throws IOException {
        @SuppressWarnings("unchecked")
        TreeMap<String, String> newBlobs =
                (TreeMap<String, String>) getHeadCommit().getBlobs().clone();

        for (String filename : parent2.getBlobs().keySet()) {
            if (!newBlobs.containsKey(filename)) {
                newBlobs.put(filename, parent2.getBlobs().get(filename));
            }
        }

        for (String x : _staging.getRemoveFiles()) {
            newBlobs.remove(x);
        }

        for (String x: getStage().getAddFiles().keySet()) {
            newBlobs.put(x, getStage().getAddFiles().get(x));
        }

        getStage().clear();
        writeObject(STAGING_AREA, getStage());

        SimpleDateFormat formatter =
                new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        Date time = new Date(System.currentTimeMillis());
        String date = formatter.format(time);

        MergeCommit child = new MergeCommit(msg, getHeadCommit().getOwnID(),
                parent2.getOwnID(), newBlobs, date);
        this._head = child;
        getBranch().changeLastCommit(child);

        File childCommit = new File(COMMITS + File.separator
                + child.getOwnID() + ".txt");
        childCommit.createNewFile();
        writeObject(childCommit, child);
        Utils.join(COMMITS, child.getOwnID());

        writeObject(HEAD_BRANCH, getBranch());
        writeObject(
                new File(BRANCHES + File.separator
                        + getBranch().getName() + ".txt"), getBranch());

    }

    static boolean helper(int size, String...args) {
        if (args.length == size) {
            return false;
        } else {
            System.out.println("Incorrect Operands.");
            return true;
        }
    }

    static boolean gitletExists() {
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return false;
        }
        return true;
    }

    public Commit getHeadCommit() {
        return this._head;
    }

    public Branch getBranch() {
        return this._headBranch;
    }

    public StagingArea getStage() {
        return this._staging;
    }


}
