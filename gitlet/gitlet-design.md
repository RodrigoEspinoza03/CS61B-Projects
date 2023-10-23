# Gitlet Design Document
author: Rodrigo Espinoza

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

### Archive 

This class creates all the necessary files/directories to organize gitlet.

### Fields

1. File gitlet = new File (".gitlet"); : holds everything in the current repository * this is directory

2. File commits = new File (".gitlet//commits"); : holds the commits files. *this is directory 

3. File Stage = new File (".gitlet//staging") : holds the staging area serialized *this is a pathname

4. File Blobs = new File (".gitlet//blobs") : holds the blob files *this is a pathname

5. File CWD = new File (".CWD"); : holds the files that are currently in use in the current working
   directory *this is directory
   
6. StagingArea staging; : holds all the info in the staging area 

7. Commit _head : contains the current head commit;

8. Branch _headBranch : contains the current head branch obj;

8. File Branches = new File (.gitlet//branches) : holds the pathname to the branches directory 


### StagingArea

This class creates a staging area object that stores the files to be added or removed in a new commit.

### Fields

1. HashMap<String, String> addFiles: holds the files in blob form where <string is SHA-1 hash, string is file name>.
2. ArrayList<String> removeFiles: holds the file names to be removed.

### Commit

This class creates a commit object. It contains its own SHA-1 ID, timestamp, message, and files.

### Fields

1. String iD: represents the SHA-1 ID.
2. String message: message representing commit.
3. String date: represents date of commit.
4. HashMap<Blob>: stores all saved files in blob format.

### Gitlet

This class is the main class. Creates a directory where files are stored through commits which are stored
in a LinkedList and in files inside the .gitlet directory.

### Fields

1. File CWD: current working directory --> files stored must be in .gitlet\ directory.
2. LinkedList<Commit>: stores the commits in a linear way where we can traverse them.
3. ArrayList<Blob> add; stores the list of blob(files) ready to be added to the next commit.
4. ArrayList<Blob> remove; stores the list of blob(files) ready to be removed in the next commit.


### Branch 

This class creates a branch object which stores the most recent commit, its name, and staging area.


### Fields

1. String _name : name of the branch;

2. Commit _lastCommit : last commit done on that branch;

3. StagingArea _stage : stagingArea obj


## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.
  
### Archive

1. init(): Creates a new Gitlet version-control system in the current directory. First, check that this commit
   doesn't already exist, if it does throw gitlet exception "Gitlet version-control systemm already exists".
   If it doesn't create a new commit object, message "intial commit" and timestamp "00:00:00 UTC, Thursday, 1
   January 1970" and intilizes the LinkedList commits to start with this commit. Then serialize it into the
   commit file inside the .gitlet\ directory.

2. commit() [message]: Create a new commit that is added to the current LinkedList commit and inside the commits file
   in .gitlet directory. Check the add and remove arraylist for files to be replaced for newer versions or
   removed entirely. Files should already be blobs inside the arrayList making it easier to be stored or not.
   Date and message will be different, and can be found as an argument and method that gives date.


3. checkout -- [file name]: If file doesn't exitst throw gitlet exception "File does not exist in that commit."
   If file exists in the current head commit, then overwrite the version of that file in the current working
   directory if it doesn't exist just add it. Don't change if it is the same file already.


4. checkout [commit id] -- [file name]: Does the same as checkout -- [file name] but taking te file from
   the commit specfied by the commit id. If commit id doesn't exist, throw gitlet exception "No commit with
   that id exists". Use checkout -- [file name] to do the work of overwriting. In this method only search for
   the file in the specified commit id, then pass it to the prev checkout method.


5. log(): Create a new LinkedList that points to the head pointer (the latest commit) of the LinkedList commit  then
   call the .toString of the commits and change the pointer to .prev  of the commit. Do this until we reach
   the beginning of the LinkedList i.e. the master commit. Use a while (.prev != null) loop.





### Staging Area

1. StagingArea(): initializes a new staging object that intializes its own HashMap and ArrayList.


2. add([file name]): Add the copy of the file name to the arraylist add, this is the staging area. If it already
   exists in the arraylist overwrite it with this new version of the file. If file is the same as in the current
   commit then do not stage. If it doesn't exist, throw exception "File does not exist."
   Files will be entered as blobs, so turn the file into a blob. Have a staging area file that is sorted
   in .gitlet directory. Check if it was in rm arrayList, if it was then remove it from there.
  
 
3. remove([file name]):


4. getBlobs():


5. getRemoveFiles():


### Commit 

1. Commit(). The class constructor. Stores message and time in string variables. Then creates a SHA-1 hashcode
   value storing it in a string variable ID. SHA-1 ID must include blob reference of files, parent reference,
   log message, commit time. Creates a hashmap by first turning all files being added to the commit
   to blobs using the blob constructor and then using their SHA-1 ids to map it into the hashmap.


2. toString(). The string representation of the information stored by a commit object. I will follow the format
   specified in the spec of Gitlet project.

### Gitlet

1. Main(). Tells which method to use depending on the first element of args. Ensure that the number of arguments
    after the first is appropriate for the method it is calling, if not called throw gitlet exception with appropriate
    error message.
   

## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.
  
java gitlet.Main init // java gitlet.Main commit 
    
We would want to create a file named commits or subdirectory containing commit files. In this way, storing
commits for later use if needed inside the .gitlet directory. 

1.To this create a method that takes a commit then serializes it and places it inside the appropriate 
  file inside the .gitlet directory.
2. Have a method that un-serializes a file; possibly uses the ID to find the right file to unserialize

java gitlet.Main add/remove

We would want to save the blobs(files) inside a file inside of the .gitlet directory.

1. To this use the serializable interface to serialize the arraylist add and remove
   inside of a file "staging area".
   
To retrieve any of these files and use them in our working directory (where our program resides). 
Deserialize the objects using readObjects method from Utils class and read the data in the files
as objects.

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

