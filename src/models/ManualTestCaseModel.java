package models;

import enums.TestType;

public class ManualTestCaseModel extends TestCaseModel {

    private String testerName;

    public ManualTestCaseModel(int id, String title, String description,
                          String preconditions, String expectedResult,
                          String testerName) {
        super(id, title, description, preconditions, expectedResult);
        this.testerName = testerName;
    }

    public String getTesterName() { return testerName; }

    public void setTesterName(String testerName) {
        this.testerName = testerName;
    }

    @Override
    public TestType getType() {
        return TestType.MANUAL;
    }

    @Override
    public String getTypeDetail() {
        return testerName;
    }
}
