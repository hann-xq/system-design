package nl.saxion;

import nl.saxion.Models.DTOs.PrintDTO;
import nl.saxion.Models.DTOs.PrintTaskDTO;
import nl.saxion.Models.DTOs.SpoolDTO;
import nl.saxion.Models.models.FilamentType;
import nl.saxion.Models.observer.DashboardObserver;
import nl.saxion.Models.printers.Printer;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main implements DashboardObserver {
    PrintFacade facade = new PrintFacade();
    Scanner scanner = new Scanner(System.in);
    int spoolChanges;
    int printsFulfilled;

    public static void main(String[] args) {
        new Main().run(args);
    }

    public Main() {
        this.spoolChanges = 0;
        this.printsFulfilled = 0;
        this.facade.addObserver(this);
    }

    public void run(String[] args) {
        if (args.length > 0) {
            facade.readPrintsFromFile(args[0]);
            facade.readSpoolsFromFile(args[1]);
            facade.readPrintersFromFile(args[2]);
        } else {
            facade.readPrintsFromFile("");
            facade.readSpoolsFromFile("");
            facade.readPrintersFromFile("");
        }
        int choice = 1;
        while (choice > 0 && choice < 10) {
            menu();
            choice = menuChoice(9);
            System.out.println("-----------------------------------");
            if (choice == 1) {
                addNewPrintTask();
            } else if (choice == 2) {
                registerPrintCompletion();
            } else if (choice == 3) {
                registerPrinterFailure();
            } else if (choice == 4) {
                changePrintStrategy();
            } else if (choice == 5) {
                facade.startPrintQueue();
            } else if (choice == 6) {
                showPrints();
            } else if (choice == 7) {
                showPrinters();
            } else if (choice == 8) {
                showSpools();
            } else if (choice == 9) {
                showPendingPrintTasks();
            } else if (choice == 10) {
                showDashboard();
            }
        }
        exit();
    }

    private void showDashboard() {
        System.out.println("Spools changed: " + spoolChanges + ".\nPrints fulfilled: " + printsFulfilled + ".\n");
    }

    /**
     * Print the menu
     */
    public void menu() {
        System.out.println("------------- Menu ----------------");
        System.out.println("- 1) Add new Print Task");
        System.out.println("- 2) Register Printer Completion");
        System.out.println("- 3) Register Printer Failure");
        System.out.println("- 4) Change printing style");
        System.out.println("- 5) Start Print Queue");
        System.out.println("- 6) Show prints");
        System.out.println("- 7) Show printers");
        System.out.println("- 8) Show spools");
        System.out.println("- 9) Show pending print tasks");
        System.out.println("- 10) Show dashboard");
        System.out.println("- 0) Exit");

    }

    private void exit() {
        this.facade.removeObserver(this);

    }

    private void changePrintStrategy() {
        System.out.println("---------- Change Strategy -------------");
        System.out.println("- Current strategy: " + facade.getStrategy());
        System.out.println("- 1: Less Spool Changes");
        System.out.println("- 2: Efficient Spool Usage");
        System.out.println("- Choose strategy: ");
        int strategyChoice = numberInput(1, 2);
        if (strategyChoice == 1) {
            facade.selectPrintStrategy(1);
        } else if (strategyChoice == 2) {
            facade.selectPrintStrategy(2);
        }
        System.out.println("-----------------------------------");
    }

    // TODO: This should be based on which printer is finished printing.
    private void registerPrintCompletion() {
        List<Printer> printers = facade.getPrinters();
        System.out.println("---------- Currently Running Printers ----------");
        for (Printer p : printers) {
            PrintTaskDTO printerCurrentTask = facade.getPrinterCurrentTask(p);
            if (printerCurrentTask != null) {
                System.out.println("- " + p.getId() + ": " + p.getName() + " - " + printerCurrentTask);
            }
        }
        System.out.print("- Printer that is done (ID): ");
        int printerId = numberInput(-1, printers.size());
        System.out.println("-----------------------------------");
        facade.registerCompletion(printerId);
    }

    private void registerPrinterFailure() {
        List<Printer> printers = facade.getPrinters();
        System.out.println("---------- Currently Running Printers ----------");
        for (Printer p : printers) {
            PrintTaskDTO printerCurrentTask = facade.getPrinterCurrentTask(p);
            if (printerCurrentTask != null) {
                System.out.println("- " + p.getId() + ": " + p.getName() + " > " + printerCurrentTask);
            }
        }
        System.out.print("- Printer ID that failed: ");
        int printerId = numberInput(1, printers.size());

        facade.registerPrinterFailure(printerId);
        System.out.println("-----------------------------------");
    }

    private void addNewPrintTask() {
        List<String> colors = new ArrayList<>();
        var prints = facade.getPrints();
        System.out.println("---------- New Print Task ----------");
        System.out.println("---------- Available prints ----------");
        int counter = 1;
        for (PrintDTO p : prints) {
            System.out.println("- " + counter + ": " + p.name());
            counter++;
        }

        System.out.print("- Print number: ");
        int printNumber = numberInput(1, prints.size());
        System.out.println("--------------------------------------");
        PrintDTO print = facade.findPrint(printNumber - 1);
        String printName = print.name();
        System.out.println("---------- Filament Type ----------");
        System.out.println("- 1: PLA");
        System.out.println("- 2: PETG");
        System.out.println("- 3: ABS");
        System.out.print("- Filament type number: ");
        int ftype = numberInput(1, 3);
        System.out.println("--------------------------------------");
        FilamentType type;
        switch (ftype) {
            case 1:
                type = FilamentType.PLA;
                break;
            case 2:
                type = FilamentType.PETG;
                break;
            case 3:
                type = FilamentType.ABS;
                break;
            default:
                System.out.println("- Not a valid filamentType, bailing out");
                return;
        }
        var spools = facade.getSpools();
        System.out.println("---------- Colors ----------");
        ArrayList<String> availableColors = new ArrayList<>();
        counter = 1;
        for (SpoolDTO spool : spools) {
            String colorString = spool.color();
            if (type == spool.filamentType() && !availableColors.contains(colorString)) {
                System.out.println("- " + counter + ": " + colorString + " (" + spool.filamentType() + ")");
                availableColors.add(colorString);
                counter++;
            }
        }
        System.out.print("- Color number: ");
        int colorChoice = numberInput(1, availableColors.size());
        colors.add(availableColors.get(colorChoice - 1));
        for (int i = 1; i < print.filamentLength().size(); i++) {
            System.out.print("- Color number: ");
            colorChoice = numberInput(1, availableColors.size());
            colors.add(availableColors.get(colorChoice - 1));
        }
        System.out.println("--------------------------------------");

        facade.addPrintTask(printName, colors, type);
        System.out.println("----------------------------");
    }

    private void showPrints() {
        var prints = facade.getPrints();
        System.out.println("---------- Available prints ----------");
        for (PrintDTO p : prints) {
            System.out.println(p);
        }
        System.out.println("--------------------------------------");
    }

    private void showSpools() {
        var spools = facade.getSpools();
        System.out.println("---------- Spools ----------");
        for (SpoolDTO spool : spools) {
            System.out.println(spool);
        }
        System.out.println("----------------------------");
    }

    private void showPrinters() {
        var printers = facade.getPrinters();
        System.out.println("--------- Available printers ---------");
        for (Printer p : printers) {
            String output = p.toString();
            PrintTaskDTO currentTask = facade.getPrinterCurrentTask(p);
            if (currentTask != null) {
                output = output.replace("--------", "- Current Print Task: " + currentTask + System.lineSeparator() +
                        "--------");
            }
            System.out.println(output);
        }
        System.out.println("--------------------------------------");
    }

    private void showPendingPrintTasks() {
        List<PrintTaskDTO> printTasks = facade.getPendingPrintTasks();
        System.out.println("--------- Pending Print Tasks ---------");
        for (PrintTaskDTO p : printTasks) {
            System.out.println(p);
        }
        System.out.println("--------------------------------------");
    }

    public int menuChoice(int max) {
        int choice = -1;
        while (choice < 0 || choice > max) {
            System.out.print("- Choose an option: ");
            try {
                choice = scanner.nextInt();
            } catch (InputMismatchException e) {
                //try again after consuming the current line
                System.out.println("- Error: Invalid input");
                scanner.nextLine();
            }
        }
        return choice;
    }

    public String stringInput() {
        String input = null;
        while (input == null || input.length() == 0) {
            input = scanner.nextLine();
        }
        return input;
    }

    public int numberInput() {
        int input = scanner.nextInt();
        return input;
    }

    public int numberInput(int min, int max) {
        int input = numberInput();
        while (input < min || input > max) {
            input = numberInput();
        }
        return input;
    }

    @Override
    public void updateSpoolChanges(int spoolChanges) {
        this.spoolChanges = spoolChanges;
    }

    @Override
    public void updatePrintsFulfilled(int printsFulfilled) {
        this.printsFulfilled = printsFulfilled;
    }
}
