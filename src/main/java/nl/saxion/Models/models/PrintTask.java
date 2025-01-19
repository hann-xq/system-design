package nl.saxion.Models.models;

import nl.saxion.Models.DTOs.PrintDTO;
import nl.saxion.Models.DTOs.PrintTaskDTO;

import java.util.List;

public class PrintTask {
    private Print print;
    private List<String> colors;
    private FilamentType filamentType;


    public PrintTask(Print print, List<String> colors, FilamentType filamentType){
        this.print = print;
        this.colors = colors;
        this.filamentType = filamentType;

    }

    public List<String> getColors() {
        return colors;
    }

    public FilamentType getFilamentType() {
        return filamentType;
    }

    public Print getPrint(){
        return print;
    }

    public PrintTaskDTO toDTO(){
        PrintDTO printDTO = this.getPrint().toDTO();
        return new PrintTaskDTO(printDTO, this.colors, this.filamentType);
    }

    @Override
    public String toString() {
        return "< " + print.getName() +" " + filamentType + " " + colors.toString() + " >";
    }
}
