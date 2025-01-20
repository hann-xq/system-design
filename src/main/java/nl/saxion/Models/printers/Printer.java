package nl.saxion.Models.printers;

import nl.saxion.Models.models.Print;
import nl.saxion.Models.models.PrintTask;

import java.util.List;

public abstract class Printer {
    private PrinterDescription printerDescription;
    private PrintSize printSize;

    protected boolean isHoused;

    public Printer(PrinterDescription printerDescription, PrintSize printSize, boolean isHoused) {
        this.printerDescription = printerDescription;
        this.printSize = printSize;
        this.isHoused = isHoused;
    }

    public int getId() {
        return this.printerDescription.getId();
    }

    public String getName(){
        return this.printerDescription.getName();
    }

    public boolean printFits(Print print) {
        return print.getHeight() <= this.printSize.getMaxZ() && print.getWidth() <= this.printSize.getMaxX() && print.getLength() <= this.printSize.getMaxY();
    }

    public abstract boolean filamentAndMaxColorsMatches(PrintTask printTask);

    public abstract boolean spoolsMatchForPrintTask(PrintTask printTask);

    public abstract void reduceSpoolsByLength(List<Double> filamentLength);

    @Override
    public String toString() {
        return  "--------" + System.lineSeparator() +
                "- ID: " + getId() + System.lineSeparator() +
                "- Name: " + getName() + System.lineSeparator() +
                "- Manufacturer: " + this.printerDescription.getManufacturer() + System.lineSeparator() +
                "--------";
    }
}
