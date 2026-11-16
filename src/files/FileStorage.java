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

    private static final String HEADER = "QA_TRACKER\t1";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final Path filePath;

    public FileStorage() {
        this(Paths.get("data", "test-cases.txt"));
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
            if (!HEADER.equals(header)) {
                throw new IOException("Unrecognized test case file format: " + filePath);
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isEmpty()) {
                    continue;
                }
                try {
                    testCases.add(parseTestCase(line));
                } catch (IllegalArgumentException exception) {
                    throw new IOException(
                            "Invalid test case record at line " + lineNumber + " in " + filePath,
                            exception);
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
                    writer.write(formatTestCase(testCase));
                    writer.newLine();
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

    private TestCaseModel parseTestCase(String line) {
        String[] fields = line.split("\t", -1);
        if (fields.length != 8) {
            throw new IllegalArgumentException("Expected 8 tab-separated fields.");
        }

        int id = Integer.parseInt(fields[0]);
        TestType type = TestType.valueOf(fields[1]);
        TestStatus status = TestStatus.valueOf(fields[2]);
        String title = decode(fields[3]);
        String description = decode(fields[4]);
        String preconditions = decode(fields[5]);
        String expectedResult = decode(fields[6]);
        String typeDetail = decode(fields[7]);

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

    private String formatTestCase(TestCaseModel testCase) {
        return testCase.getId() + "\t"
                + testCase.getType().name() + "\t"
                + testCase.getStatus().name() + "\t"
                + encode(testCase.getTitle()) + "\t"
                + encode(testCase.getDescription()) + "\t"
                + encode(testCase.getPreconditions()) + "\t"
                + encode(testCase.getExpectedResult()) + "\t"
                + encode(testCase.getTypeDetail());
    }

    private String encode(String value) {
        return value == null ? "-" : "+" + ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        if ("-".equals(value)) {
            return null;
        }
        if (!value.startsWith("+")) {
            throw new IllegalArgumentException("Invalid encoded text field.");
        }
        byte[] bytes = DECODER.decode(value.substring(1));
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
