package commands;

import manager.chapter.CardList;
import manager.chapter.Chapter;
import manager.module.ChapterList;
import manager.module.Module;
import access.Access;
import storage.Storage;
import ui.Ui;

public class AddChapterCommand extends AddCommand {
    private final Chapter chapter;
    public static final String CHAPTER_PARAMETERS = " CHAPTER_NAME";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a chapter to the module. \n"
            + "Parameters:" + CHAPTER_PARAMETERS + "\n"
            + "Example: " + COMMAND_WORD + " Chapter 1\n";

    public AddChapterCommand(String chapterCode) {
        this.chapter = new Chapter(chapterCode);
    }

    @Override
    public void execute(CardList cards, Ui ui, Access access, Storage storage) {
        if (access.isAdminLevel() || access.isChapterLevel()) {
            System.out.println("Sorry, you currently are in the admin/chapter level, "
                    + "please go to module level first.");
            return;
        }

        Module newModule = access.getModule();
        ChapterList chapters = newModule.getChapters();
        chapters.addChapter(chapter);
        int chapterCount = chapters.getChapterCount();
        ui.showChapterAdded(chapter, chapterCount);
        access.setModule(newModule);
        storage.createChapter(chapter.getChapterName(), access.getModuleLevel());
    }

    @Override
    public boolean isExit() {
        return false;
    }
}

