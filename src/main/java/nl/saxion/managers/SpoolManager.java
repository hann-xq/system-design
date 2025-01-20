package nl.saxion.managers;

import nl.saxion.Models.models.Spool;
import nl.saxion.Models.adapters.CSVReaderAdapter;
import nl.saxion.Models.adapters.FileReaderAdapter;
import nl.saxion.Models.adapters.JSONReaderAdapter;
import nl.saxion.Models.adapters.ObjectCollector;
import java.util.ArrayList;
import java.util.List;

public class SpoolManager {
    private List<Spool> spools = new ArrayList<Spool>();
    private List<Spool> freeSpools = new ArrayList<>();

    public void addSpool(Spool spool) {
        spools.add(spool);
        freeSpools.add(spool);
    }

    public Spool getSpoolByID(int id) {
        for (Spool s : spools) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }

    public boolean containsSpool(final List<Spool> list, final String name) {
        return list.stream().anyMatch(o -> o.getColor().equals(name));
    }

    public List<Spool> getSpools() {
        return spools;
    }

    public List<Spool> getFreeSpools() {
        return freeSpools;
    }

    public void readSpoolsFromJson(String filename) {
        if (filename.length() == 0) {
            filename = "spools.csv";
        }
        FileReaderAdapter<Spool> jsonReader = new JSONReaderAdapter<>(Spool.class);
        ObjectCollector<Spool> jsonCollector = new ObjectCollector<>();
        jsonReader.read(filename, jsonCollector);
        spools = jsonCollector.getObjects();
        freeSpools = jsonCollector.getObjects();
    }

    public void readSpoolsFromCSV(String filename) {
        if (filename.length() == 0) {
            filename = "spools.csv";
        }
        FileReaderAdapter<Spool> csvReader = new CSVReaderAdapter<>(Spool.class);
        ObjectCollector<Spool> csvCollector = new ObjectCollector<>();
        csvReader.read(filename, csvCollector);
        spools = csvCollector.getObjects();
        freeSpools = csvCollector.getObjects();
    }
}
