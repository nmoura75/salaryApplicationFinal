package com.payroll.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class BankDetails {

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account")
    private String iban;

    @Column(name = "swift")
    private String swift;
}
