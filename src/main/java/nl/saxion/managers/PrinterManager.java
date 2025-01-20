package nl.saxion.managers;

import nl.saxion.Models.PrinterFactory;
import nl.saxion.Models.models.PrintTask;
import nl.saxion.Models.printers.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrinterManager {
    private List<Printer> printers = new ArrayList<Printer>();
    private List<Printer> freePrinters = new ArrayList<>();
    private Map<Printer, PrintTask> runningPrintTasks = new HashMap();

    public void addPrinter(int printerType, PrinterDescription printerDescription, PrintSize printSize, int maxColors) {
        PrinterFactory factory = new PrinterFactory();
        Printer printer = factory.createPrinter(printerType, printerDescription, printSize, maxColors);
        printers.add(printer);
        freePrinters.add(printer);
    }

    public void readPrintersFromFile(String filename) {
        JSONParser jsonParser = new JSONParser();
        if (filename.length() == 0) {
            filename = "printers.json";
        }
        URL printersResource = getClass().getResource("/" + filename);
        if (printersResource == null) {
            System.err.println("Warning: Could not find printers.json file");
            return;
        }
        try (FileReader reader = new FileReader(URLDecoder.decode(printersResource.getPath(), StandardCharsets.UTF_8))) {
            JSONArray printers = (JSONArray) jsonParser.parse(reader);
            for (Object p : printers) {
                JSONObject printer = (JSONObject) p;
                int id = ((Long) printer.get("id")).intValue();
                int type = ((Long) printer.get("type")).intValue();
                String name = (String) printer.get("name");
                String manufacturer = (String) printer.get("manufacturer");
                int maxX = ((Long) printer.get("maxX")).intValue();
                int maxY = ((Long) printer.get("maxY")).intValue();
                int maxZ = ((Long) printer.get("maxZ")).intValue();
                int maxColors = ((Long) printer.get("maxColors")).intValue();
                PrintSize printSize = new PrintSize(maxX, maxY, maxZ);
                PrinterDescription printerDescription = new PrinterDescription(id, name, manufacturer);
                addPrinter(type, printerDescription, printSize, maxColors);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public List<Printer> getPrinters() {
        return printers;
    }

    public List<Printer> getFreePrinters() {
        return freePrinters;
    }

    public PrintTask getPrinterCurrentTask(Printer printer) {
        if (!runningPrintTasks.containsKey(printer)) {
            return null;
        }
        return runningPrintTasks.get(printer);
    }

}
