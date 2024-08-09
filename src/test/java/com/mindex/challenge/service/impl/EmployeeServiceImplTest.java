package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String reportingStructureUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        reportingStructureUrl = "http://localhost:" + port + "/employee/{id}/reportingStructure";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }
    /*
        Using the format of the create test above to create my test employees, and then setting direct reports. We will
        call the entity and assert all the information we need for this method is correct. Afterward, we will follow
        a similar format and read the ReportingStructure entity and assert that.
     */
    @Ignore
    @Test
    public void testReportingStructure() {
        // Create test data
        Employee createEmployeeA = new Employee();
        createEmployeeA.setFirstName("EmployeeA");
        createEmployeeA.setLastName("LastA");
        createEmployeeA.setDepartment("Engineering");
        createEmployeeA.setPosition("Manager");

        Employee createEmployeeB = new Employee();
        createEmployeeB.setFirstName("EmployeeB");
        createEmployeeB.setLastName("LastB");
        createEmployeeB.setDepartment("Engineering");
        createEmployeeB.setPosition("Developer");

        Employee createEmployeeC = new Employee();
        createEmployeeC.setFirstName("EmployeeC");
        createEmployeeC.setLastName("LastC");
        createEmployeeC.setDepartment("Engineering");
        createEmployeeC.setPosition("Developer");

        // Create employees and check HTTP status
        ResponseEntity<Employee> responseEmployeeA = restTemplate.postForEntity(employeeUrl, createEmployeeA, Employee.class);
        ResponseEntity<Employee> responseEmployeeB = restTemplate.postForEntity(employeeUrl, createEmployeeB, Employee.class);
        ResponseEntity<Employee> responseEmployeeC = restTemplate.postForEntity(employeeUrl, createEmployeeC, Employee.class);

        assertEquals(HttpStatus.OK, responseEmployeeA.getStatusCode());
        assertEquals(HttpStatus.OK, responseEmployeeB.getStatusCode());
        assertEquals(HttpStatus.OK, responseEmployeeC.getStatusCode());

        Employee createdEmployeeA = responseEmployeeA.getBody();
        Employee createdEmployeeB = responseEmployeeB.getBody();
        Employee createdEmployeeC = responseEmployeeC.getBody();

        System.out.println("EmployeeA ID: " + createdEmployeeA.getEmployeeId());
        System.out.println("EmployeeB ID: " + createdEmployeeB.getEmployeeId());
        System.out.println("EmployeeC ID: " + createdEmployeeC.getEmployeeId());

        assertNotNull(createdEmployeeA.getEmployeeId());
        assertNotNull(createdEmployeeB.getEmployeeId());
        assertNotNull(createdEmployeeC.getEmployeeId());

        createdEmployeeA.setDirectReports(Arrays.asList(createdEmployeeB, createdEmployeeC));
        createdEmployeeC.setDirectReports(Collections.singletonList(createdEmployeeB));

        restTemplate.put(employeeIdUrl, createdEmployeeA, createdEmployeeA.getEmployeeId());
        restTemplate.put(employeeIdUrl, createdEmployeeC, createdEmployeeC.getEmployeeId());

        ReportingStructure reportingStructure = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, createdEmployeeA.getEmployeeId()).getBody();

        assertNotNull(reportingStructure);
        assertEquals(2, reportingStructure.getNumberOfReports());
        assertEquals("EmployeeA", reportingStructure.getEmployee().getFirstName());
        assertEquals("EmployeeB", reportingStructure.getEmployee().getDirectReports().get(0).getFirstName());
        assertEquals("EmployeeC", reportingStructure.getEmployee().getDirectReports().get(1).getFirstName());
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
