package com.payroll.service;

import java.math.BigDecimal;

public record MonthlyIncomeLine(Integer categoryId, BigDecimal amount) {
}
