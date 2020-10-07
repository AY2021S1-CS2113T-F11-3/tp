package seedu.duke.command;

import seedu.duke.tool.Access;
import seedu.duke.tool.Storage;
import seedu.duke.tool.Ui;

public class backModuleCommand extends Command {
    public backModuleCommand(String fullCommand) {
        super(fullCommand);
    }

    @Override
    public void execute(Access access, Ui ui, Storage storage) {
        access.setModuleLevel("");
    }
}
