package storage;

import common.KajiLog;
import exception.StorageDataException;
import exception.ExclusionFileException;
import manager.card.Card;
import manager.history.History;
import manager.chapter.CardList;
import manager.chapter.Chapter;
import manager.chapter.DueChapter;
import manager.module.Module;
import ui.Ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Storage {
    private static Logger logger = KajiLog.getLogger(Storage.class.getName());

    public static final String QUESTION_PREFIX = "[Q]";
    public static final String ANSWER_PREFIX = "[A]";
    public static final String PREVIOUS_INTERVAL_PREFIX = "[P]";
    public static final String RATING_PREFIX = "[R]";

    protected String filePath;

    public Storage(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    //@@author gua-guargia
    //create the folder --> 'data/admin'
    public void createAdmin() {
        File f = new File(filePath);
        logger.info("Filepath: " + filePath);

        StorageWrite.createDir(f.getParentFile());
        StorageWrite.createDir(f);

        StorageWrite.createHistoryDir();
    }

    //@@author gua-guargia
    public void createModule(String moduleName) {
        File f = new File(filePath + "/" + moduleName);
        StorageWrite.createDir(f);
    }

    //@@author gua-guargia
    public void createChapter(String chapterName, String moduleName) throws IOException {
        File f = new File(filePath + "/" + moduleName + "/" + chapterName + ".txt");
        StorageWrite.createFile(f);
    }

    public ArrayList<Module> loadModule() throws FileNotFoundException {
        return StorageLoad.loadModule(filePath);
    }

    public ArrayList<Chapter> loadChapter(String module) throws IOException {
        return StorageLoad.loadChapter(module, filePath);
    }

    public ArrayList<DueChapter> loadAllDueChapters(Ui ui) throws FileNotFoundException, ExclusionFileException {
        return StorageLoad.loadAllDueChapters(ui, filePath);
    }

    public void appendModuleToExclusionFile(String moduleName) throws FileNotFoundException, ExclusionFileException {
        StorageWrite.appendModuleToExclusionFile(moduleName, filePath);
    }

    public void appendChapterToExclusionFile(String moduleName, String chapterName) throws FileNotFoundException,
            ExclusionFileException {
        StorageWrite.appendChapterToExclusionFile(moduleName, chapterName, filePath);
    }

    public void removeModuleFromExclusionFile(String moduleName) throws FileNotFoundException, ExclusionFileException {
        StorageWrite.removeModuleFromExclusionFile(moduleName, filePath);
    }

    public void removeChapterFromExclusionFile(String moduleName, String chapterName) throws FileNotFoundException,
            ExclusionFileException {
        StorageWrite.removeChapterFromExclusionFile(moduleName, chapterName, filePath);
    }

    public ArrayList<Card> loadCard(String module, String chapter) throws FileNotFoundException {
        return StorageLoad.loadCard(module, chapter, filePath);
    }

    public void saveCards(CardList cards, String module, String chapter) throws IOException {
        StorageWrite.saveCards(cards,module, chapter, filePath);
    }

    public void saveChapterDeadline(String dueBy, String moduleName, String chapterName) {
        StorageWrite.saveChapterDeadline(dueBy, moduleName, chapterName, filePath);
    }

    public boolean deleteDirectory(File directoryToBeDeleted) {
        return StorageWrite.deleteDirectory(directoryToBeDeleted);
    }

    public void renameChapter(String newChapterName, Module module, Chapter chapter) throws StorageDataException {
        StorageWrite.renameChapter(newChapterName, module, chapter, filePath);
    }

    public void renameModule(String newModuleName, Module module) throws StorageDataException {
        StorageWrite.renameModule(newModuleName, module, filePath);
    }

    public void createHistory(String date) throws IOException {
        StorageWrite.createHistory(date);
    }

    public void saveHistory(ArrayList<History> histories, String date) throws IOException {
        StorageWrite.saveHistory(histories, date);
    }

    public ArrayList<History> loadHistory(String date) throws FileNotFoundException {
        return StorageLoad.loadHistory(date);
    }

    public boolean removeChapterFromDue(String module, String chapter) {
        return StorageWrite.removeChapterFromDue(module, chapter, filePath);
    }

    public static boolean createChapterDue(String duePath, String dirPath) throws IOException {
        return StorageWrite.createChapterDue(duePath, dirPath);
    }

    public static ArrayList<String> loadExclusionFile(String filePath) throws ExclusionFileException {
        return StorageLoad.loadExclusionFile(filePath);
    }

    public static String[] loadChaptersFromSpecifiedModule(String moduleName, String filePath)
            throws FileNotFoundException {
        return StorageLoad.loadChaptersFromSpecifiedModule(moduleName, filePath);
    }

}
