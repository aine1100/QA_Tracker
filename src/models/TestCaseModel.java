package models;

import enums.TestStatus;
import enums.TestType;

public abstract class TestCaseModel {

    private int id;
    private String title;
    private String description;
    private String preconditions;
    private String expectedResult;
    private TestStatus status;

    public TestCaseModel(int id, String title, String description,
                    String preconditions, String expectedResult) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.preconditions = preconditions;
        this.expectedResult = expectedResult;
        this.status = TestStatus.NOT_RUN;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPreconditions() {
        return preconditions;
    }

    public String getExpectedResult() {
        return expectedResult;
    }


    public TestStatus getStatus() {
        return status;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPreconditions(String preconditions) {
        this.preconditions = preconditions;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }


    public void setStatus(TestStatus status) {
        this.status = status;
    }

    // Each child must say what it is and what its extra detail is
    public abstract TestType getType();

    public abstract String getTypeDetail();
}
