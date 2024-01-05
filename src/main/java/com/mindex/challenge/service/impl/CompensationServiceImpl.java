package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompensationServiceImpl implements CompensationService {
    @Autowired
    CompensationRepository compensationRepository;

    @Override
    public Compensation create(Compensation compensation) {
        String employeeId = compensation.getEmployee().getEmployeeId();
        if (compensationRepository.findByEmployeeEmployeeId(employeeId) != null) {
            throw new RuntimeException("Employee with id " + employeeId + " already has compensation");
        }
        return compensationRepository.insert(compensation);
    }

    @Override
    public Compensation read(String employeeId) {
        Compensation compensation = compensationRepository.findByEmployeeEmployeeId(employeeId);
        if (compensation == null) {
            throw new RuntimeException("Compensation does not exist for employeeId: " + employeeId);
        }
        return compensation;
    }
}