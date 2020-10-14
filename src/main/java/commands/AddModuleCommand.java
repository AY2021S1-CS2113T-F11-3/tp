package commands;

import access.Access;
import manager.admin.Admin;
import manager.admin.ModuleList;
import manager.chapter.CardList;
import manager.module.Module;
import storage.Storage;
import ui.Ui;

public class AddModuleCommand extends AddCommand {
    public static final String MODULE_PARAMETERS = " MODULE_NAME";
    private final Module module;

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a new module. \n"
            + "Parameters:" + MODULE_PARAMETERS + "\n"
            + "Example: " + COMMAND_WORD + " CS2113T\n";

    public AddModuleCommand(String moduleCode) {
        this.module = new Module(moduleCode);
    }

    @Override
    public void execute(CardList cards, Ui ui, Access access, Storage storage) {
        if (access.isModuleLevel() || access.isChapterLevel()) {
            System.out.println("Sorry, you currently are in the module/chapter level, "
                    + "please go to admin level first.");
            return;
        }

        Admin newAdmin = access.getAdmin();
        ModuleList modules = newAdmin.getModules();
        modules.addModule(module);
        int moduleCount = modules.getModuleCount();
        ui.showModuleAdded(module, moduleCount);
        access.setAdmin(newAdmin);
        storage.createModule(module.getModuleName());
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
