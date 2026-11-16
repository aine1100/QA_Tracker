package services;

import enums.TestStatus;
import enums.TestType;
import models.TestCaseModel;

import java.util.List;

public class ReportGeneratorService {

    public void generateReport(List<TestCaseModel> testCases) {
        int total = 0;
        int manual = 0;
        int automated = 0;
        int pass = 0;
        int fail = 0;
        int blocked = 0;
        int notRun = 0;

        if (testCases != null) {
            total = testCases.size();
            for (TestCaseModel tc : testCases) {
                if (tc.getType() == TestType.MANUAL) {
                    manual++;
                } else if (tc.getType() == TestType.AUTOMATED) {
                    automated++;
                }

                TestStatus status = tc.getStatus();
                if (status == TestStatus.PASS) {
                    pass++;
                } else if (status == TestStatus.FAIL) {
                    fail++;
                } else if (status == TestStatus.BLOCKED) {
                    blocked++;
                } else if (status == TestStatus.NOT_RUN) {
                    notRun++;
                }
            }
        }

        double passRate = calculatePassRate(pass, total);

        String thick = "================================";
        String thin = "--------------------------------";

        System.out.println(thick);
        System.out.println(" TEST EXECUTION REPORT");
        System.out.println(thick);
        System.out.println("Total Test Cases : " + total);
        System.out.println(" - Manual : " + manual);
        System.out.println(" - Automated : " + automated);
        System.out.println(thin);
        System.out.println("PASS : " + pass);
        System.out.println("FAIL : " + fail);
        System.out.println("BLOCKED : " + blocked);
        System.out.println("NOT RUN : " + notRun);
        System.out.println(thin);
        System.out.printf("Pass Rate : %.1f%%%n", passRate);
        System.out.println(thick);
    }

    public double calculatePassRate(int pass, int total) {
        if (total == 0) {
            return 0.0;
        }
        return (pass * 100.0) / total;
    }
}