package com.payroll.service;

import com.payroll.model.WithholdingCategory;
import com.payroll.model.WithholdingTable;

/**
 * Category A with one of the dependent-work tables I to VII.
 * Pension tables VIII to XI are not chosen here.
 */
public record WithholdingChoice(WithholdingCategory category, WithholdingTable table) {
}
