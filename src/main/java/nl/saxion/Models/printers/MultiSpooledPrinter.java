package nl.saxion.Models.printers;

import nl.saxion.Models.models.FilamentType;
import nl.saxion.Models.models.PrintTask;
import nl.saxion.Models.models.Spool;

import java.util.List;

public class MultiSpooledPrinter extends Printer {
    private List<Spool> spools;

    public MultiSpooledPrinter(PrinterDescription printerDescription, PrintSize printSize, boolean isHoused) {
        super(printerDescription, printSize, isHoused);
    }

    @Override
    public boolean filamentAndMaxColorsMatches(PrintTask printTask) {
        boolean isABS = printTask.getFilamentType() == FilamentType.ABS;
        boolean maxSpoolsMatches = printTask.getColors().size() <= this.spools.size();

        if (this.isHoused) {
            return isABS && maxSpoolsMatches;
        }

        return !isABS && maxSpoolsMatches;
    }

    @Override
    public boolean spoolsMatchForPrintTask(PrintTask printTask) {
        boolean printWorks = true;

        for (int i = 0; i < spools.size() && i < printTask.getColors().size(); i++) {
            if (!spools.get(i).spoolMatch(printTask.getColors().get(i), printTask.getFilamentType())) {
                printWorks = false;
            }
        }
        return printWorks;
    }

    @Override
    public void reduceSpoolsByLength(List<Double> filamentLength) {
        for (int i = 0; i < this.spools.size(); i++) {
            Spool spool = this.spools.get(i);
            spool.reduceLength(filamentLength.get(i));
        }
    }

    public List<Spool> getCurrentSpools() {
        return this.spools;
    }

    public void setCurrentSpools(List<Spool> spools) {
        this.spools = spools;
    }
}
