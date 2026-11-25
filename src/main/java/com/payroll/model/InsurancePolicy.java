package com.payroll.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class InsurancePolicy {

    @Column(name = "insurer")
    private String insurer;

    @Column(name = "insurance_policy_number")
    private String policyNumber;
}
