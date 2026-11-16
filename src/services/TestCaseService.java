package services;

import enums.TestStatus;
import models.TestCaseModel;

import java.util.ArrayList;
import java.util.List;

public class TestCaseService {

    private final List<TestCaseModel> testCases = new ArrayList<>();
    private int nextId = 1;

    // ---------- ID generation ----------

    public int generateId() {
        return nextId++;
    }

    // ---------- Add ----------

    public void addTestCase(TestCaseModel testCase) {
        if (testCase == null) {
            throw new IllegalArgumentException("Test case cannot be null.");
        }
        testCases.add(testCase);
        if (testCase.getId() >= nextId) {
            nextId = testCase.getId() + 1;
        }
    }

    // ---------- Load----------

    public void loadTestCases(List<TestCaseModel> loaded) {
        testCases.clear();
        nextId = 1;
        if (loaded == null) {
            return;
        }
        for (TestCaseModel tc : loaded) {
            testCases.add(tc);
            if (tc.getId() >= nextId) {
                nextId = tc.getId() + 1;
            }
        }
    }

    // ---------- Read ----------

    public List<TestCaseModel> getAllTestCases() {
        return new ArrayList<>(testCases);
    }

    public TestCaseModel findById(int id) {
        for (TestCaseModel tc : testCases) {
            if (tc.getId() == id) {
                return tc;
            }
        }
        return null;
    }

    // ---------- Update ----------

    public boolean updateStatus(int id, TestStatus newStatus) {
        TestCaseModel tc = findById(id);
        if (tc == null || newStatus == null) {
            return false;
        }
        tc.setStatus(newStatus);
        return true;
    }

    // ---------- Search ----------

    public List<TestCaseModel> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllTestCases();
        }
        String key = keyword.trim().toLowerCase();
        List<TestCaseModel> results = new ArrayList<>();
        for (TestCaseModel tc : testCases) {
            String title = safeLower(tc.getTitle());
            String description = safeLower(tc.getDescription());
            if (title.contains(key) || description.contains(key)) {
                results.add(tc);
            }
        }
        return results;
    }

    private String safeLower(String text) {
        return text == null ? "" : text.toLowerCase();
    }

    // ---------- Display ----------

    public void viewAllTestCases() {
        displayTestCases(testCases);
    }

    public void displayTestCases(List<TestCaseModel> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("No test cases found.");
            return;
        }

        String format = "%-5s %-28s %-10s %-10s %-30s%n";
        String line = "-".repeat(87);

        System.out.println(line);
        System.out.printf(format, "ID", "Title", "Type", "Status", "Tester / Script Path");
        System.out.println(line);

        for (TestCaseModel tc : list) {
            System.out.printf(format,
                    tc.getId(),
                    truncate(tc.getTitle(), 28),
                    tc.getType(),
                    tc.getStatus(),
                    truncate(tc.getTypeDetail(), 30));
        }
        System.out.println(line);
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}