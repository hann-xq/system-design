package nl.saxion.managers;

import nl.saxion.Models.models.FilamentType;
import nl.saxion.Models.models.Print;
import nl.saxion.Models.models.PrintTask;
import nl.saxion.Models.models.Spool;
import nl.saxion.Models.observer.DashboardObserver;
import nl.saxion.Models.printers.MultiSpooledPrinter;
import nl.saxion.Models.printers.Printer;
import nl.saxion.Models.printers.SingleSpooledPrinter;
import nl.saxion.Models.strategies.SelectPrintTaskStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private final List<PrintTask> pendingPrintTasks = new ArrayList<>();
    private final Map<Printer, PrintTask> runningPrintTasks = new HashMap();
    private final SpoolManager spoolManager;
    private final PrintManager printManager;
    private final PrinterManager printerManager;
    private final List<DashboardObserver> observers = new ArrayList<>();
    private int spoolChanges;
    private int printsFulfilled;
    private SelectPrintTaskStrategy strategy;


    public TaskManager(SpoolManager spoolManager, PrintManager printManager, PrinterManager printerManager) {
        this.spoolManager = spoolManager;
        this.printManager = printManager;
        this.printerManager = printerManager;
        this.spoolChanges = 0;
        this.printsFulfilled = 0;
    }

    public void selectPrintStrategy(SelectPrintTaskStrategy strategy) {
        this.strategy = strategy;
    }

    public String getStrategy() {
        return strategy.getStrategyName();
    }

    public void selectPrintTask(Printer printer) {
        strategy.selectPrintTask(printer);
    }

    public PrintTask findTaskForCurrentSpool(Printer printer) {
        for (PrintTask printTask : pendingPrintTasks) {
            if (printer.printFits(printTask.getPrint())) {
                if (printer.filamentAndMaxColorsMatches(printTask) && printer.spoolsMatchForPrintTask(printTask)) {
                    assignTaskToPrinter(printer, printTask);
                    return printTask;
                }
            }
        }
        return null;
    }

    public PrintTask findTaskForFreeSpools(Printer printer) {
        for (PrintTask printTask : pendingPrintTasks) {
            if (printer.printFits(printTask.getPrint()) && getPrinterCurrentTask(printer) == null) {
                if (printer.filamentAndMaxColorsMatches(printTask)) {
                    if (printer instanceof SingleSpooledPrinter) {
                        Spool chosenSpool = findMatchingSpool(printTask);
                        if (chosenSpool != null) {
                            changeSpoolAndAssignTask((SingleSpooledPrinter) printer, printTask, chosenSpool);
                        }
                    }
                    if (printer instanceof MultiSpooledPrinter) {
                        List<Spool> chosenSpools = findMatchingSpools(printTask);
                        if (chosenSpools.size() == printTask.getColors().size()) {
                            changeSpoolsAndAssignTask((MultiSpooledPrinter) printer, printTask, chosenSpools);
                        }
                    }
                    return printTask;
                }
            }
        }
        return null;
    }

    public Spool findMatchingSpool(PrintTask printTask) {
        for (Spool spool : spoolManager.getFreeSpools()) {
            if (spool.spoolMatch(printTask.getColors().get(0), printTask.getFilamentType())) {
                return spool;
            }
        }
        return null;
    }

    public List<Spool> findMatchingSpools(PrintTask printTask) {
        List<Spool> chosenSpools = new ArrayList<>();
        for (String color : printTask.getColors()) {
            for (Spool spool : spoolManager.getFreeSpools()) {
                if (spool.spoolMatch(color, printTask.getFilamentType()) && !spoolManager.containsSpool(chosenSpools, color)) {
                    chosenSpools.add(spool);
                }
            }
        }
        return chosenSpools;
    }

    public void assignTaskToPrinter(Printer printer, PrintTask printTask) {
        runningPrintTasks.put(printer, printTask);
        printerManager.getFreePrinters().remove(printer);
    }

    public void changeSpoolAndAssignTask(SingleSpooledPrinter printer, PrintTask printTask, Spool chosenSpool) {
        spoolManager.getFreeSpools().add(printer.getCurrentSpool());
        System.out.println("- Spool change: Please place spool " + chosenSpool.getId() + " in printer " + printer.getName());
        spoolManager.getFreeSpools().remove(chosenSpool);
        printer.setCurrentSpool(chosenSpool);
        assignTaskToPrinter(printer, printTask);
    }

    public void changeSpoolsAndAssignTask(MultiSpooledPrinter printer, PrintTask printTask, List<Spool> chosenSpools) {
        spoolManager.getFreeSpools().addAll(printer.getCurrentSpools());
        printer.setCurrentSpools(chosenSpools);
        int position = 1;
        for (Spool spool : chosenSpools) {
            System.out.println("- Spool change: Please place spool " + spool.getId() + " in printer " + printer.getName() + " position " + position++);
            spoolManager.getFreeSpools().remove(spool);
            incrementSpoolChanges();
        }
        assignTaskToPrinter(printer, printTask);
    }

    public void startInitialQueue() {
        for (Printer printer : printerManager.getPrinters()) {
            selectPrintTask(printer);
        }
    }

    public PrintTask getPrinterCurrentTask(Printer printer) {
        if (!runningPrintTasks.containsKey(printer)) {
            return null;
        }
        return runningPrintTasks.get(printer);
    }

    public List<PrintTask> getPendingPrintTasks() {
        return pendingPrintTasks;
    }

    public void addPrintTask(String printName, List<String> colors, FilamentType type) {
        Print print = printManager.findPrint(printName);
        if (print == null) {
            printError("Could not find print with name " + printName);
            return;
        }
        if (colors.size() == 0) {
            printError("Need at least one color, but none given");
            return;
        }
        for (String color : colors) {
            boolean found = false;
            for (Spool spool : spoolManager.getSpools()) {
                if (spool.getColor().equals(color) && spool.getFilamentType() == type) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                printError("Color " + color + " (" + type + ") not found");
                return;
            }
        }

        PrintTask task = new PrintTask(print, colors, type);
        pendingPrintTasks.add(task);
        System.out.println("Added task to queue");
    }

    public void registerPrinterFailure(int printerId) {
        Map.Entry<Printer, PrintTask> foundEntry = findEntryFromPrinterId(printerId);
        assert foundEntry != null;
        PrintTask task = foundEntry.getValue();
        pendingPrintTasks.add(task); // add the task back to the queue.
        runningPrintTasks.remove(foundEntry.getKey());

        System.out.println("Task " + task + " removed from printer "
                + foundEntry.getKey().getName());

        Printer printer = foundEntry.getKey();
        printer.reduceSpoolsByLength(task.getPrint().getFilamentLength());
        selectPrintTask(printer);
    }

    public void registerCompletion(int printerId) {
        Map.Entry<Printer, PrintTask> foundEntry = findEntryFromPrinterId(printerId);
        assert foundEntry != null;
        PrintTask task = foundEntry.getValue();
        runningPrintTasks.remove(foundEntry.getKey());

        System.out.println("Task " + task + " removed from printer "
                + foundEntry.getKey().getName());

        Printer printer = foundEntry.getKey();
        printer.reduceSpoolsByLength(task.getPrint().getFilamentLength());
        selectPrintTask(printer);
        incrementPrintsFulfilled();
    }

    private Map.Entry<Printer, PrintTask> findEntryFromPrinterId(int printerId) {
        for (Map.Entry<Printer, PrintTask> entry : runningPrintTasks.entrySet()) {
            if (entry.getKey().getId() == printerId) {
                return entry;
            }
        }

        printError("cannot find a running task on printer with ID " + printerId);
        return null;
    }

    public void printError(String s) {
        System.out.println("---------- Error Message ----------");
        System.out.println("Error: " + s);
        System.out.println("--------------------------------------");
    }

    public void addObserver(DashboardObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(DashboardObserver observer) {
        observers.remove(observer);
    }

    private void notifySpoolChanges() {
        for (DashboardObserver observer : observers) {
            observer.updateSpoolChanges(spoolChanges);
        }
    }

    private void notifyPrintsFulfilled() {
        for (DashboardObserver observer : observers) {
            observer.updatePrintsFulfilled(printsFulfilled);
        }
    }

    private void incrementSpoolChanges() {
        spoolChanges++;
        notifySpoolChanges();
    }

    private void incrementPrintsFulfilled() {
        printsFulfilled++;
        notifyPrintsFulfilled();
    }
}
