package seedu.duke.command;

import seedu.duke.level.Module;
import seedu.duke.tool.Access;
import seedu.duke.tool.Storage;
import seedu.duke.tool.Ui;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class goModuleCommand extends Command {
    public goModuleCommand(String fullCommand) {
        super(fullCommand);
    }

    @Override
    public void execute(Access access, Ui ui, Storage storage) {
        String filter = fullCommand.replace("goModule ", "");
        boolean isLevelExist = false;
        ArrayList<Module> modules = access.getAdmin().getModules();
        for (Module module : modules) {
            if(filter.equalsIgnoreCase(module.getModule())) {
                access.setModuleLevel(filter);
                isLevelExist = true;
                try {
                    Module newModule = new Module(module.getModule(), storage.loadChapter(module.getModule()));
                    access.setModule(newModule);
                } catch (FileNotFoundException e) {
                    Module newModule = new Module(module.getModule());
                    access.setModule(newModule);
                    System.out.println("Hihi, seems like it is a new module, you can try to add chapter inside!");
                }
                break;
            }
        }
        if (isLevelExist == false) {
            System.out.println("Sorry, I cannot find this module, please add this module first");
        }
    }
}
