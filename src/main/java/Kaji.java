import access.Access;
import commands.Command;
import exception.IncorrectAccessLevelException;
import exception.InvalidInputException;
import exception.InvalidFileFormatException;
import manager.admin.Admin;
import manager.chapter.CardList;
import parser.Parser;
import storage.Storage;
import ui.Ui;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Kaji {
    private CardList cards;
    private Ui ui;
    private Access access;
    private Storage storage;


    public Kaji(String filePath) {
        ui = new Ui();
        cards = new CardList();
        storage = new Storage(filePath);
        try {
            Admin admin = new Admin(storage.loadModule());
            access = new Access(admin);
        } catch (FileNotFoundException e) {
            storage.createAdmin();
            access = new Access();
        }
    }

    public void run() {
        ui.showWelcome();
        ui.showHelpList();
        boolean isExit = false;
        while (!isExit) {
            try {
                ui.showLevel(access);
                String fullCommand = ui.readCommand();
                Command c = Parser.parse(fullCommand, access);
                c.execute(cards, ui, access, storage);
                ui.printEmptyLine();
                isExit = c.isExit();
            } catch (InvalidInputException | IncorrectAccessLevelException | IOException 
                     | IndexOutOfBoundsException | InvalidFileFormatException e) {
                ui.showError(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new Kaji("data/admin").run();
    }
}
