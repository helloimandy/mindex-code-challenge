package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.ICompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.UUID;

@Service
public class CompensationServiceImpl implements ICompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    private final CompensationRepository compensationRepository;

    private final EmployeeRepository employeeRepository;

    @Autowired
    public CompensationServiceImpl(CompensationRepository compensationRepository, EmployeeRepository employeeRepository) {
        this.compensationRepository = compensationRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation for employee [{}]", compensation.getEmployee());

        if (compensation.getEmployee() == null) {
            throw new IllegalArgumentException("Compensation must be associated with an employee.");
        }

        compensation.getEmployee().setEmployeeId(UUID.randomUUID().toString());
        compensationRepository.insert(compensation);

        return compensation;
    }

    @Override
    public Compensation read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Compensation compensation = compensationRepository.findByEmployeeEmployeeId(id);

        if (compensation == null) {
            throw new RuntimeException("Cannot find compensation for id: " + id);
        } else {
            loadDirectReportsData(compensation.getEmployee(), new HashSet<>());
        }

        return compensation;
    }

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
}
