package commands;

import access.Access;
import exception.InvalidInputException;
import exception.StorageDataException;
import manager.history.History;
import storage.Storage;
import ui.Ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

import static common.Messages.CARD;

//@@author Zhu-Ze-Yu
/**
 * Lists all the revision history in a day.
 */
public class HistoryCommand extends Command {
    public static final String COMMAND_WORD = "history";
    public static final String DATE_PARAMETER = " DATE";
    public static final String MESSAGE_DOES_NOT_EXIST = "You did not have any revision in this session.";
    public static final String MESSAGE_EXIST = "Here is the revision completed in the session/in a day:\n";

    private String date;

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists the revision completed in the session/in a day. \n"
            + "Parameters:" + DATE_PARAMETER + "\n"
            + "Example: " + COMMAND_WORD + " 2020-10-10\n";

    /**
     * Creates a HistoryCommand to list the revision completed in the {@code date}.
     *
     * @param date the date of the revision history the user want to list
     */
    public HistoryCommand(String date) {
        this.date = date;
    }

    @Override
    public void execute(Ui ui, Access access, Storage storage) {
        String result = listHistory(storage);
        ui.showToUser(result);
    }

    /**
     * Add module and chapter name revised into history.
     *
     * @param access to get the module and chapter name
     * @param storage to save the history content to the storage file
     * @param reviseIndex to get which chapter have revised
     * @throws IOException if there is an error writing to the storage file
     */
    public static void addHistory(Access access, Storage storage, int reviseIndex)
            throws IOException, StorageDataException {
        LocalDate date = java.time.LocalDate.now();
        storage.createHistory(date.toString());
        String moduleName = access.getModule().getModuleName();
        String chapterName = access.getModule().getChapters().getChapter(reviseIndex).getChapterName();
        History history = new History(moduleName, chapterName);
        ArrayList<History> histories = storage.loadHistory(date.toString());;
        histories.add(history);
        storage.saveHistory(histories, date.toString());
    }

    /**
     * list the revision history.
     *
     * @param storage to get the history content in the storage file
     * @return result to be displayed
     */
    private String listHistory(Storage storage) {
        try {
            ArrayList<History> histories = storage.loadHistory(date);;
            StringBuilder result = new StringBuilder();
            result.append(MESSAGE_EXIST);
            for (History h : histories) {
                result.append("\n").append(histories.indexOf(h) + 1).append(".").append(h);
            }
            return result.toString();
        } catch (FileNotFoundException e) {
            return MESSAGE_DOES_NOT_EXIST;
        }
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
