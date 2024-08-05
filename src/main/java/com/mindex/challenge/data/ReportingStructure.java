package com.mindex.challenge.data;

public class ReportingStructure {
    private int numberOfReports;
    private Employee employee;

    public ReportingStructure(int numberOfReports, Employee employee) {
        this.numberOfReports = numberOfReports;
        this.employee = employee;
    }

    public int getNumberOfReports() {
        return numberOfReports;
    }

    public void setNumberOfReports(int numberOfReports) {
        this.numberOfReports = numberOfReports;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
