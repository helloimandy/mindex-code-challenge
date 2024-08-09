package com.mindex.challenge.service;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;

public interface ICompensationService {
    Compensation create(Compensation compensation);
    Compensation read(String id);
}
