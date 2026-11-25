package com.payroll.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class WithholdingTableConverter implements AttributeConverter<WithholdingTable, Integer> {

    @Override
    public Integer convertToDatabaseColumn(WithholdingTable table) {
        return table == null ? null : table.code();
    }

    @Override
    public WithholdingTable convertToEntityAttribute(Integer code) {
        return code == null ? null : WithholdingTable.fromCode(code);
    }
}
