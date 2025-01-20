package nl.saxion.Models.printers;

public class PrinterDescription {
    private int id;
    private String name;
    private String manufacturer;

    public PrinterDescription(int id, String name, String manufacturer) {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getManufacturer() {
        return manufacturer;
    }
}
