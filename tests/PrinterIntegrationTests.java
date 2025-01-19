import nl.saxion.Models.models.FilamentType;
import nl.saxion.Models.models.Print;
import nl.saxion.Models.models.PrintTask;
import nl.saxion.Models.models.Spool;
import nl.saxion.Models.printers.PrintSize;
import nl.saxion.Models.printers.Printer;
import nl.saxion.Models.printers.PrinterDescription;
import nl.saxion.Models.printers.SingleSpooledPrinter;
import nl.saxion.managers.PrintManager;
import nl.saxion.managers.PrinterManager;
import nl.saxion.managers.SpoolManager;
import nl.saxion.managers.TaskManager;
import nl.saxion.Models.strategies.LessSpoolChangeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class PrinterIntegrationTests {
    private TaskManager taskManager;
    private SpoolManager spoolManager;
    private PrintManager printManager;
    private PrinterManager printerManager;

    @BeforeEach
    void setUp() {
        spoolManager = new SpoolManager();
        printerManager = new PrinterManager();
        printManager = new PrintManager();
        taskManager = new TaskManager(spoolManager, printManager, printerManager);
        setupBasicTestEnvironment();
    }

    private Print createTestPrint(String name) {
        ArrayList<Double> standardFilamentLength = new ArrayList<>(Arrays.asList(10.0));
        return new Print(name, 100, 100, 100, standardFilamentLength, 60);
    }

    private void addTestPrint(String name, int height, int width, int length) {
        ArrayList<Double> filamentLength = new ArrayList<>(Arrays.asList(10.0));
        Print print = new Print(name, height, width, length, filamentLength, 60);
        printManager.addPrint(name, height, width, length, filamentLength, 60);
    }

    private void setupBasicTestEnvironment() {
        // standard printer size used by all printers
        PrintSize standardSize = new PrintSize(200, 200, 200);

        //  set up all printers according to compatibility table
        // Non-housed single spool printers (can't print ABS)
        printerManager.addPrinter(1, new PrinterDescription(1, "Enterprise", "Federation"), standardSize, 1);
        printerManager.addPrinter(1, new PrinterDescription(3, "Red Dwarf", "JMC"), standardSize, 1);
        printerManager.addPrinter(1, new PrinterDescription(4, "Heart of Gold", "Sirius"), standardSize, 1);
        printerManager.addPrinter(1, new PrinterDescription(5, "Tardis", "Gallifrey"), standardSize, 1);

        // Housed printers (can print ABS)
        printerManager.addPrinter(2, new PrinterDescription(2, "Serenity", "Firefly"), standardSize, 1);
        printerManager.addPrinter(2, new PrinterDescription(6, "Rocinante", "MCR"), standardSize, 1);
        printerManager.addPrinter(2, new PrinterDescription(7, "Bebop", "ISSP"), standardSize, 1);

        //  test spools with different materials and colors
        // PLA Spools
        spoolManager.addSpool(new Spool(1, "Blue", FilamentType.PLA, 100.0));
        spoolManager.addSpool(new Spool(2, "Red", FilamentType.PLA, 100.0));
        spoolManager.addSpool(new Spool(3, "Green", FilamentType.PLA, 100.0));
        // PETG Spools
        spoolManager.addSpool(new Spool(4, "Blue", FilamentType.PETG, 100.0));
        spoolManager.addSpool(new Spool(5, "Red", FilamentType.PETG, 100.0));
        // ABS Spools
        spoolManager.addSpool(new Spool(6, "Blue", FilamentType.ABS, 100.0));
        spoolManager.addSpool(new Spool(7, "Red", FilamentType.ABS, 100.0));

        // all prints from compatibility table with their characteristics
        // Universal prints (compatible with most printers)
        addTestPrint("Acoustic Guitar Cooky Cutter", 150, 150, 150);
        addTestPrint("Fucktopus", 120, 120, 120);

        // Medium compatibility prints
        addTestPrint("Stegosaurus Pickholder", 180, 180, 180);
        addTestPrint("Collapsing Jian", 200, 200, 200);

        // Special prints (limited printer compatibility)
        addTestPrint("Earth Globe", 190, 190, 190);  // Only Heart of Gold for PLA/PETG
        addTestPrint("Moon Lamp", 160, 160, 160);    // Only Red Dwarf for PLA/PETG
        addTestPrint("Cathedral", 170, 170, 170);    // Only Heart of Gold for PLA/PETG
        addTestPrint("Lizard", 140, 140, 140);       // Only Heart of Gold for PLA/PETG
        addTestPrint("Tree Frog", 130, 130, 130);    // Only Heart of Gold for PLA/PETG

        // 5. Set up task manager strategy
        taskManager.selectPrintStrategy(new LessSpoolChangeStrategy(taskManager));
    }

    @Test
    void testAddPrintWithMatchingSpool() {
        // Print should be assigned to a printer with matching spool
        SingleSpooledPrinter printer = (SingleSpooledPrinter)printerManager.getPrinters().get(0);
        Spool bluePLA = spoolManager.getSpoolByID(1);
        printer.setCurrentSpool(bluePLA);

        taskManager.addPrintTask("Fucktopus", Arrays.asList("Blue"), FilamentType.PLA);

        assertFalse(taskManager.getPendingPrintTasks().isEmpty(),
                "Print task should be added to pending tasks");

        taskManager.startInitialQueue();

        assertTrue(taskManager.getPendingPrintTasks().isEmpty(),
                "Print task should be assigned from pending tasks");
        assertNotNull(taskManager.getPrinterCurrentTask(printer),
                "Printer should have an assigned task");
    }

    @Test
    void testAddPrintRequiringSpoolChange() {
        SingleSpooledPrinter printer = (SingleSpooledPrinter)printerManager.getPrinters().get(0);
        Spool redPLA = spoolManager.getSpoolByID(2);
        printer.setCurrentSpool(redPLA);

        taskManager.addPrintTask("Fucktopus", Arrays.asList("Blue"), FilamentType.PLA);
        taskManager.startInitialQueue();

        assertEquals("Blue", printer.getCurrentSpool().getColor(),
                "Printer should have changed to blue spool");
        assertNotNull(taskManager.getPrinterCurrentTask(printer),
                "Printer should have an assigned task");
    }

    @Test
    void testAddPrintWithNoAvailablePrinter() {
        for (Printer printer : printerManager.getPrinters()) {
            ArrayList<Double> filamentLengths = new ArrayList<>(Arrays.asList(10.0));
            Print busyPrint = new Print("BusyPrint", 100, 100, 100, filamentLengths, 60);
            PrintTask busyTask = new PrintTask(busyPrint, Arrays.asList("Blue"), FilamentType.PLA);
            taskManager.assignTaskToPrinter(printer, busyTask);
        }

        taskManager.addPrintTask("Fucktopus", Arrays.asList("Blue"), FilamentType.PLA);

        assertFalse(taskManager.getPendingPrintTasks().isEmpty(),
                "Print should be queued when no printers available");
    }

    @Test
    void testPrinterReadySelectsMatchingSpool() {
        SingleSpooledPrinter printer = (SingleSpooledPrinter)printerManager.getPrinters().get(0);
        Spool bluePLA = spoolManager.getSpoolByID(1);
        printer.setCurrentSpool(bluePLA);

        taskManager.addPrintTask("Fucktopus", Arrays.asList("Red"), FilamentType.PLA);
        taskManager.addPrintTask("Stegosaurus", Arrays.asList("Blue"), FilamentType.PLA);

        taskManager.startInitialQueue();

        PrintTask selectedTask = taskManager.getPrinterCurrentTask(printer);
        assertNotNull(selectedTask);
        assertEquals("Blue", selectedTask.getColors().get(0),
                "Task with matching spool color should be selected");
    }

    @Test
    void testMultipleColorPrintSelection() {
        SingleSpooledPrinter printer = (SingleSpooledPrinter)printerManager.getPrinters().get(0);
        Spool bluePLA = spoolManager.getSpoolByID(1);
        printer.setCurrentSpool(bluePLA);

        taskManager.addPrintTask("Fucktopus", Arrays.asList("Blue"), FilamentType.PLA);
        taskManager.addPrintTask("Stegosaurus", Arrays.asList("Red"), FilamentType.PLA);
        taskManager.addPrintTask("Fucktopus", Arrays.asList("Blue"), FilamentType.PLA);

        taskManager.startInitialQueue();
        taskManager.registerCompletion(printer.getId());

        PrintTask selectedTask = taskManager.getPrinterCurrentTask(printer);
        assertNotNull(selectedTask);
        assertEquals("Blue", selectedTask.getColors().get(0),
                "Second blue task should be selected after printer becomes ready");
    }

    @Test
    void testQueueStartFillsAllPrinters() {
        taskManager.addPrintTask("Fucktopus", Arrays.asList("Blue"), FilamentType.PLA);
        taskManager.addPrintTask("Stegosaurus", Arrays.asList("Red"), FilamentType.PLA);

        taskManager.startInitialQueue();

        int assignedPrinters = 0;
        for (Printer printer : printerManager.getPrinters()) {
            if (taskManager.getPrinterCurrentTask(printer) != null) {
                assignedPrinters++;
            }
        }

        assertTrue(assignedPrinters > 0,
                "Queue start should assign tasks to available printers");
    }

    @ParameterizedTest
    @CsvSource({
            "Enterprise, Acoustic Guitar Cooky Cutter, PLA, true",
            "Enterprise, Earth Globe, PLA, false",
            "Enterprise, Moon Lamp, PLA, false",
            "Heart of Gold, Cathedral, PLA, true",
            "Heart of Gold, Fucktopus, PLA, true",
            "Bebop, Stegosaurus Pickholder, PLA, false",
            "Tardis, Collapsing Jian, PLA, false"
    })
    void testPLAPETGCompatibility(String printerName, String printName, String material, boolean shouldBeCompatible) {
        Printer printer = null;
        for (Printer p : printerManager.getPrinters()) {
            if (p.getName().equals(printerName)) {
                printer = p;
                break;
            }
        }
        assertNotNull(printer, "Printer should exist");

        Print print = createTestPrint(printName);
        PrintTask task = new PrintTask(print, Arrays.asList("Blue"), FilamentType.PLA);

        assertEquals(shouldBeCompatible, printer.filamentAndMaxColorsMatches(task),
                String.format("%s should%s be compatible with %s for PLA",
                        printerName, shouldBeCompatible ? "" : " not", printName));
    }

    @ParameterizedTest
    @CsvSource({
            "Serenity, Acoustic Guitar Cooky Cutter, ABS, true",
            "Serenity, Fucktopus, ABS, true",
            "Enterprise, Acoustic Guitar Cooky Cutter, ABS, false",
            "Enterprise, Fucktopus, ABS, false",
            "Rocinante, Acoustic Guitar Cooky Cutter, ABS, true",
            "Rocinante, Stegosaurus Pickholder, ABS, true",
            "Bebop, Fucktopus, ABS, true",
            "Bebop, Stegosaurus Pickholder, ABS, false"
    })
    void testABSCompatibility(String printerName, String printName, String material, boolean shouldBeCompatible) {
        Printer printer = null;
        for (Printer p : printerManager.getPrinters()) {
            if (p.getName().equals(printerName)) {
                printer = p;
                break;
            }
        }
        assertNotNull(printer, "Printer should exist");

        Print print = createTestPrint(printName);
        PrintTask task = new PrintTask(print, Arrays.asList("Blue"), FilamentType.ABS);

        assertEquals(shouldBeCompatible, printer.filamentAndMaxColorsMatches(task),
                String.format("%s should%s be compatible with %s for ABS",
                        printerName, shouldBeCompatible ? "" : " not", printName));
    }

    @Test
    void testHeartOfGoldSpecialCases() {
        Printer heartOfGold = null;
        for (Printer p : printerManager.getPrinters()) {
            if (p.getName().equals("Heart of Gold")) {
                heartOfGold = p;
                break;
            }
        }
        assertNotNull(heartOfGold, "Heart of Gold printer should exist");

        Print earthGlobe = createTestPrint("Earth Globe");
        Print cathedral = createTestPrint("Cathedral");
        Print lizard = createTestPrint("Lizard");

        PrintTask earthGlobeTask = new PrintTask(earthGlobe, Arrays.asList("Blue"), FilamentType.PLA);
        PrintTask cathedralTask = new PrintTask(cathedral, Arrays.asList("Blue"), FilamentType.PLA);
        PrintTask lizardTask = new PrintTask(lizard, Arrays.asList("Blue"), FilamentType.PLA);

        assertTrue(heartOfGold.filamentAndMaxColorsMatches(earthGlobeTask),
                "Heart of Gold should print Earth Globe in PLA");
        assertTrue(heartOfGold.filamentAndMaxColorsMatches(cathedralTask),
                "Heart of Gold should print Cathedral in PLA");
        assertTrue(heartOfGold.filamentAndMaxColorsMatches(lizardTask),
                "Heart of Gold should print Lizard in PLA");
    }

    @Test
    void testRedDwarfSpecialCases() {
        Printer redDwarf = null;
        for (Printer p : printerManager.getPrinters()) {
            if (p.getName().equals("Red Dwarf")) {
                redDwarf = p;
                break;
            }
        }
        assertNotNull(redDwarf, "Red Dwarf printer should exist");

        // Create the Moon Lamp print and task
        Print moonLamp = createTestPrint("Moon Lamp");
        PrintTask moonLampTask = new PrintTask(moonLamp, Arrays.asList("Blue"), FilamentType.PLA);

        assertTrue(redDwarf.filamentAndMaxColorsMatches(moonLampTask),
                "Red Dwarf should print Moon Lamp in PLA");

        // Test that other printers can't print Moon Lamp
        for (Printer printer : printerManager.getPrinters()) {
            if (!printer.getName().equals("Red Dwarf")) {
                assertFalse(printer.filamentAndMaxColorsMatches(moonLampTask),
                        printer.getName() + " should not print Moon Lamp in PLA");
            }
        }
    }
        @Test
        void testMultiPrinterConsistency() {
            // Test that all printers agree on Fucktopus
            Print fucktopus = createTestPrint("Fucktopus");
            PrintTask plaTask = new PrintTask(fucktopus, Arrays.asList("Blue"), FilamentType.PLA);
            PrintTask absTask = new PrintTask(fucktopus, Arrays.asList("Blue"), FilamentType.ABS);

            // PLA/PETG: All printers should be able to print Fucktopus
            // ABS: Only Serenity, Rocinante, and Bebop should print Fucktopus
            for (Printer printer : printerManager.getPrinters()) {
                if (printer.getName().equals("Serenity") ||
                        printer.getName().equals("Rocinante") ||
                        printer.getName().equals("Bebop")) {
                    assertTrue(printer.filamentAndMaxColorsMatches(absTask),
                            printer.getName() + " should print Fucktopus in ABS");
                } else {
                    assertFalse(printer.filamentAndMaxColorsMatches(absTask),
                            printer.getName() + " should not print Fucktopus in ABS");
                }

                assertTrue(printer.filamentAndMaxColorsMatches(plaTask),
                        printer.getName() + " should print Fucktopus in PLA");
            }
        }
    }
