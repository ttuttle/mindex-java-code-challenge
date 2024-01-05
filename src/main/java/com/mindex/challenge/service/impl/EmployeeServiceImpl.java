package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

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

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public List<Employee> readMultiple(List<String> ids) {
        List<Employee> employees = new ArrayList<>();
        for (String id : ids) {
            employees.add(employeeRepository.findByEmployeeId(id));
        }
        return employees;
    }

    @Override
    public List<Employee> collectReports(Employee employee) {
        List<Employee> directReports = employee.getDirectReports();
        List<Employee> reportingEmployees = new ArrayList<>();

        if (null == directReports) {
            return emptyList();
        }

        // Collect direct report employee objects, as direct reports only contain ids
        for (Employee e : employee.getDirectReports()) {
            reportingEmployees.add(employeeRepository.findByEmployeeId(e.getEmployeeId()));
        }

        return collectReports(reportingEmployees);
    }

    @Override
    public List<Employee> collectReports(List<Employee> reportingEmployees) {
        return reportingEmployees.stream().flatMap(e -> e.getDirectReports() != null ?
                // For each direct report, recursively concatenate the employee with underlying direct reports
                Stream.concat(
                        Stream.of(e),
                        collectReports(
                                readMultiple(e.getDirectReports().stream().map(Employee::getEmployeeId).collect(Collectors.toList()))
                        ).stream()
                )
                // If employee has no direct reports, only return the employee
                : Stream.of(e)).collect(Collectors.toList());
    }
}
