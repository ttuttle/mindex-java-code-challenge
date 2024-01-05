package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import com.mindex.challenge.service.EmployeeService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {
    @Autowired
    CompensationService compensationService;

    @Autowired
    EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    TestRestTemplate testRestTemplate;

    String compensationUrl;

    @Before
    public void setup() {
        compensationUrl = "http://localhost:" + port + "/employee/compensation/{id}";
    }

    @Test
    public void testCreateRead() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Manager");
        testEmployee.setEmployeeId("1");

        Compensation compensation = new Compensation(testEmployee, 200000, Date.from(Instant.now()));

        // Create check
        Compensation createdCompensation = testRestTemplate.exchange(
                compensationUrl,
                HttpMethod.POST,
                new HttpEntity<>(compensation),
                Compensation.class,
                testEmployee.getEmployeeId()).getBody();

        Assertions.assertThat(createdCompensation).usingRecursiveComparison().isEqualTo(compensation);

        // Read check
        Compensation readCompensation = testRestTemplate.exchange(
                compensationUrl,
                HttpMethod.GET,
                new HttpEntity<>(compensation),
                Compensation.class,
                testEmployee.getEmployeeId()).getBody();

        Assertions.assertThat(readCompensation).usingRecursiveComparison().isEqualTo(compensation);
    }

    @Test
    public void testCreate_forExistingEmployeeCompensation() throws RuntimeException {
        Employee testEmployee = employeeService.read("16a596ae-edd3-4847-99fe-c4518e82c86f");
        Compensation compensation = new Compensation(testEmployee, 200000, Date.from(Instant.now()));
        String message = assertThrows(RuntimeException.class, () -> compensationService.create(compensation)).getMessage();
        assertEquals(message, "Employee with id 16a596ae-edd3-4847-99fe-c4518e82c86f already has compensation");
    }

    @Test
    public void testRead_forEmployeeWithoutCompensation() throws RuntimeException {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Manager");
        testEmployee.setEmployeeId("1");

        String message = assertThrows(RuntimeException.class, () -> compensationService.read(testEmployee.getEmployeeId())).getMessage();
        assertEquals(message, "Compensation does not exist for employeeId: 1");
    }
}
