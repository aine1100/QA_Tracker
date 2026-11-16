package files;

import enums.TestStatus;
import enums.TestType;
import models.AutomatedTestCaseModel;
import models.ManualTestCaseModel;
import models.TestCaseModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class FileStorage {

    private static final String HEADER =
            "TEST CASE ID,TEST TYPE,TEST STATUS,TITLE,DESCRIPTION,PRECONDITIONS,EXPECTED RESULT,TESTER NAME (MANUAL) / SCRIPT PATH (AUTOMATED)";

    private final Path filePath;

    public FileStorage() {
        this(defaultFilePath());
    }

    private static Path defaultFilePath() {
        Path currentDirectory = Paths.get("").toAbsolutePath();
        if (Files.isDirectory(currentDirectory.resolve("src"))) {
            return currentDirectory.resolve(Paths.get("data", "test-cases.csv"));
        }
        Path projectDirectory = currentDirectory.resolve("QA_Tracker");
        if (Files.isDirectory(projectDirectory.resolve("src"))) {
            return projectDirectory.resolve(Paths.get("data", "test-cases.csv"));
        }
        return currentDirectory.resolve(Paths.get("data", "test-cases.csv"));
    }

    public FileStorage(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null.");
        }
        this.filePath = filePath;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public List<TestCaseModel> loadTestCases() throws IOException {
        if (Files.notExists(filePath)) {
            return new ArrayList<>();
        }

        List<TestCaseModel> testCases = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return testCases;
            }

            if (!HEADER.equals(header)) {
                throw new IOException("Unrecognised file header in " + filePath
                        + ". Expected CSV header: " + HEADER);
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                try {
                    testCases.add(parseCsvRow(line));
                } catch (IllegalArgumentException exception) {
                    throw new IOException("Invalid test case record at line " + lineNumber
                            + " in " + filePath, exception);
                }
            }
        }
        return testCases;
    }

    public void saveTestCases(List<TestCaseModel> testCases) throws IOException {
        if (testCases == null) {
            throw new IllegalArgumentException("Test case list cannot be null.");
        }

        Path absolutePath = filePath.toAbsolutePath();
        Path parent = absolutePath.getParent();
        if (parent == null) {
            throw new IOException("Cannot determine a parent directory for: " + filePath);
        }
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "qa-tracker-", ".tmp");

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();
                for (TestCaseModel testCase : testCases) {
                    if (testCase == null || testCase.getType() == null || testCase.getStatus() == null) {
                        throw new IllegalArgumentException(
                                "Test cases, types, and statuses must not be null.");
                    }
                    writeCsvRow(writer, testCase);
                }
            }

            try {
                Files.move(temporaryFile, absolutePath,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, absolutePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    public Path getFilePath() {
        return filePath.toAbsolutePath();
    }

    // -------------------------------------------------------------------------
    // CSV write helpers
    // -------------------------------------------------------------------------

    private void writeCsvRow(BufferedWriter writer, TestCaseModel testCase) throws IOException {
        writer.write(csvField(String.valueOf(testCase.getId())));
        writer.write(',');
        writer.write(csvField(testCase.getType().name()));
        writer.write(',');
        writer.write(csvField(testCase.getStatus().name()));
        writer.write(',');
        writer.write(csvField(testCase.getTitle()));
        writer.write(',');
        writer.write(csvField(testCase.getDescription()));
        writer.write(',');
        writer.write(csvField(testCase.getPreconditions()));
        writer.write(',');
        writer.write(csvField(testCase.getExpectedResult()));
        writer.write(',');
        writer.write(csvField(testCase.getTypeDetail()));
        writer.newLine();
    }

    /**
     * Encodes a single field following RFC 4180:
     * - Null becomes an empty field.
     * - If the value contains a comma, double-quote, CR, or LF it is wrapped in
     *   double-quotes and any internal double-quote is escaped as "".
     */
    private String csvField(String value) {
        if (value == null) {
            return "";
        }
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    // -------------------------------------------------------------------------
    // CSV parse helpers
    // -------------------------------------------------------------------------

    private TestCaseModel parseCsvRow(String line) {
        List<String> fields = splitCsvLine(line);
        if (fields.size() != 8) {
            throw new IllegalArgumentException(
                    "Expected 8 CSV fields but got " + fields.size() + ": " + line);
        }

        int id = Integer.parseInt(fields.get(0).trim());
        TestType type = TestType.valueOf(fields.get(1).trim());
        TestStatus status = TestStatus.valueOf(fields.get(2).trim());
        String title = fields.get(3);
        String description = fields.get(4);
        String preconditions = fields.get(5);
        String expectedResult = fields.get(6);
        String typeDetail = fields.get(7);

        TestCaseModel testCase;
        if (type == TestType.MANUAL) {
            testCase = new ManualTestCaseModel(
                    id, title, description, preconditions, expectedResult, typeDetail);
        } else {
            testCase = new AutomatedTestCaseModel(
                    id, title, description, preconditions, expectedResult, typeDetail);
        }
        testCase.setStatus(status);
        return testCase;
    }

    /**
     * Minimal RFC 4180 CSV line splitter.
     * Handles double-quote-enclosed fields (including embedded commas and escaped
     * quotes written as "") but does not support multi-line fields.
     */
    private List<String> splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int length = line.length();

        for (int i = 0; i < length; i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    // Peek ahead: "" is an escaped quote inside the field.
                    if (i + 1 < length && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++; // skip the second quote
                    } else {
                        inQuotes = false; // closing quote
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString()); // last field (no trailing comma required)
        return fields;
    }
}
