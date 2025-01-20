package nl.saxion.Models.printers;

public class PrintSize {
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    public PrintSize(int maxX, int maxY, int maxZ) {
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }
}
