package commands;

import access.Access;
import manager.card.Card;
import manager.chapter.Chapter;
import manager.history.History;
import manager.history.HistoryList;
import scheduler.Scheduler;
import storage.Storage;
import ui.Ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;


/**
 * Starts revision for a particular chapter.
 */
public class ReviseCommand extends Command {
    public static final String COMMAND_WORD = "revise";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Starts revision based on a particular chapter. \n"
            + "Parameters: INDEX_OF_CHAPTER\n" + "Example: " + COMMAND_WORD + " 2\n";

    public static final String MESSAGE_SUCCESS = "You have completed revision for %1$s.";
    public static final String MESSAGE_NO_CARDS_IN_CHAPTER = "You currently have no cards in %1$s.";
    public static final String MESSAGE_CHAPTER_NOT_DUE = "The chapter %1$s is not due for revision today.\n";
    public static final String MESSAGE_SHOW_ANSWER_PROMPT = "\n[enter s to show answer]";
    public static final String MESSAGE_SHOW_RATING_PROMPT = "How well did you do for this card?\n"
            + "[enter e(easy), m(medium), h(hard), c(cannot answer)]";
    public static final String MESSAGE_SHOW_REVISE_PROMPT = "Are you sure you want to revise this? (Y/N)";
    public static final String MESSAGE_START_REVISION = "The revision for %s will start now:";
    public static final String EASY = "e";
    public static final String MEDIUM = "m";
    public static final String HARD = "h";
    public static final String CANNOT_ANSWER = "c";

    private final int reviseIndex;

    public ReviseCommand(int reviseIndex) {
        this.reviseIndex = reviseIndex;
    }

    public Chapter getChapter(int reviseIndex, Access access) throws IndexOutOfBoundsException {
        Chapter chapter;
        try {
            chapter = access.getModule().getChapters().getChapter(reviseIndex);
            return chapter;
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("The chapter is not found.\n");
        }
    }

    private ArrayList<Card> getCards(Access access, Storage storage, Chapter toRevise)
            throws FileNotFoundException {
        ArrayList<Card> allCards;
        try {
            allCards = storage.loadCard(access.getModuleLevel(), toRevise.getChapterName());
            toRevise.setCards(allCards);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File is not found.\n");
        }
        return allCards;
    }

    private int reviseCard(int count, Card c, Ui ui, ArrayList<Card> repeatCards) {
        ui.showToUser("\nQuestion " + count + ":");
        ui.showCardRevision(c);
        String input = ui.getInput(MESSAGE_SHOW_RATING_PROMPT);
        rateCard(ui, repeatCards, c, input);
        return ++count;
    }

    @Override
    public void execute(Ui ui, Access access, Storage storage) throws IOException {
        Chapter toRevise = getChapter(reviseIndex, access);
        if (!Scheduler.isDeadlineDue(toRevise.getDueBy())) {
            StringBuilder prompt = new StringBuilder();
            prompt.append(String.format(MESSAGE_CHAPTER_NOT_DUE, toRevise));
            prompt.append(MESSAGE_SHOW_REVISE_PROMPT);
            String input = ui.getInput(prompt.toString()).trim().toUpperCase();
            boolean isInvalid = true;
            while (isInvalid) {
                switch (input) {
                case "Y":
                    isInvalid = false;
                    break;
                case "N":
                    return;
                default:
                    input = ui.getInput("You have entered an invalid input, please try again.")
                            .trim().toUpperCase();
                }
            }
        }

        ArrayList<Card> allCards = getCards(access, storage, toRevise);
        ArrayList<Card> repeatCards = new ArrayList<>();
        int cardCount = allCards.size();
        ui.showToUser("\nCard count: " + cardCount);
        if (cardCount == 0) {
            ui.showToUser(String.format(MESSAGE_NO_CARDS_IN_CHAPTER, toRevise));
            return;
        }

        ui.showToUser(String.format(MESSAGE_START_REVISION, toRevise));

        int count = 1;

        for (Card c : allCards) {
            count = reviseCard(count, c, ui, repeatCards);
        }
        repeatRevision(ui, repeatCards, count);
        ui.showToUser(String.format(MESSAGE_SUCCESS, toRevise));
        toRevise.setDueBy(Scheduler.computeDeckDeadline(toRevise.getCards()), storage, access);
        addHistory(ui, access, storage);
    }

    private void addHistory(Ui ui, Access access, Storage storage) throws IOException {
        LocalDate date = java.time.LocalDate.now();
        storage.createHistory(ui, date.toString());
        String moduleName = access.getModule().getModuleName();
        String chapterName = access.getChapter().getChapterName();
        History history = new History(moduleName, chapterName, 100);
        HistoryList histories = access.getAdmin().getHistories();
        histories.addHistory(history);
        storage.saveHistory(histories, date.toString());
    }

    public static ArrayList<Card> rateCard(Ui ui, ArrayList<Card> repeatCards, Card c, String input) {
        boolean isInvalid = true;

        while (isInvalid) {
            switch (input.trim().toLowerCase()) {
            case EASY:
                c.setPreviousInterval(Scheduler.computeEasyInterval(c.getPreviousInterval()));
                isInvalid = false;
                break;
            case MEDIUM:
                c.setPreviousInterval(Scheduler.computeMediumInterval(c.getPreviousInterval()));
                isInvalid = false;
                break;
            case HARD:
                c.setPreviousInterval(Scheduler.computeHardInterval(c.getPreviousInterval()));
                isInvalid = false;
                break;
            case CANNOT_ANSWER:
                repeatCards.add(c);
                isInvalid = false;
                break;
            default:
                input = ui.getInput("You have entered an invalid input, please try again.");
            }
        }
        return repeatCards;
    }

    private void repeatRevision(Ui ui, ArrayList<Card> cards, int count) {
        while (cards.size() != 0) {
            ArrayList<Card> repeatCards = new ArrayList<>();
            for (Card c : cards) {
                count = reviseCard(count, c, ui, repeatCards);
            }
            cards = new ArrayList<>(repeatCards);
        }
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
