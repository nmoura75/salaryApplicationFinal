package com.payroll.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Receipts created for a month, and employees skipped because a receipt already exists.
 */
@Data
public class ProcessMonthResult {

    private List<com.payroll.model.SalaryReceipt> created = new ArrayList<>();
    private List<String> refusedEmployeeNames = new ArrayList<>();
}
