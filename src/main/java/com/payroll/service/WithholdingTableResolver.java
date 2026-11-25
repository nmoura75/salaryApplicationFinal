package com.payroll.service;

import com.payroll.model.IncomeHolders;
import com.payroll.model.MaritalStatus;
import com.payroll.model.WithholdingCategory;
import com.payroll.model.WithholdingTable;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Chooses the Continente dependent-work table, I to VII, from the employee's household.
 * A disability stays in category A and selects tables IV to VII. Pension tables are not included.
 */
@Component
public class WithholdingTableResolver {

    public WithholdingChoice resolve(MaritalStatus status, IncomeHolders holders, int dependents, boolean disability) {
        Objects.requireNonNull(status, "maritalStatus");
        Objects.requireNonNull(holders, "incomeHolders");
        boolean married = status == MaritalStatus.MARRIED;
        boolean singleHolder = holders != IncomeHolders.TWO_HOLDERS;
        boolean hasDependents = dependents > 0;
        WithholdingCategory category = WithholdingCategory.CATEGORY_A;

        if (!disability) {
            if (married && singleHolder) {
                return new WithholdingChoice(category, WithholdingTable.TABLE_III);
            }
            if (!married && hasDependents) {
                return new WithholdingChoice(category, WithholdingTable.TABLE_II);
            }
            return new WithholdingChoice(category, WithholdingTable.TABLE_I);
        }
        if (married && singleHolder) {
            return new WithholdingChoice(category, WithholdingTable.TABLE_VII);
        }
        if (married && hasDependents) {
            return new WithholdingChoice(category, WithholdingTable.TABLE_VI);
        }
        if (!married && hasDependents) {
            return new WithholdingChoice(category, WithholdingTable.TABLE_V);
        }
        return new WithholdingChoice(category, WithholdingTable.TABLE_IV);
    }
}
