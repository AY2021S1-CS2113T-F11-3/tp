package storage;

import common.KajiLog;
import exception.ExclusionFileException;
import exception.InvalidFileFormatException;
import manager.card.Card;
import manager.chapter.Chapter;
import manager.chapter.DueChapter;
import manager.history.History;
import manager.module.Module;
import scheduler.Scheduler;
import ui.Ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 *  Manages loading of Kaji data from local storage.
 */
public class StorageLoad {
    private static Logger logger = KajiLog.getLogger(Storage.class.getName());

    public static final String DELIMITER = " \\| ";
    public static final String MESSAGE_MODULE_CHAPTER = "Module: %1$s ; Chapter: %2$s";

    //@@author Darticune
    protected static void checkExists(File f) throws FileNotFoundException {
        boolean dirExists = f.exists();
        if (!dirExists) {
            throw new FileNotFoundException();
        }
    }

    //@@author Darticune
    protected static String[] loadChaptersFromSpecifiedModule(String moduleName, String filePath)
            throws FileNotFoundException {
        File file = new File(filePath + "/" + moduleName);
        boolean moduleExists = file.exists();
        if (!moduleExists) {
            throw new FileNotFoundException();
        }
        return file.list();
    }

    //@@author Darticune
    protected static String retrieveChapterDeadline(String moduleName, String chapterName, String filePath) {
        File f = new File(filePath + "/" + moduleName + "/dues/" + chapterName + "due" + ".txt");
        try {
            Scanner s = new Scanner(f);
            if (s.hasNext()) {
                String deadline = s.nextLine();
                s.close();
                return deadline;
            } else {
                s.close();
                return "Invalid Date";
            }
        } catch (FileNotFoundException e) {
            return "Does not exist";
        }
    }

    //@@author Darticune
    protected static ArrayList<String> loadExclusionFile(String filePath) throws ExclusionFileException {
        File exclusionFile = new File(filePath + "/exclusions.txt");
        ArrayList<String> excludedChapters = new ArrayList<>();
        try {
            if (!exclusionFile.exists()) {
                if (!exclusionFile.createNewFile()) {
                    throw new ExclusionFileException();
                } else {
                    return new ArrayList<String>();
                }
            }
            Scanner exclusionFileScanner = new Scanner(exclusionFile);
            while (exclusionFileScanner.hasNext()) {
                //to read the card
                String entry = exclusionFileScanner.nextLine();
                excludedChapters.add(entry);
            }
            exclusionFileScanner.close();
            return excludedChapters;
        } catch (IOException e) {
            throw new ExclusionFileException();
        }
    }

    //@@author Darticune
    protected static ArrayList<DueChapter> loadAllChaptersAsDueChapters(Ui ui, ArrayList<String> excludedChapters,
                                                                        ArrayList<DueChapter> dueChapters,
                                                                        String[] modules, String filePath)
                                                                  throws FileNotFoundException {
        for (String module : modules) {
            if (module.equals("exclusions.txt")) {
                continue;
            }
            File f2 = new File(filePath + "/" + module);
            boolean chapterExists = f2.exists();
            if (!chapterExists) {
                throw new FileNotFoundException();
            }
            String[] chapters = f2.list();
            if (chapters.length == 0) {
                return dueChapters;
            }
            for (String chapter : chapters) {
                convertChapterInToDueChapter(ui, excludedChapters, dueChapters, module, chapter, filePath);
            }
        }
        return dueChapters;
    }

    //@@author Darticune
    private static void convertChapterInToDueChapter(Ui ui, ArrayList<String> excludedChapters,
                                                     ArrayList<DueChapter> dueChapters, String module, String chapter,
                                                     String filePath) {
        if (chapter.equals("dues")) {
            return;
        }
        String target = chapter.replace(".txt", "");
        String entry = "Module: " + module + "; Chapter: " + target;
        if (excludedChapters.contains(entry)) {
            return;
        }
        checkChapterDeadline(ui, dueChapters, module, target, filePath);
    }

    //@@author Darticune
    private static void checkChapterDeadline(Ui ui, ArrayList<DueChapter> dueChapters, String module,
                                             String target, String filePath) {
        String deadline = retrieveChapterDeadline(module, target, filePath);
        assert !deadline.equals(null) : "Invalid deadline retrieved";
        if (deadline.equals("Invalid Date")) {
            ui.showToUser("\nThe chapter:");
            ui.showToUser(String.format(MESSAGE_MODULE_CHAPTER, module, target)
                    + " has a corrupted deadline. Please revise it ASAP! It will be considered due!\n");
            dueChapters.add(new DueChapter(module, new Chapter(target, Scheduler.parseDate(deadline))));
        } else if (deadline.equals("Does not exist")) {
            deadline = "Invalid Date";
            ui.showToUser("\nThe chapter:");
            ui.showToUser(String.format(MESSAGE_MODULE_CHAPTER, module, target)
                    + " has a corrupted deadline. Please revise it ASAP! It will be considered due!\n");
            dueChapters.add(new DueChapter(module, new Chapter(target, Scheduler.parseDate(deadline))));
        } else {
            dueChapters.add(new DueChapter(module, new Chapter(target, Scheduler.parseDate(deadline))));
        }
    }

    //@@author gua-guargia
    protected static ArrayList<Module> loadModule(String filePath) throws FileNotFoundException {
        File f = new File(filePath);
        checkExists(f);

        ArrayList<Module> modules = new ArrayList<>();
        String[] contents = f.list();
        String result = "List of files and directories in the specified directory:";
        for (String content : contents) {
            if (content.equals("exclusions.txt")) {
                continue;
            }
            result += "\n" + content;
            modules.add(new Module(content));
        }
        logger.info(result);
        return modules;
    }

    //@@author Jane-Ng
    /**
     * Loads all the chapters of the specified {@code module}.
     *
     * @param module module name of the chapters
     * @param filePath of the storage file
     * @return list of chapters
     * @throws IOException if there is an error when loading from storage file
     */
    protected static ArrayList<Chapter> loadChapter(String module, String filePath) throws IOException {
        File f = new File(filePath + "/" + module);
        checkExists(f);

        ArrayList<Chapter> chapters = new ArrayList<>();
        String[] contents = f.list();
        if (contents.length == 0) {
            return chapters;
        }
        String result = "List of files and directories in the specified directory:";
        for (String content : contents) {
            if (content.equals("dues")) {
                continue;
            }
            String target = content.replace(".txt", "");
            result += content;

            String deadline = retrieveChapterDeadline(module, target, filePath);
            assert !deadline.equals(null) : "Invalid deadline retrieved";
            if (deadline.equals("Invalid Date")) {
                chapters.add(new Chapter(target));
                continue;
            }
            if (deadline.equals("Does not exist")) {
                String dirPath = filePath + "/" + module + "/" + "dues";
                String duePath = filePath + "/" + module + "/" + "dues" + "/" + target + "due" + ".txt";
                boolean success = Storage.createChapterDue(duePath, dirPath);
                if (!success) {
                    throw new IOException("Error creating chapter due file.");
                }
                chapters.add(new Chapter(target));
                continue;
            }

            chapters.add(new Chapter(target, Scheduler.parseDate(deadline)));
        }
        logger.info(result);
        return chapters;
    }

    //@@author Darticune
    protected static ArrayList<DueChapter> loadAllDueChapters(Ui ui, String filePath)
            throws FileNotFoundException, ExclusionFileException {
        ArrayList<String> excludedChapters = loadExclusionFile(filePath);
        File admin = new File(filePath);
        checkExists(admin);
        String[] modules = admin.list();
        ArrayList<DueChapter> dueChapters = new ArrayList<>();
        return loadAllChaptersAsDueChapters(ui, excludedChapters, dueChapters, modules, filePath);
    }

    //@@author Zhu-Ze-Yu
    /**
     * Loads all the cards of the specified {@code chapter}.
     *
     * @param module module name of the chapters
     * @param chapter chapter name of the cards
     * @param filePath of the storage file
     * @return list of cards
     * @throws FileNotFoundException if there is not any cards in the storage file
     */
    protected static ArrayList<Card> loadCard(String module, String chapter, String filePath)
            throws FileNotFoundException {
        File f = new File(filePath + "/" + module + "/" + chapter + ".txt");
        checkExists(f);

        ArrayList<Card> cards = new ArrayList<>();
        Scanner s = new Scanner(f);
        while (s.hasNext()) {
            //to read the card
            String fileCommand = s.nextLine();
            String[] args = fileCommand.split(DELIMITER, 4);
            try {
                String question = StorageParser.parseQuestionInFile(args[0]);
                String answer = StorageParser.parseAnswerInFile(args[1]);
                String interval = StorageParser.parsePreIntervalInFile(args[2]);
                String rating = StorageParser.parseRatingInFile(args[3]);
                int preInterval = Integer.parseInt(interval);
                int intRating = Integer.parseInt(rating);

                Card card = new Card(question, answer, preInterval, intRating);
                cards.add(card);
            } catch (InvalidFileFormatException e) {
                return null;
            }
        }
        s.close();
        return cards;
    }

    //@@author Zhu-Ze-Yu
    /**
     * Loads the revision history.
     *
     * @param date the date for the revision
     * @return list of revision history
     * @throws FileNotFoundException if there is not any revision in the storage file
     */
    protected static ArrayList<History> loadHistory(String date) throws FileNotFoundException {
        File f = new File("data/history/" + date + ".txt");
        checkExists(f);

        ArrayList<History> histories = new ArrayList<>();
        Scanner s = new Scanner(f);
        while (s.hasNext()) {
            //to read the history
            String revision = s.nextLine();
            String[] name = revision.split("/", 2);
            try {
                String moduleName = StorageParser.parseTaskNameInFile(name[0]);
                String chapterName = StorageParser.parseTaskNameInFile(name[1]);

                History history = new History(moduleName, chapterName);
                histories.add(history);
            } catch (InvalidFileFormatException e) {
                return null;
            }
        }
        s.close();
        return histories;
    }
}
