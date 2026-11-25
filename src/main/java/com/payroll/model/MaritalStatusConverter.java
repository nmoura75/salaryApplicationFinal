package com.payroll.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class MaritalStatusConverter implements AttributeConverter<MaritalStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(MaritalStatus status) {
        return status == null ? null : status.code();
    }

    @Override
    public MaritalStatus convertToEntityAttribute(Integer code) {
        return code == null ? null : MaritalStatus.fromCode(code);
    }
}
