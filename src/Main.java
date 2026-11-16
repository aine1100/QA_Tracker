import enums.TestStatus;
import enums.TestType;
import files.FileStorage;
import models.AutomatedTestCaseModel;
import models.ManualTestCaseModel;
import models.TestCaseModel;
import services.ReportGeneratorService;
import services.TestCaseService;
import validations.InputValidator;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/** Console entry point for QA Tracker. */
public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final InputValidator validator = new InputValidator(scanner);
    private final TestCaseService testCaseService = new TestCaseService();
    private final ReportGeneratorService reportGenerator = new ReportGeneratorService();
    private final FileStorage fileStorage = new FileStorage();

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        loadSavedCases();
        boolean running = true;
        while (running) {
            showMenu();
            int choice = validator.readMenuChoice(1, 6);
            switch (choice) {
                case 1:
                    addTestCase();
                    break;
                case 2:
                    testCaseService.viewAllTestCases();
                    break;
                case 3:
                    updateStatus();
                    break;
                case 4:
                    searchTestCases();
                    break;
                case 5:
                    reportGenerator.generateReport(testCaseService.getAllTestCases());
                    break;
                case 6:
                    saveAndNotify();
                    running = false;
                    System.out.println("Goodbye.");
                    break;
                default:
                    throw new IllegalStateException("Unexpected menu choice.");
            }
            if (running) System.out.println();
        }
        scanner.close();
    }

    private void showMenu() {
        System.out.println("================================");
        System.out.println("          QA TRACKER v1.0");
        System.out.println("================================");
        System.out.println("1. Add a test case");
        System.out.println("2. View all test cases");
        System.out.println("3. Update test status");
        System.out.println("4. Search test cases");
        System.out.println("5. Generate execution report");
        System.out.println("6. Save & Exit");
    }

    private void addTestCase() {
        TestType type = validator.readTestType();
        String title = validator.readRequired("Title (required): ");
        String description = validator.readOptional("Description (optional): ");
        String preconditions = validator.readOptional(
                "Preconditions (optional; separate multiple items with commas): ");
        String expectedResult = validator.readRequired(
                "Expected result (required; separate multiple items with commas): ");

        int id = testCaseService.generateId();
        TestCaseModel testCase;
        if (type == TestType.MANUAL) {
            String testerName = validator.readRequired("Tester name (required for manual tests): ");
            testCase = new ManualTestCaseModel(id, title, description, preconditions,
                    expectedResult, testerName);
        } else {
            String scriptPath = validator.readRequired("Automation script path (required for automated tests): ");
            testCase = new AutomatedTestCaseModel(id, title, description, preconditions,
                    expectedResult, scriptPath);
        }
        testCaseService.addTestCase(testCase);
        System.out.println("Added test case with ID " + id + ".");
        saveAndNotify();
    }

    private void updateStatus() {
        int id = validator.readPositiveId();
        TestCaseModel testCase = testCaseService.findById(id);
        if (testCase == null) {
            System.out.println("No test case exists with ID " + id + ".");
            return;
        }
        System.out.println("Current status: " + testCase.getStatus());
        TestStatus status = validator.readTestStatus();
        testCaseService.updateStatus(id, status);
        System.out.println("Updated test case " + id + " to " + status + ".");
        saveAndNotify();
    }

    private void searchTestCases() {
        System.out.print("Search title or description (leave empty to show all): ");
        String keyword = scanner.nextLine();
        List<TestCaseModel> matches = testCaseService.searchByKeyword(keyword);
        testCaseService.displayTestCases(matches);
    }

    private void loadSavedCases() {
        try {
            testCaseService.loadTestCases(fileStorage.loadTestCases());
            System.out.println("Loaded " + testCaseService.getAllTestCases().size()
                    + " saved test case(s).");
        } catch (IOException | RuntimeException exception) {
            System.out.println("Could not load saved test cases: " + exception.getMessage());
            System.out.println("Starting with an empty in-memory list.");
        }
    }

    private void saveAndNotify() {
        try {
            fileStorage.saveTestCases(testCaseService.getAllTestCases());
            System.out.println("Test cases saved to: " + fileStorage.getFilePath());
        } catch (IOException | RuntimeException exception) {
            System.out.println("Could not save test cases: " + exception.getMessage());
        }
    }

}
