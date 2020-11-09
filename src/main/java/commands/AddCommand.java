package commands;

import access.Access;
import exception.StorageDataException;
import storage.Storage;
import ui.Ui;

import java.io.IOException;

/**
 * Represents an AddCommand which contains methods used in the various AddCommand classes.
 */
public abstract class AddCommand extends Command {
    public static final String COMMAND_WORD = "add";

    public static final String CHAPTER_PARAMETERS = " CHAPTER_NAME";

    public static final String MODULE_PARAMETERS = " MODULE_NAME";

    public static final String CARD_PARAMETERS = " q:QUESTION | a:ANSWER";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a module / chapter / flashcard.\n"
            + "Parameters:" + MODULE_PARAMETERS + "\n"
            + "           " + CHAPTER_PARAMETERS + "\n"
            + "           " + CARD_PARAMETERS + "\n"
            + "Example: " + COMMAND_WORD + " CS2113T\n"
            + "         " + COMMAND_WORD + " Chapter 1\n"
            + "         " + COMMAND_WORD + " q:What is the result of one plus one | a:two\n";

    public static final String MESSAGE_SUCCESS = "Got it. I've added this %1$s:\n";
    public static final String MESSAGE_COUNT = "Now you have %1$d %2$s(s) in the list.";

    @Override
    public abstract void execute(Ui ui, Access access, Storage storage) throws IOException, StorageDataException;

    /**
     * Constructs the result of the command execution.
     *
     * @param type module, chapter or card type
     * @param content content that is added
     * @param count total number of modules, chapters or flashcards
     * @return result to be displayed
     */
    public String prepareResult(String type, String content, int count) {
        StringBuilder result = new StringBuilder();
        result.append(String.format(MESSAGE_SUCCESS, type));
        result.append(content).append("\n");
        result.append(String.format(MESSAGE_COUNT, count, type));
        return result.toString();
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
