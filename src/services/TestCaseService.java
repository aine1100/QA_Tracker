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

        String border = "+------+------------+------------+----------+--------------------+--------------------+--------------------+--------------------+";
        System.out.println(border);
        System.out.printf("| %-4s | %-10s | %-10s | %-8s | %-18s | %-18s | %-18s | %-18s |%n",
                "ID", "Type", "Status", "Title", "Description", "Preconditions", "Expected result", "Tester / Script");
        System.out.println(border);
        for (TestCaseModel tc : list) {
            String[] values = {String.valueOf(tc.getId()), tc.getType().toString(), tc.getStatus().toString(),
                    tc.getTitle(), tc.getDescription(), tc.getPreconditions(),
                    tc.getExpectedResult(), tc.getTypeDetail()};
            String[][] wrapped = new String[values.length][];
            int maxLines = 1;
            for (int i = 0; i < values.length; i++) {
                wrapped[i] = wrap(values[i], i == 0 ? 4 : i == 1 || i == 2 ? 10 : 18);
                maxLines = Math.max(maxLines, wrapped[i].length);
            }
            for (int row = 0; row < maxLines; row++) {
                System.out.printf("| %-4s | %-10s | %-10s | %-8s | %-18s | %-18s | %-18s | %-18s |%n",
                        cell(wrapped[0], row), cell(wrapped[1], row), cell(wrapped[2], row),
                        cell(wrapped[3], row), cell(wrapped[4], row), cell(wrapped[5], row),
                        cell(wrapped[6], row), cell(wrapped[7], row));
            }
            System.out.println(border);
        }
    }

    private String[] wrap(String text, int width) {
        String value = text == null ? "" : text.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ').trim();
        if (value.isEmpty()) return new String[]{""};
        List<String> lines = new ArrayList<>();
        while (value.length() > width) {
            int split = value.lastIndexOf(' ', width);
            if (split <= 0) split = width;
            lines.add(value.substring(0, split));
            value = value.substring(split).trim();
        }
        lines.add(value);
        return lines.toArray(new String[0]);
    }

    private String cell(String[] lines, int row) {
        return row < lines.length ? lines[row] : "";
    }
}
