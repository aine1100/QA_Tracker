package models;

import enums.TestType;

public class AutomatedTestCaseModel extends TestCaseModel {

    private String scriptPath;

    public AutomatedTestCaseModel(int id, String title, String description,
                             String preconditions, String expectedResult,
                             String scriptPath) {
        super(id, title, description, preconditions, expectedResult);
        this.scriptPath = scriptPath;
    }

    public String getScriptPath() { 
        return scriptPath; }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    @Override
    public TestType getType() {
        return TestType.AUTOMATED;
    }

    @Override
    public String getTypeDetail() {
        return scriptPath;
    }
}
