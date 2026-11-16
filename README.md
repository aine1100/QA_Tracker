# QA Tracker

QA Tracker is a console application for recording manual and automated test cases, tracking their execution status, searching cases, and producing a summary report. It saves records as a tab-separated table in the project's `data/test-cases.txt`. The columns name the test case ID, type, status, title, description, preconditions, expected result, and tester name (manual) or script path (automated). The file is created the first time you choose Save, add a case, update a status, or exit; it is not created just by starting the app. After saving, the application prints the full path so the file is easy to locate.

## Run the application

From the `QA_Tracker` project directory, compile and run with Java 17 or later:

```sh
javac -d out src/Main.java src/enums/*.java src/models/*.java src/validations/*.java src/services/*.java src/files/*.java
java -cp out Main
```

The menu lets you add a test case, view all cases, update a status, search by title or description, generate a report, save, and exit. Required test case details cannot be left blank. Separate multiple preconditions or expected results with commas, for example `Access to the home page, Existing account` or `Welcome message appears, Dashboard is displayed`. Type and status selections use numbered menus. The application loads saved data at startup and saves changes after adding or updating a case, as well as on explicit save and exit.

## Project structure

```text
src/
├── Main.java
├── enums/       TestStatus, TestType
├── files/       FileStorage
├── models/      TestCaseModel, ManualTestCaseModel, AutomatedTestCaseModel
├── services/    TestCaseService, ReportGeneratorService
└── validations/ InputValidator
```

The package names above match the existing Java source. Java package names and directory names must match, so this project uses `models`, `validations`, `services`, and `files` (plural / existing names) rather than the singular names in the original outline.

## Java concepts used

### Classes, objects, and constructors

Each class describes a kind of object. For example, `ManualTestCaseModel` objects hold the data for individual manual tests. Constructors initialize those objects with an ID, title, description, preconditions, expected result, and type-specific detail.

### Encapsulation

`TestCaseModel` stores its fields as `private`. Other classes use getters and setters to read or update them. This keeps the data and its access points organized in the model.

### Abstract classes, inheritance, and polymorphism

`TestCaseModel` is abstract because a generic test case is not created directly. `ManualTestCaseModel` and `AutomatedTestCaseModel` extend it and supply their own test type and type detail. Services can work with a `List<TestCaseModel>` and use each object's overridden methods, allowing both kinds of test case to be handled uniformly.

### Enums

`TestType` restricts a case to `MANUAL` or `AUTOMATED`. `TestStatus` restricts execution status to `PASS`, `FAIL`, `BLOCKED`, or `NOT_RUN`. Enums avoid inconsistent free-form values and are validated when entered at the console.

### Collections and generics

`TestCaseService` stores cases in an `ArrayList<TestCaseModel>`. The `List<TestCaseModel>` type communicates that the collection contains test case models and allows it to be returned or passed between services. The service returns a copy for listing, so callers do not directly replace its internal list.

### Methods, loops, and conditional logic

Methods separate responsibilities such as adding cases, searching, updating status, generating reports, and saving data. Loops power the interactive menu, input retries, collection searches, and report counting. `if` statements and a `switch` select behavior based on user choices and test case values.

### Input validation and exception handling

`InputValidator` checks required text, positive numeric IDs, menu bounds, and enum values. Invalid input prompts the user again instead of terminating the application. `Main` catches loading and saving errors so file problems are reported clearly to the user.

### File I/O and try-with-resources

`FileStorage` uses `Path` and `Files` to read and write records. It uses try-with-resources so readers and writers are closed automatically. Each row of the tab-separated table preserves the ID, type, status, shared fields, and type-specific detail. Multiple preconditions are joined with `/` in the table cell. Missing data files are treated as an empty collection, and writes use a temporary file before replacing the saved file. Older saved records can still be loaded.

### Separation of responsibilities

`Main` connects the user interface to the other classes. `InputValidator` handles console input, model classes represent data, `TestCaseService` manages cases, `ReportGeneratorService` calculates statistics, and `FileStorage` handles persistence. Keeping these jobs in separate classes makes the program easier to understand and maintain.

## Report calculation

The report counts total, manual, and automated cases, then counts each status. Pass rate is calculated as `PASS count / total test cases × 100`; for an empty project it is `0%` to avoid division by zero.
