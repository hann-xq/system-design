package nl.saxion.managers;

import nl.saxion.Models.models.Print;
import nl.saxion.Models.adapters.FileReaderAdapter;
import nl.saxion.Models.adapters.JSONReaderAdapter;
import nl.saxion.Models.adapters.ObjectCollector;

import java.util.ArrayList;
import java.util.List;

public class PrintManager {
    private List<Print> prints = new ArrayList<Print>();

    public void addPrint(String name, int height, int width, int length, ArrayList<Double> filamentLength, int printTime) {
        Print p = new Print(name, height, width, length, filamentLength, printTime);
        prints.add(p);
    }

    public List<Print> getPrints() {
        return prints;
    }

    public Print findPrint(String printName) {
        for (Print p : prints) {
            if (p.getName().equals(printName)) {
                return p;
            }
        }
        return null;
    }

    public Print findPrint(int index) {
        if (index > prints.size() - 1) {
            return null;
        }
        return prints.get(index);
    }

    public void readPrintsFromFile(String filename) {
        if (filename.length() == 0) {
            filename = "prints.json";
        }
        FileReaderAdapter<Print> jsonReader = new JSONReaderAdapter<>(Print.class);
        ObjectCollector<Print> jsonCollector = new ObjectCollector<>();
        jsonReader.read(filename, jsonCollector);
        prints = jsonCollector.getObjects();
    }
}
