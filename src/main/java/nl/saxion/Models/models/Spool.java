package nl.saxion.Models.models;

import com.opencsv.bean.CsvBindByName;
import nl.saxion.Models.DTOs.SpoolDTO;

public class Spool {
    @CsvBindByName(column = "id")
    private final int id;
    @CsvBindByName(column = "color")
    private final String color;
    @CsvBindByName(column = "filamentType")
    private final FilamentType filamentType;
    @CsvBindByName(column = "length")
    private double length;

    public Spool(int id, String color, FilamentType filamentType, double length) {
        this.id = id;
        this.color = color;
        this.filamentType = filamentType;
        this.length = length;
    }

    public int getId() {
        return this.id;
    }

    public double getLength() {
        return length;
    }

    public boolean spoolMatch(String color, FilamentType type) {
        if(color.equals(this.color) && type == this.getFilamentType()) {
            return true;
        }
        return false;
    }
    /**
     * This method will try to reduce the length of the spool.
     *
     * @param byLength
     * @return boolean which tells you if it is possible or not.
     */
    public boolean reduceLength(double byLength) {
        boolean success = true;
        this.length -= byLength;
        if (this.length < 0) {
            this.length -= byLength;
            success = false;
        }
        return success;
    }

    public String getColor() {
        return color;
    }

    public FilamentType getFilamentType(){
        return filamentType;
    }

    public SpoolDTO toDTO() {
        return new SpoolDTO(id, color, filamentType, length);
    }

    @Override
    public String toString() {
        return  "--------" + System.lineSeparator() +
                "- id: " + id + System.lineSeparator() +
                "- color: " + color + System.lineSeparator() +
                "- filamentType: " + filamentType + System.lineSeparator() +
                "- length: " + length + System.lineSeparator() +
                "--------";
    }
}
