package nl.saxion.Models.printers;

import nl.saxion.Models.models.FilamentType;
import nl.saxion.Models.models.PrintTask;
import nl.saxion.Models.models.Spool;

import java.util.List;

public class SingleSpooledPrinter extends Printer {
    protected Spool currentSpool;

    public SingleSpooledPrinter(PrinterDescription printerDescription, PrintSize printSize, boolean isHoused) {
        super(printerDescription, printSize, isHoused);
    }

    public Spool getCurrentSpool() {
        return currentSpool;
    }

    public void setCurrentSpool(Spool currentSpool) {
        this.currentSpool = currentSpool;
    }

    @Override
    public boolean filamentAndMaxColorsMatches(PrintTask printTask) {
        boolean isABS = printTask.getFilamentType() == FilamentType.ABS;
        boolean maxColorMatches = printTask.getColors().size() == 1;

        if (this.isHoused) {
            return isABS && maxColorMatches;
        }

        return !isABS && maxColorMatches;
    }

    @Override
    public boolean spoolsMatchForPrintTask(PrintTask printTask) {
        return getCurrentSpool().spoolMatch(printTask.getColors().get(0), printTask.getFilamentType());
    }

    @Override
    public void reduceSpoolsByLength(List<Double> filamentLength) {
        this.currentSpool.reduceLength(filamentLength.getFirst());
    }
}
