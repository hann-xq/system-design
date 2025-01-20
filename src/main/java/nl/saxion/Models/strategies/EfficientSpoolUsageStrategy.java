package nl.saxion.Models.strategies;

import nl.saxion.Models.models.PrintTask;
import nl.saxion.Models.models.Spool;
import nl.saxion.Models.printers.MultiSpooledPrinter;
import nl.saxion.Models.printers.Printer;
import nl.saxion.Models.printers.SingleSpooledPrinter;
import nl.saxion.managers.SpoolManager;
import nl.saxion.managers.TaskManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EfficientSpoolUsageStrategy implements SelectPrintTaskStrategy {
    private SpoolManager spoolManager;
    private TaskManager taskManager;

    public EfficientSpoolUsageStrategy(TaskManager taskManager, SpoolManager spoolManager) {
        this.taskManager = taskManager;
        this.spoolManager = spoolManager;
    }

    @Override
    public void selectPrintTask(Printer printer) {
        List<PrintTask> pendingTasks = taskManager.getPendingPrintTasks();

        PrintTask chosenTask = null;
        for (PrintTask task : pendingTasks) {
            List<Spool> suitableSpools = new ArrayList<>();
            if (printer instanceof SingleSpooledPrinter) {
                findMatchingSpool(task, suitableSpools);
                taskManager.changeSpoolAndAssignTask((SingleSpooledPrinter) printer, task, suitableSpools.getFirst());
            }
            if (printer instanceof MultiSpooledPrinter) {
                findMatchingSpools(task, suitableSpools);
                taskManager.changeSpoolsAndAssignTask((MultiSpooledPrinter) printer, task, suitableSpools);
            }
            chosenTask = task;
        }
        taskManager.getPendingPrintTasks().remove(chosenTask);
        System.out.println("- Started task: " + chosenTask + " on printer " + printer.getName());
    }

    public void findMatchingSpool(PrintTask printTask, List<Spool> suitableSpools) {
        for (Spool spool : spoolManager.getSpools()) {
            if (spool.spoolMatch(printTask.getColors().get(0), printTask.getFilamentType())) {
                suitableSpools.add(spool);
            }
        }
        orderSpools(suitableSpools);
    }

    public void findMatchingSpools(PrintTask printTask, List<Spool> suitableSpools) {
        for (String color : printTask.getColors()) {
            Spool suitedSpool = null;
            for (Spool spool : spoolManager.getFreeSpools()) {
                if (spool.spoolMatch(color, printTask.getFilamentType())) {
                    if (suitedSpool == null) {
                        suitedSpool = spool;
                    } else {
                        if (suitedSpool.getLength() > spool.getLength()) {
                            suitedSpool = spool;
                        }
                    }
                }
            }
            suitableSpools.add(suitedSpool);
        }
    }

    private void orderSpools(List<Spool> list) {
        list.sort(Comparator.comparingDouble(Spool::getLength));
    }


    @Override
    public String getStrategyName() {
        return "Efficient Spool Usage";
    }
}
