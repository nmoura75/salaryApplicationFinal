package com.payroll.service;

import com.payroll.model.WithholdingTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Continente employment withholding for 2026, tables I to VII.
 * The next year is another file. The formula stays the same.
 */
public final class WithholdingTables {

    private static final String RESOURCE = "/withholding/continente-2026.csv";
    private static final int COLUMNS = 8;
    private static final int DEPENDENTS_FOR_RATE_REDUCTION = 3;
    private static final BigDecimal RATE_REDUCTION = new BigDecimal("0.01");
    private static final WithholdingTables CONTINENTE_2026 = load(RESOURCE);

    private final Map<WithholdingTable, List<Bracket>> bracketsByTable;

    private WithholdingTables(Map<WithholdingTable, List<Bracket>> bracketsByTable) {
        this.bracketsByTable = bracketsByTable;
    }

    public static WithholdingTables continente2026() {
        return CONTINENTE_2026;
    }

    /**
     * Withholding on monthly remuneration {@code remuneration}. Social security is not deducted from it.
     * Three or more dependents lower the marginal rate by one point. The parcela keeps the rate printed in the table.
     */
    public BigDecimal withhold(WithholdingTable table, BigDecimal remuneration, int dependents) {
        Objects.requireNonNull(table, "withholdingTable");
        BigDecimal amount = remuneration == null ? BigDecimal.ZERO : remuneration;
        int dependentCount = Math.max(dependents, 0);
        Bracket bracket = bracketFor(table, amount);
        BigDecimal rate = marginalRate(bracket, dependentCount);
        BigDecimal tax = amount.multiply(rate)
                .subtract(bracket.deduction(amount))
                .subtract(bracket.extraPerDependent().multiply(BigDecimal.valueOf(dependentCount)));
        return money(tax.max(BigDecimal.ZERO));
    }

    private Bracket bracketFor(WithholdingTable table, BigDecimal amount) {
        List<Bracket> brackets = bracketsByTable.get(table);
        if (brackets == null || brackets.isEmpty()) {
            throw new IllegalArgumentException("No withholding brackets for " + table);
        }
        for (Bracket bracket : brackets) {
            if (bracket.upperLimit() == null || amount.compareTo(bracket.upperLimit()) <= 0) {
                return bracket;
            }
        }
        throw new IllegalStateException("Withholding table " + table + " has no open bracket");
    }

    private static BigDecimal marginalRate(Bracket bracket, int dependents) {
        if (dependents >= DEPENDENTS_FOR_RATE_REDUCTION) {
            return bracket.marginalRate().subtract(RATE_REDUCTION);
        }
        return bracket.marginalRate();
    }

    private static WithholdingTables load(String resource) {
        InputStream stream = WithholdingTables.class.getResourceAsStream(resource);
        if (stream == null) {
            throw new IllegalStateException("Withholding tables are missing");
        }
        Map<WithholdingTable, List<Bracket>> bracketsByTable = new EnumMap<>(WithholdingTable.class);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String value = line.replace("\uFEFF", "").trim();
                if (value.isEmpty() || value.startsWith("table;")) {
                    continue;
                }
                BracketRow row = BracketRow.parse(value);
                bracketsByTable.computeIfAbsent(row.table(), ignored -> new ArrayList<>()).add(row.bracket());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read the withholding tables", exception);
        }
        for (WithholdingTable table : WithholdingTable.values()) {
            List<Bracket> brackets = bracketsByTable.get(table);
            if (brackets == null || brackets.isEmpty() || brackets.get(brackets.size() - 1).upperLimit() != null) {
                throw new IllegalStateException("Withholding table " + table + " needs an open bracket");
            }
        }
        bracketsByTable.replaceAll((table, brackets) -> List.copyOf(brackets));
        return new WithholdingTables(Map.copyOf(bracketsByTable));
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal decimal(String value) {
        return new BigDecimal(value.trim());
    }

    private record Bracket(BigDecimal upperLimit,
                           BigDecimal marginalRate,
                           BigDecimal fixedDeduction,
                           BigDecimal formulaRate,
                           BigDecimal formulaFactor,
                           BigDecimal formulaReference,
                           BigDecimal extraPerDependent) {

        private BigDecimal deduction(BigDecimal remuneration) {
            if (formulaRate == null) {
                return fixedDeduction;
            }
            return formulaRate.multiply(formulaFactor).multiply(formulaReference.subtract(remuneration));
        }
    }

    private record BracketRow(WithholdingTable table, Bracket bracket) {

        private static BracketRow parse(String line) {
            String[] parts = line.split(";", -1);
            if (parts.length != COLUMNS) {
                throw new IllegalStateException("Withholding row must have " + COLUMNS + " columns: " + line);
            }
            WithholdingTable table = tableOf(parts[0].trim());
            BigDecimal upperLimit = blank(parts[1]) ? null : decimal(parts[1]);
            BigDecimal marginalRate = decimal(parts[2]);
            boolean formula = !blank(parts[4]);
            if (formula && (blank(parts[5]) || blank(parts[6]))) {
                throw new IllegalStateException("Formula row is incomplete: " + line);
            }
            BigDecimal fixedDeduction = formula ? null : decimal(blank(parts[3]) ? "0" : parts[3]);
            BigDecimal formulaRate = formula ? decimal(parts[4]) : null;
            BigDecimal formulaFactor = formula ? decimal(parts[5]) : null;
            BigDecimal formulaReference = formula ? decimal(parts[6]) : null;
            BigDecimal extra = blank(parts[7]) ? BigDecimal.ZERO : decimal(parts[7]);
            Bracket bracket = new Bracket(upperLimit, marginalRate, fixedDeduction,
                    formulaRate, formulaFactor, formulaReference, extra);
            return new BracketRow(table, bracket);
        }

        private static WithholdingTable tableOf(String token) {
            return switch (token) {
                case "I" -> WithholdingTable.TABLE_I;
                case "II" -> WithholdingTable.TABLE_II;
                case "III" -> WithholdingTable.TABLE_III;
                case "IV" -> WithholdingTable.TABLE_IV;
                case "V" -> WithholdingTable.TABLE_V;
                case "VI" -> WithholdingTable.TABLE_VI;
                case "VII" -> WithholdingTable.TABLE_VII;
                default -> throw new IllegalStateException("Unknown withholding table: " + token);
            };
        }

        private static boolean blank(String value) {
            return value == null || value.isBlank();
        }
    }
}
