package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }
    /*
        I noticed this base read method that was already a part of this file was basically having the same issue as I
        was having before, where it does not recursively go deep to load the indirect report's data. I will just add the
        recursion call here to make things consistent and have the full data.
     */
    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        } else {
            loadDirectReportsData(employee, new HashSet<>());
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    /*
        1.) Our goal is to find the number of direct reports for each employee id, with a chance that their direct report
        also has direct reports. We can imagine this to be a tree structure, and we can easily find the total number of
        reports by traversing through the tree and just keeping count of each direct report.

        2.) There could be a cycle error, i.e. Employee A has direct report of Employee B who has direct report of
        Employee C but then Employee C loops back around to Employee A. The HashSet can be used to keep track of
        pre-processed employees to avoid reprocessing any cycles.

    */
    public ReportingStructure getReportingStructure(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId);
        if (employee != null) {
            loadDirectReportsData(employee, new HashSet<>());
        }
        int directReports = calculateDirectReports(employee, new HashSet<>());
        return new ReportingStructure(directReports, employee);
    }
    /*
        After calculating the step above, I realized the direct reports object information was not loaded in, we must
        also apply the same logic of recursively set the indirect report's full data
     */
    private void loadDirectReportsData(Employee employee, HashSet<String> seen) {
        if (employee == null || seen.contains(employee.getEmployeeId())) {
            return;
        }
        seen.add(employee.getEmployeeId());
        if (employee.getDirectReports() != null && !employee.getDirectReports().isEmpty()) {
            for (int i = 0; i < employee.getDirectReports().size(); i++) {
                String directReportId = employee.getDirectReports().get(i).getEmployeeId();
                System.out.println("Loading direct report: " + directReportId);

                Employee directReport = employeeRepository.findByEmployeeId(directReportId);
                System.out.println("Full report loaded: " + (directReport != null ? directReport.getEmployeeId() : "null"));

                loadDirectReportsData(directReport, seen);
                employee.getDirectReports().set(i, directReport);
            }
        }
    }

    /*
        Here is where our recursive call is happening. We know that once we are not getting anymore indirect reports, or
        we land on someone that is already processed we have our base case to return 0.

        Within the actual calculation, note that the + 1 is for the count of the direct report itself, and the recursive
        call itself are for the indirect reports.
     */
    private int calculateDirectReports(Employee employee, HashSet<String> seen) {
        if (employee == null || seen.contains(employee.getEmployeeId())) {
            return 0;
        }
        seen.add(employee.getEmployeeId());
        int count = 0;
        if (employee.getDirectReports() != null) {
            for (Employee directReport : employee.getDirectReports()) {
                // putting logs to see outputs on null report for testing class (since it is failing)
                if (directReport == null) {
                    System.out.println("Null direct report found for employee: " + employee.getEmployeeId());
                    continue;
                }
                Employee indirectReports = employeeRepository.findByEmployeeId(directReport.getEmployeeId());
                if (indirectReports == null) {
                    System.out.println("No employee found for ID: " + directReport.getEmployeeId());
                    continue;
                }

                count += calculateDirectReports(indirectReports, seen) + 1;
            }
        }

        return count;
    }
}
