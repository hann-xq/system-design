package nl.saxion.Models.strategies;

import nl.saxion.Models.models.PrintTask;
import nl.saxion.Models.printers.Printer;
import nl.saxion.managers.TaskManager;


public class LessSpoolChangeStrategy implements SelectPrintTaskStrategy {
    private TaskManager taskManager;

    public LessSpoolChangeStrategy(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void selectPrintTask(Printer printer) {
        PrintTask chosenTask = taskManager.findTaskForCurrentSpool(printer);

        if (chosenTask == null) {
            chosenTask = taskManager.findTaskForFreeSpools(printer);
        }

        if (chosenTask != null) {
            taskManager.getPendingPrintTasks().remove(chosenTask);
            System.out.println("- Started task: " + chosenTask + " on printer " + printer.getName());
        }
    }

    @Override
    public String getStrategyName() {
        return "Less Spool Changes";
    }

}
