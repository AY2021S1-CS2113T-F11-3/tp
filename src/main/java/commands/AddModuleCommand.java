package commands;

import access.Access;
import exception.StorageDataException;
import manager.admin.Admin;
import manager.admin.ModuleList;
import manager.module.Module;
import storage.Storage;
import ui.Ui;

import static common.Messages.MODULE;
import static common.Messages.MESSAGE_ITEM_EXISTED;

/**
 * Adds a module to a admin.
 */
public class AddModuleCommand extends AddCommand {
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a new module. \n"
            + "Parameters:" + MODULE_PARAMETERS + "\n"
            + "Example: " + COMMAND_WORD + " CS2113T\n";


    private String module;

    /**
     * Creates an AddModuleCommand to add a new {@code module} with name entered by the user.
     *
     * @param module name of the new module that users would like to create
     */
    public AddModuleCommand(String module) {
        this.module = module;
    }

    @Override
    public void execute(Ui ui, Access access, Storage storage) throws StorageDataException {
        Module module = new Module(this.module);
        String result = addModule(access, storage, module);
        ui.showToUser(result);
    }

    /**
     * Adds a module.
     *
     * @param access temporary access data about user's current access level
     * @param storage file storage and file management of Kaji
     * @param module new module that user would like to create
     * @return result to be displayed
     */
    private String addModule(Access access, Storage storage, Module module) throws StorageDataException {
        Admin newAdmin = access.getAdmin();
        ModuleList modules = newAdmin.getModules();
        boolean isModuleExist = modules.checkModuleExistence(module.getModuleName().toLowerCase());
        if (isModuleExist) {
            String result = String.format(MESSAGE_ITEM_EXISTED, MODULE, module.getModuleName(), MODULE);
            return result;
        }
        modules.addModule(module);
        int moduleCount = modules.getModuleCount();
        access.setAdmin(newAdmin);
        storage.createModule(module.getModuleName());
        return prepareResult(MODULE, module.toString(), moduleCount);
    }
}
