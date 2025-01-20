package nl.saxion.Models.printers;

public class MultiColorPrinter extends MultiSpooledPrinter {
    private final int maxColors;

    public MultiColorPrinter(PrinterDescription printerDescription, PrintSize printSize, boolean isHoused, int maxColors) {
        super(printerDescription, printSize, isHoused);
        this.maxColors = maxColors;
    }

    public int getMaxColors() {
        return maxColors;
    }
}
