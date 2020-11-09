package commands;

import access.Access;
import exception.StorageDataException;
import manager.chapter.Chapter;
import manager.module.ChapterList;
import manager.module.Module;
import storage.Storage;
import ui.Ui;

import java.io.IOException;

import static common.Messages.CHAPTER;
import static common.Messages.MESSAGE_ITEM_EXISTED;

/**
 * Adds a chapter to a module.
 */
public class AddChapterCommand extends AddCommand {
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a chapter to the module. \n"
            + "Parameters:" + CHAPTER_PARAMETERS + "\n"
            + "Example: " + COMMAND_WORD + " Chapter 1\n";

    private String chapter;

    /**
     * Creates an AddChapterCommand to add a new {@code chapter} with name entered by the user.
     *
     * @param chapter name of the new chapter that users would like to create
     */
    public AddChapterCommand(String chapter) {
        this.chapter = chapter;
    }

    @Override
    public void execute(Ui ui, Access access, Storage storage) throws IOException, StorageDataException {
        Chapter chapter = new Chapter(this.chapter);
        String result = addChapter(access, storage, chapter);
        ui.showToUser(result);
    }

    /**
     * Adds a chapter.
     *
     * @param access temporary access data about user's current access level
     * @param storage file storage and file management of Kaji
     * @param chapter new chapter that user would like to create
     * @return result to be displayed
     * @throws IOException if there is an error writing to the storage file
     */
    private String addChapter(Access access, Storage storage, Chapter chapter)
            throws IOException, StorageDataException {
        Module newModule = access.getModule();
        ChapterList chapters = newModule.getChapters();
        boolean isChapterExist = chapters.checkChapterExistence(chapter.getChapterName().toLowerCase());
        if (isChapterExist) {
            String result = String.format(MESSAGE_ITEM_EXISTED, CHAPTER, chapter.getChapterName(), CHAPTER);
            return result;
        }
        Chapter newChapter = new Chapter(this.chapter, rateChapter(), storage, access);
        chapters.addChapter(newChapter);
        int chapterCount = chapters.getChapterCount();
        access.setModule(newModule);
        storage.createChapter(chapter.getChapterName(), access.getModuleLevel());
        return prepareResult(CHAPTER, chapter.toString(), chapterCount);
    }

    /**
     * Give an initial rating to a new chapter.
     *
     * @return the corresponding rate of chapter entered by user
     */
    public String rateChapter() {
        if (Ui.chooseToRateNewDeck()) {
            return Ui.getChoiceOfNewDeckRating();
        } else {
            return "N";
        }
    }
}
