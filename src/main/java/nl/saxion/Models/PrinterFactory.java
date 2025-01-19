package nl.saxion.Models;

import nl.saxion.Models.printers.*;

public class PrinterFactory {
    public Printer createPrinter(int printerType, PrinterDescription printerDescription, PrintSize printSize, int maxColors) {
        return switch (printerType) {
            case 1 -> new SingleSpooledPrinter(printerDescription, printSize, false);
            case 2 -> new SingleSpooledPrinter(printerDescription, printSize, true);
            case 3 -> new MultiColorPrinter(printerDescription, printSize, false, maxColors);
            case 4 -> new MultiColorPrinter(printerDescription, printSize, true, maxColors);
            default -> null;
        };
    }
}
