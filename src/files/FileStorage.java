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
import java.util.Base64;
import java.util.List;

public class FileStorage {

    private static final String HEADER = "ID\tTYPE\tSTATUS\tTITLE\tDESCRIPTION\tPRECONDITIONS\tEXPECTED RESULT\tTESTER / SCRIPT PATH";
    private static final String STEPS_TABLE_HEADER = "ID\tTYPE\tSTATUS\tTITLE\tDESCRIPTION\tPRECONDITIONS\tTEST STEPS\tEXPECTED RESULT\tTESTER / SCRIPT PATH";
    private static final String OLD_TABLE_HEADER = "TEST CASE ID\tTEST TYPE\tTEST STATUS\tTITLE\tDESCRIPTION\tPRECONDITIONS\tEXPECTED RESULT\tTESTER NAME (MANUAL) / SCRIPT PATH (AUTOMATED)";
    private static final String LEGACY_HEADER = "QA_TRACKER\t1";
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final Path filePath;

    public FileStorage() {
        this(defaultFilePath());
    }

    private static Path defaultFilePath() {
        Path currentDirectory = Paths.get("").toAbsolutePath();
        if (Files.isDirectory(currentDirectory.resolve("src"))) {
            return currentDirectory.resolve(Paths.get("data", "test-cases.txt"));
        }
        Path projectDirectory = currentDirectory.resolve("QA_Tracker");
        if (Files.isDirectory(projectDirectory.resolve("src"))) {
            return projectDirectory.resolve(Paths.get("data", "test-cases.txt"));
        }
        return currentDirectory.resolve(Paths.get("data", "test-cases.txt"));
    }

    public FileStorage(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null.");
        }
        this.filePath = filePath;
    }

    public List<TestCaseModel> loadTestCases() throws IOException {
        if (Files.notExists(filePath)) {
            return new ArrayList<>();
        }

        List<TestCaseModel> testCases = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (HEADER.equals(header) || OLD_TABLE_HEADER.equals(header) || STEPS_TABLE_HEADER.equals(header)) {
                boolean hasSteps = STEPS_TABLE_HEADER.equals(header);
                String line;
                int lineNumber = 1;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    if (line.isEmpty()) continue;
                    try {
                        testCases.add(parseTableRow(line, hasSteps));
                    } catch (IllegalArgumentException exception) {
                        throw new IOException("Invalid test case record at line " + lineNumber
                                + " in " + filePath, exception);
                    }
                }
            } else if (LEGACY_HEADER.equals(header)) {
                String line;
                int lineNumber = 1;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    if (line.isEmpty()) continue;
                    try {
                        testCases.add(parseLegacyTestCase(line));
                    } catch (IllegalArgumentException exception) {
                        throw new IOException("Invalid test case record at line " + lineNumber
                                + " in " + filePath, exception);
                    }
                }
            } else {
                // A readable, labeled text format is also accepted for easy editing by hand.
                List<String> lines = new ArrayList<>();
                if (header != null) lines.add(header);
                String line;
                while ((line = reader.readLine()) != null) lines.add(line);
                testCases.addAll(parseReadableRecords(lines));
            }
        }
        return testCases;
    }

    public Path getFilePath() {
        return filePath.toAbsolutePath();
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
                    writeReadableRecord(writer, testCase);
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

    private TestCaseModel parseTableRow(String line, boolean hasSteps) {
        String[] fields = line.split("\t", -1);
        if (fields.length != (hasSteps ? 9 : 8)) {
            throw new IllegalArgumentException("Unexpected number of tab-separated fields.");
        }

        int id = Integer.parseInt(fields[0]);
        TestType type = TestType.valueOf(fields[1]);
        TestStatus status = TestStatus.valueOf(fields[2]);
        String title = fields[3];
        String description = fields[4];
        String preconditions = fields[5];
        String expectedResult = fields[hasSteps ? 7 : 6];
        String typeDetail = fields[hasSteps ? 8 : 7];

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

    private TestCaseModel parseLegacyTestCase(String line) {
        String[] fields = line.split("\\t", -1);
        if (fields.length != 8 && fields.length != 9) throw new IllegalArgumentException("Unexpected number of fields.");
        int id = Integer.parseInt(fields[0]);
        TestType type = TestType.valueOf(fields[1]);
        TestStatus status = TestStatus.valueOf(fields[2]);
        String title = decodeLegacy(fields[3]);
        String description = decodeLegacy(fields[4]);
        String preconditions = decodeLegacy(fields[5]);
        boolean hasSteps = fields.length == 9;
        String expectedResult = decodeLegacy(fields[hasSteps ? 7 : 6]);
        String detail = decodeLegacy(fields[hasSteps ? 8 : 7]);
        TestCaseModel testCase = type == TestType.MANUAL
                ? new ManualTestCaseModel(id, title, description, preconditions, expectedResult, detail)
                : new AutomatedTestCaseModel(id, title, description, preconditions, expectedResult, detail);
        testCase.setStatus(status);
        return testCase;
    }

    private String decodeLegacy(String value) {
        if ("-".equals(value)) return null;
        if (!value.startsWith("+")) throw new IllegalArgumentException("Invalid encoded field.");
        return new String(DECODER.decode(value.substring(1)), StandardCharsets.UTF_8);
    }

    private void writeReadableRecord(BufferedWriter writer, TestCaseModel testCase) throws IOException {
        writer.write(testCase.getId() + "\t"
                + testCase.getType() + "\t"
                + testCase.getStatus() + "\t"
                + tableCell(testCase.getTitle()) + "\t"
                + tableCell(testCase.getDescription()) + "\t"
                + tableCell(testCase.getPreconditions()) + "\t"
                + tableCell(testCase.getExpectedResult()) + "\t"
                + tableCell(testCase.getTypeDetail()));
        writer.newLine();
    }

    private String tableCell(String value) {
        return value == null ? "" : value.replace("\t", " ").replace("\r\n", " / ")
                .replace("\n", " / ").replace("\r", " / ");
    }

    private List<TestCaseModel> parseReadableRecords(List<String> lines) throws IOException {
        List<TestCaseModel> records = new ArrayList<>();
        java.util.Map<String, String> fields = new java.util.LinkedHashMap<>();
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                if (!fields.isEmpty()) {
                    records.add(createReadableRecord(fields));
                    fields.clear();
                }
                continue;
            }
            int separator = line.indexOf(':');
            if (separator < 1) throw new IOException("Invalid readable record in " + filePath);
            fields.put(line.substring(0, separator).trim(), line.substring(separator + 1).trim());
        }
        if (!fields.isEmpty()) records.add(createReadableRecord(fields));
        return records;
    }

    private TestCaseModel createReadableRecord(java.util.Map<String, String> fields) throws IOException {
        try {
            int id = Integer.parseInt(fields.get("ID"));
            TestType type = TestType.valueOf(fields.get("Type"));
            TestStatus status = TestStatus.valueOf(fields.get("Status"));
            String detailKey = type == TestType.MANUAL ? "Tester name" : "Script path";
            TestCaseModel testCase;
            if (type == TestType.MANUAL) {
                testCase = new ManualTestCaseModel(id, fields.get("Title"), fields.get("Description"),
                        fields.get("Preconditions"), fields.get("Expected result"), fields.get(detailKey));
            } else {
                testCase = new AutomatedTestCaseModel(id, fields.get("Title"), fields.get("Description"),
                        fields.get("Preconditions"), fields.get("Expected result"), fields.get(detailKey));
            }
            testCase.setStatus(status);
            return testCase;
        } catch (RuntimeException exception) {
            throw new IOException("Invalid readable test case record in " + filePath, exception);
        }
    }
}
