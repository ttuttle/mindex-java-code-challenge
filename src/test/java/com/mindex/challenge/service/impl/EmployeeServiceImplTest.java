package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

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

    @Test
    public void testReadMultiple() {
        List<String> idList = List.of(
                "16a596ae-edd3-4847-99fe-c4518e82c86f", // John
                "b7839309-3348-463b-a7e3-5de1c168beb3", // Paul
                "03aa1462-ffa9-4978-901b-7c001562cf6f" // Ringo
        );
        List<Employee> expectedEmployees = List.of(
                employeeService.read(idList.get(0)),
                employeeService.read(idList.get(1)),
                employeeService.read(idList.get(2))
        );

        List<Employee> returnedEmployees = employeeService.readMultiple(idList);

        Assertions.assertThat(expectedEmployees).usingRecursiveFieldByFieldElementComparator().isEqualTo(returnedEmployees);
    }

    @ParameterizedTest
    @CsvSource({
            "16a596ae-edd3-4847-99fe-c4518e82c86f, 4", // John
            "b7839309-3348-463b-a7e3-5de1c168beb3, 0", // Paul
            "03aa1462-ffa9-4978-901b-7c001562cf6f, 2" // Ringo
    })

    public void testGetReportingStructure(String employeeId, Integer expectedReportCount) {
        reportingStructureUrl = "http://localhost:" + port + "/employee/reportingStructure/{id}";
        Employee expectedEmployee = employeeService.read(employeeId);

        ReportingStructure reportingStructure = restTemplate.getForEntity(reportingStructureUrl,
                ReportingStructure.class,
                employeeId).getBody();

        ReportingStructure expectedReportingStructure = new ReportingStructure(expectedEmployee, expectedReportCount);

        Assertions.assertThat(reportingStructure).usingRecursiveComparison().isEqualTo(expectedReportingStructure);
    }

    @Test
    public void testCollectReports() {
        List<String> ids = List.of("03aa1462-ffa9-4978-901b-7c001562cf6f", "b7839309-3348-463b-a7e3-5de1c168beb3", "62c1084e-6e34-4630-93fd-9153afb65309", "c0c2293d-16bd-4603-8e08-638a9d18b22c");
        List<Employee> expectedEmployees = employeeService.readMultiple(ids);

        Employee john = employeeService.read("16a596ae-edd3-4847-99fe-c4518e82c86f");

        List<Employee> reportingEmployees = employeeService.collectReports(john);

        assertEquals(4, reportingEmployees.size());
        Assertions.assertThat(expectedEmployees).usingRecursiveFieldByFieldElementComparator().containsAll(reportingEmployees);
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
