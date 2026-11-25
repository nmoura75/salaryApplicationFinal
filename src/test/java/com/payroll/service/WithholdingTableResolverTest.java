package com.payroll.service;

import com.payroll.model.IncomeHolders;
import com.payroll.model.MaritalStatus;
import com.payroll.model.WithholdingCategory;
import com.payroll.model.WithholdingTable;
import com.payroll.service.WithholdingChoice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WithholdingTableResolverTest {

    private final WithholdingTableResolver resolver = new WithholdingTableResolver();

    @ParameterizedTest(name = "{0}, {1}, dependents {2}, disability {3} -> {4}")
    @CsvSource({
            "NOT_MARRIED, SINGLE_HOLDER, 0, false, TABLE_I",
            "MARRIED, TWO_HOLDERS, 2, false, TABLE_I",
            "NOT_MARRIED, SINGLE_HOLDER, 1, false, TABLE_II",
            "NOT_MARRIED, TWO_HOLDERS, 2, false, TABLE_II",
            "MARRIED, SINGLE_HOLDER, 0, false, TABLE_III",
            "MARRIED, SINGLE_HOLDER, 3, false, TABLE_III",
            "NOT_MARRIED, SINGLE_HOLDER, 0, true, TABLE_IV",
            "MARRIED, TWO_HOLDERS, 0, true, TABLE_IV",
            "NOT_MARRIED, SINGLE_HOLDER, 1, true, TABLE_V",
            "MARRIED, TWO_HOLDERS, 2, true, TABLE_VI",
            "MARRIED, SINGLE_HOLDER, 0, true, TABLE_VII",
            "MARRIED, SINGLE_HOLDER, 1, true, TABLE_VII"
    })
    void choosesTheContinenteEmploymentTable(MaritalStatus status, IncomeHolders holders, int dependents,
                                             boolean disability, WithholdingTable expected) {
        WithholdingChoice choice = resolver.resolve(status, holders, dependents, disability);

        assertEquals(expected, choice.table());
        assertEquals(WithholdingCategory.CATEGORY_A, choice.category());
    }

    @Test
    void olderHouseholdCodesAreReadAsNotMarried() {
        assertEquals(MaritalStatus.NOT_MARRIED, MaritalStatus.fromCode(1));
        assertEquals(MaritalStatus.MARRIED, MaritalStatus.fromCode(2));
        assertEquals(MaritalStatus.NOT_MARRIED, MaritalStatus.fromCode(3));
        assertEquals(MaritalStatus.NOT_MARRIED, MaritalStatus.fromCode(4));
    }
}
