package validations;

import enums.TestStatus;
import enums.TestType;
import java.util.Scanner;

public class InputValidator {
    private final Scanner scanner;

    public InputValidator(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner cannot be null.");
        }
        this.scanner = scanner;
    }

    public String readRequired(String message) {
        while (true) {
            System.out.print(message);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field is required. Please try again.");
        }
    }

    public String readOptional(String message) {
        System.out.print(message);
        String value = scanner.nextLine().trim();
        return value.isEmpty() ? null : value;
    }

    public int readMenuChoice(int minimum, int maximum) {
        while (true) {
            System.out.print("Choose an option: ");
            String value = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(value);
                if (choice >= minimum && choice <= maximum) return choice;
            } catch (NumberFormatException ignored) {
               System.out.println("Invalid input. Please try again.");
            }
            System.out.printf("Enter a number from %d to %d.%n", minimum, maximum);
        }
    }

    public int readPositiveId() {
        while (true) {
            System.out.print("Enter test case ID: ");
            String value = scanner.nextLine().trim();
            try {
                int id = Integer.parseInt(value);
                if (id > 0) return id;
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid input. Please try again.");
            }
            System.out.println("Enter a valid positive ID.");
        }
    }

    public TestType readTestType() {
        while (true) {
            System.out.println("Select test type:");
            System.out.println("1. Manual");
            System.out.println("2. Automated");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 1) return TestType.MANUAL;
                if (choice == 2) return TestType.AUTOMATED;
            } catch (NumberFormatException ignored) {
               System.out.println("Please choose between one or two");
            }
            System.out.println("Enter 1 for Manual or 2 for Automated.");
        }
    }

    public TestStatus readTestStatus() {
        while (true) {
            System.out.println("Select test status:");
            System.out.println("1. PASS");
            System.out.println("2. FAIL");
            System.out.println("3. BLOCKED");
            System.out.println("4. NOT_RUN");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1:
                        return TestStatus.PASS;
                    case 2:
                        return TestStatus.FAIL;
                    case 3:
                        return TestStatus.BLOCKED;
                    case 4:
                        return TestStatus.NOT_RUN;
                    default:
                        System.out.println("Enter a number from 1 to 4.");
                }
            } catch (NumberFormatException ignored) {
                System.out.println("Enter a number from 1 to 4.");
            }
        }
    }
}
