package nl.saxion;

import nl.saxion.Models.DTOs.PrintDTO;
import nl.saxion.Models.DTOs.PrintTaskDTO;
import nl.saxion.Models.DTOs.PrinterDTO;
import nl.saxion.Models.DTOs.SpoolDTO;
import nl.saxion.Models.models.FilamentType;
import nl.saxion.Models.models.Print;
import nl.saxion.Models.models.PrintTask;
import nl.saxion.Models.models.Spool;
import nl.saxion.Models.observer.DashboardObserver;
import nl.saxion.Models.printers.Printer;
import nl.saxion.Models.strategies.EfficientSpoolUsageStrategy;
import nl.saxion.Models.strategies.LessSpoolChangeStrategy;
import nl.saxion.managers.PrintManager;
import nl.saxion.managers.PrinterManager;
import nl.saxion.managers.SpoolManager;
import nl.saxion.managers.TaskManager;

import java.util.ArrayList;
import java.util.List;

public class PrintFacade {
    private PrinterManager printerManager;
    private PrintManager printManager;
    private TaskManager taskManager;
    private SpoolManager spoolManager;


    public PrintFacade() {
        printerManager = new PrinterManager();
        printManager = new PrintManager();
        spoolManager = new SpoolManager();
        taskManager = new TaskManager(spoolManager, printManager, printerManager);
    }

    public void startPrintQueue() {
        System.out.println("---------- Starting Print Queue ----------");
        taskManager.startInitialQueue();
        System.out.println("-----------------------------------");
    }

    public List<PrinterDTO> getPrinters() {
        List<PrinterDTO> list = new ArrayList<>();
        for (Printer printer : printerManager.getPrinters()) {
            list.add(printer.toDTO());
        }
        return list;
    }

    public PrintTaskDTO getPrinterCurrentTask(PrinterDTO printer) {
        Printer p = printerManager.getPrinter(printer.id());
        return taskManager.getPrinterCurrentTask(p).toDTO();
    }

    public List<PrintDTO> getPrints() {
        List<PrintDTO> list = new ArrayList<>();
        for (Print print : printManager.getPrints()) {
            list.add(print.toDTO());
        }
        return list;
    }

    public List<SpoolDTO> getSpools() {
        List<SpoolDTO> list = new ArrayList<>();
        for (Spool spool : spoolManager.getSpools()) {
            list.add(spool.toDTO());
        }
        return list;
    }

    public PrintDTO findPrint(int index) {
        return printManager.findPrint(index).toDTO();
    }

    public void addPrintTask(String printName, List<String> colors, FilamentType type) {
        taskManager.addPrintTask(printName, colors, type);
    }

    public List<PrintTaskDTO> getPendingPrintTasks() {
        List<PrintTaskDTO> list = new ArrayList<>();
        for (PrintTask printTask : taskManager.getPendingPrintTasks()) {
            list.add(printTask.toDTO());
        }
        return list;
    }

    public void readPrintsFromFile(String filename) {
        printManager.readPrintsFromFile(filename);
    }

    public void readSpoolsFromFile(String filename) {
        spoolManager.readSpoolsFromCSV(filename);
    }

    public void readPrintersFromFile(String filename) {
        printerManager.readPrintersFromFile(filename);
    }

    public void registerPrinterFailure(int printerId) {
        taskManager.registerPrinterFailure(printerId);
    }

    public void registerCompletion(int printerId) {
        taskManager.registerCompletion(printerId);
    }

    public void selectPrintStrategy(int strategy) {
        if ((strategy == 1)) {
            taskManager.selectPrintStrategy(new LessSpoolChangeStrategy(this.taskManager));
        } else {
            taskManager.selectPrintStrategy(new EfficientSpoolUsageStrategy(this.taskManager, this.spoolManager));
        }
    }

    public String getStrategy() {
        return taskManager.getStrategy();
    }

    public void addObserver(DashboardObserver observer) {
        taskManager.addObserver(observer);
    }

    public void removeObserver(DashboardObserver observer) {
        taskManager.removeObserver(observer);
    }
}
