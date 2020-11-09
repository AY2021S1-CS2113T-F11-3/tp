package storage;

import common.KajiLog;
import exception.StorageDataException;
import exception.ExclusionFileException;
import manager.chapter.CardList;
import manager.chapter.Chapter;
import manager.history.History;
import manager.module.Module;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import static storage.StorageLoad.checkExists;

/**
 *  Manages writing of Kaji data to local storage.
 */
public class StorageWrite {
    private static Logger logger = KajiLog.getLogger(Storage.class.getName());

    private static final String MESSAGE_CREATED = "Successfully created new %1$s %2$s\n";
    private static final String MESSAGE_EXISTS = "%1$s %2$s already exists\n";

    public static final String FILE = "file";
    public static final String DIR = "directory";

    //@@author gua-guargia
    protected static void createDir(File f) throws StorageDataException {
        boolean dirExists = f.exists();
        boolean dirCreated = false;
        if (!dirExists) {
            dirCreated = f.mkdir();
        } else {
            logger.info(String.format(MESSAGE_EXISTS, DIR, f));
            throw new StorageDataException("The folder is already exist. Please check your directory.");
        }
        if (dirCreated) {
            logger.info(String.format(MESSAGE_CREATED, DIR, f));
        } else {
            throw new StorageDataException("Error creating new folder. Please check your directory.");
        }
    }

    //@@author gua-guargia
    protected static void createFile(File f) throws IOException, StorageDataException {
        boolean fileExists = f.exists();
        boolean fileCreated = false;
        if (!fileExists) {
            fileCreated = f.createNewFile();
        } else {
            logger.info(String.format(MESSAGE_EXISTS, FILE, f));
            throw new StorageDataException("The file is already exist. Please check your directory.");
        }
        if (fileCreated) {
            logger.info(String.format(MESSAGE_CREATED, FILE, f));
        } else {
            throw new StorageDataException("Error creating new file. Please check your directory.");
        }
    }

    //@@author Darticune
    protected static void updateExclusionFile(ArrayList<String> excludedChapters, String filePath)
            throws ExclusionFileException {
        try {
            FileWriter exclusionFileWriter = new FileWriter(filePath + "/exclusions.txt");
            for (String excluded : excludedChapters) {
                exclusionFileWriter.write(excluded + "\n");
            }
            exclusionFileWriter.close();
        } catch (IOException e) {
            throw new ExclusionFileException("Sorry, there was an error in accessing the Exclusion File");
        }
    }

    //@@author Darticune
    protected static boolean createChapterDue(String duePath, String dirPath) throws IOException {
        File due = new File(duePath);
        File dir = new File(dirPath);
        boolean isDirValid = dir.exists() && dir.isDirectory();
        if (!isDirValid) {
            return dir.mkdir() && due.createNewFile();
        } else if (!due.exists()) {
            return due.createNewFile();
        } else {
            return true;
        }
    }

    //@@author Zhu-Ze-Yu
    public static void createHistoryDir() throws StorageDataException {
        File f = new File("data/history");
        createDir(f);
    }

    //@@author Darticune
    protected static void writeDeadlineToChapterDue(String dueBy, String chapterPath) throws IOException {
        FileWriter fw = new FileWriter(chapterPath);
        fw.write(dueBy);
        fw.close();
    }

    private static void addEntryToExclusionFile(String moduleName, String chapterName,
                                                ArrayList<String> excludedChapters) {
        String chapterEntry = "Module: " + moduleName + "; Chapter: " + chapterName;
        if (!excludedChapters.contains(chapterEntry)) {
            excludedChapters.add(chapterEntry);
        }
    }

    //@@author Darticune
    protected static void appendModuleToExclusionFile(String moduleName, String filePath)
            throws FileNotFoundException, ExclusionFileException {
        ArrayList<String> excludedChapters = Storage.loadExclusionFile(filePath);
        String[] chaptersInModule = Storage.loadChaptersFromSpecifiedModule(moduleName, filePath);
        for (String chapter : chaptersInModule) {
            if (chapter.equals("dues")) {
                continue;
            }
            chapter = chapter.replace(".txt", "");
            addEntryToExclusionFile(moduleName, chapter, excludedChapters);
        }
        updateExclusionFile(excludedChapters, filePath);
    }

    //@@author Darticune
    protected static void appendChapterToExclusionFile(String moduleName, String chapterName, String filePath)
            throws FileNotFoundException,
            ExclusionFileException {
        ArrayList<String> excludedChapters = Storage.loadExclusionFile(filePath);
        File file = new File(filePath + "/" + moduleName + "/" + chapterName + ".txt");
        checkExists(file);
        addEntryToExclusionFile(moduleName, chapterName, excludedChapters);
        updateExclusionFile(excludedChapters, filePath);
    }

    private static void deleteEntryFromExclusionFile(String moduleName, ArrayList<String> excludedChapters,
                                                     String chapterName) {
        String chapterEntry = "Module: " + moduleName + "; Chapter: " + chapterName;
        excludedChapters.remove(chapterEntry);
    }

    //@@author Darticune
    protected static void removeModuleFromExclusionFile(String moduleName, String filePath)
            throws FileNotFoundException, ExclusionFileException {
        ArrayList<String> excludedChapters = Storage.loadExclusionFile(filePath);
        String[] chaptersInModule = Storage.loadChaptersFromSpecifiedModule(moduleName, filePath);
        for (String chapter : chaptersInModule) {
            String chapterName = chapter.replace(".txt", "");
            deleteEntryFromExclusionFile(moduleName, excludedChapters, chapterName);
        }
        updateExclusionFile(excludedChapters, filePath);
    }

    //@@author Darticune
    protected static void removeChapterFromExclusionFile(String moduleName, String chapterName, String filePath)
            throws FileNotFoundException, ExclusionFileException {
        ArrayList<String> excludedChapters = Storage.loadExclusionFile(filePath);
        deleteEntryFromExclusionFile(moduleName, excludedChapters, chapterName);
        updateExclusionFile(excludedChapters, filePath);
    }

    //@@author Zhu-Ze-Yu
    protected static void saveCards(CardList cards, String module, String chapter, String filePath)
            throws IOException {
        FileWriter fw = new FileWriter(filePath + "/" + module + "/" + chapter + ".txt");
        for (int i = 0; i < cards.getCardCount(); i++) {
            int ratingInt = cards.getCard(i).getRating();
            String ratingString = Integer.toString(ratingInt);
            fw.write(cards.getCard(i).toString()
                    + " | [P] " + cards.getCard(i).getPreviousInterval() + " | [R] "
                    + ratingString + "\n");
        }
        fw.close();
    }

    //@@author Darticune
    protected static void saveChapterDeadline(String dueBy, String moduleName, String chapterName, String filePath) {
        try {
            String dirPath = filePath + "/" + moduleName + "/" + "dues";
            String duePath = filePath + "/" + moduleName + "/" + "dues" + "/" + chapterName + "due" + ".txt";
            if (createChapterDue(duePath, dirPath)) {
                writeDeadlineToChapterDue(dueBy, duePath);
            } else {
                System.out.println("Unable to produce ChapterDue");
            }
        } catch (IOException e) {
            System.out.println("Error in saving chapter deadline");
        }
    }

    //@@author neojiaern
    /**
     * Deletes a directory recursively.
     *
     * @param directoryToBeDeleted file to be deleted
     * @return boolean result of the deletion
     */
    protected static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    //@@author Jane-Ng
    /**
     * Rename the filename of a chapter.
     *
     * @param newChapterName new chapter name to rename to
     * @param module module of the chapter to be renamed
     * @param chapter existing chapter to be renamed
     * @param filePath of the storage file
     * @throws StorageDataException if there is an error renaming the storage file
     */
    protected static void renameChapter(String newChapterName, Module module, Chapter chapter, String filePath)
            throws StorageDataException {
        File chapterFile = new File(filePath
                + "/" + module.getModuleName()
                + "/" + chapter.getChapterName() + ".txt");
        File dueFile = new File(filePath
                + "/" + module.getModuleName()
                + "/dues"
                + "/" + chapter.getChapterName() + "due.txt");
        boolean chapterSuccess = chapterFile.renameTo(new File(filePath
                + "/" + module.getModuleName()
                + "/" + newChapterName + ".txt"));
        boolean dueSuccess = dueFile.renameTo(new File(filePath
                + "/" + module.getModuleName()
                + "/dues"
                + "/" + newChapterName + "due.txt"));
        if (!chapterSuccess || !dueSuccess) {
            throw new StorageDataException("Error renaming the chapter. Please check your directory.");
        }
    }

    //@@author Jane-Ng
    /**
     * Rename the folder of a module.
     *
     * @param newModuleName new module name to rename to
     * @param module existing module to be renamed
     * @param filePath of the storage file
     * @throws StorageDataException if there is an error renaming the storage file
     */
    protected static void renameModule(String newModuleName, Module module, String filePath)
            throws StorageDataException {
        File file = new File(filePath + "/" + module.getModuleName());
        boolean success = file.renameTo(new File(filePath + "/" + newModuleName));
        if (!success) {
            throw new StorageDataException("Error renaming the module. Please check your directory.");
        }
    }

    //@@author Zhu-Ze-Yu
    /**
     * Create a txt file to store history.
     *
     * @param date the date of the revision
     * @throws IOException if there is an error creating the storage file
     * @throws StorageDataException if there is an error creating the storage file
     */
    protected static void createHistory(String date) throws IOException, StorageDataException {
        try {
            File f = new File("data/history/" + date + ".txt");
            createFile(f);
        } catch (IOException e) {
            throw new IOException("Error creating the data/history file.");
        } catch (StorageDataException e) {
            throw new StorageDataException("Error creating the new file.");
        }
    }

    //@@author Zhu-Ze-Yu
    /**
     * Save revision history into file.
     *
     * @param date the date of the revision
     * @param histories all chapters revised in the date
     * @throws IOException if there is an error creating the storage file
     */
    protected static void saveHistory(ArrayList<History> histories, String date) throws IOException {
        FileWriter fw = new FileWriter("data/history/" + date + ".txt");
        for (History h : histories) {
            fw.write(h.toString() + "\n");
        }
        fw.close();
    }

    //@@author neojiaern
    /**
     * Removes the chapter due file from due folder after chapter is deleted.
     *
     * @param module name of module
     * @param chapter name of chapter
     * @param filePath filepath of file to be deleted
     * @return boolean result of deletion
     */
    protected static boolean removeChapterFromDue(String module, String chapter, String filePath) {
        File fileToDelete = new File(filePath + "/" + module
                + "/dues/" + chapter + "due.txt");
        return deleteDirectory(fileToDelete);
    }
}
