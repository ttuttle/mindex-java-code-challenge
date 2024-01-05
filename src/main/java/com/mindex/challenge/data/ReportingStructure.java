package com.mindex.challenge.data;

public class ReportingStructure {
    private final Employee employee;
    private final Integer reportCount;

    public ReportingStructure(Employee employee, Integer reportCount) {
        this.employee = employee;
        this.reportCount = reportCount;
    }
    public Employee getEmployee() {
        return employee;
    }
    public Integer getReportCount() {
        return reportCount;
    }
}