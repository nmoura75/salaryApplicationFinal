package com.payroll.service;

import com.payroll.model.RemunerationCategory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Income types taken from the Social Security remuneration catalogue.
 * Subject-to-contribution flags stay at the category default until that configuration exists.
 */
public final class StandardRemunerations {

    public static final String BASE_SALARY = "Remuneração base";
    public static final String MEAL_ALLOWANCE = "Subsídio de refeição";
    public static final String CHRISTMAS_BONUS = "Subsídio de Natal";
    public static final String VACATION_SUBSIDY = "Subsídio de férias";
    public static final String TRANSPORT_ALLOWANCE = "Subsídio de Transporte";

    private static final List<Type> TYPES = List.of(
            type("A", "A22", "Ajudas de custo e transportes Portugal (Dia)", Kind.DAILY, "69.19", 1),
            type("B", "A23", "Abono para Falhas", Kind.MONTHLY, "125.00", 2),
            type("C", "A", "Comissões", Kind.MONTHLY, "0.00", 3),
            type("D", "A20", "Compensação por cessação do contrato de trabalho", Kind.MONTHLY, "0.00", 4),
            type("F", "A4", VACATION_SUBSIDY, Kind.MONTHLY, "0.00", 5),
            type("H", "A", "Honorários por acumulação de atividades", Kind.MONTHLY, "0.00", 6),
            type("M", "A", "Subsídios de caráter regular mensal", Kind.MONTHLY, "0.00", 7),
            type("O", "A", "Prémios, bónus e outras prestações de caráter não mensal", Kind.MONTHLY, "0.00", 8),
            type("P", "A", BASE_SALARY, Kind.MONTHLY, "0.00", 9),
            type("R", "A21", MEAL_ALLOWANCE, Kind.MEAL, "10.46", 10),
            type("S", "A", "Trabalho suplementar", Kind.MONTHLY, "0.00", 11),
            type("T", "A", "Trabalho noturno", Kind.MONTHLY, "0.00", 12),
            type("X", "A", "Subsídios de caráter regular não mensal", Kind.MONTHLY, "0.00", 13),
            type("2", "A", "Remunerações referentes a férias pagas e não gozadas", Kind.MONTHLY, "0.00", 14),
            type("6", "A", "Diferenças de remunerações", Kind.MONTHLY, "0.00", 15),
            type("I", "A3", "Compensação remuneratória do Contrato Intermitente", Kind.MONTHLY, "0.00", 16),
            type("N", "A3", CHRISTMAS_BONUS, Kind.MONTHLY, "0.00", 17),
            type("Y", "Y", "Penhora Salarial/Falhas de Caixa", Kind.MONTHLY, "0.00", 18),
            type("A", "A22", TRANSPORT_ALLOWANCE, Kind.MONTHLY, "69.19", 19),
            type("P", "A", "Remuneração base Apoio A familia Estado", Kind.MONTHLY, "0.00", 20),
            type("P", "A", "Remuneração base Apoio A familia Empresa", Kind.MONTHLY, "0.00", 21),
            type("P", "A", "Remuneração base LayOff - Empresa", Kind.MONTHLY, "0.00", 22),
            type("P", "A", "Remuneração base LayOff - Estado", Kind.MONTHLY, "0.00", 23)
    );

    private StandardRemunerations() {
    }

    public static List<Type> types() {
        return TYPES;
    }

    public static boolean isBaseSalary(RemunerationCategory category) {
        return category != null && BASE_SALARY.equals(category.getName());
    }

    public static RemunerationCategory newCategory(Type type, Integer companyId) {
        RemunerationCategory category = new RemunerationCategory();
        category.setName(type.name());
        category.setDescription(type.name());
        category.setSocialSecurityCode(type.socialSecurityCode());
        category.setTaxAuthorityCode(type.taxAuthorityCode());
        category.setType(type.remunerationType());
        category.setContributionRule(type.contributionRule());
        category.setDefaultValue(type.defaultValue());
        category.setActive(true);
        category.setCompanyId(companyId);
        category.setSortOrder(type.sortOrder());
        return category;
    }

    private static Type type(String socialSecurityCode, String taxAuthorityCode, String name,
                             Kind kind, String defaultValue, int sortOrder) {
        return new Type(socialSecurityCode, taxAuthorityCode, name, kind.remunerationType,
                kind.contributionRule, new BigDecimal(defaultValue), sortOrder);
    }

    public record Type(String socialSecurityCode, String taxAuthorityCode, String name,
                       RemunerationCategory.RemunerationType remunerationType,
                       RemunerationCategory.ContributionRule contributionRule,
                       BigDecimal defaultValue, int sortOrder) {
    }

    private enum Kind {
        MONTHLY(RemunerationCategory.RemunerationType.MONTHLY, RemunerationCategory.ContributionRule.SUBJECT),
        DAILY(RemunerationCategory.RemunerationType.DAILY, RemunerationCategory.ContributionRule.SUBJECT),
        MEAL(RemunerationCategory.RemunerationType.DAILY, RemunerationCategory.ContributionRule.MEAL_DAILY);

        private final RemunerationCategory.RemunerationType remunerationType;
        private final RemunerationCategory.ContributionRule contributionRule;

        Kind(RemunerationCategory.RemunerationType remunerationType,
             RemunerationCategory.ContributionRule contributionRule) {
            this.remunerationType = remunerationType;
            this.contributionRule = contributionRule;
        }
    }
}
