package nl.saxion.Models.strategies;

import nl.saxion.Models.printers.Printer;

public interface SelectPrintTaskStrategy {
    void selectPrintTask(Printer printer);
    String getStrategyName();
}
