package com.dcnt.take_away_now.value_object.converter;

import com.dcnt.take_away_now.value_object.Dinero;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;
@Converter
public class DineroAttributeConverter implements AttributeConverter<Dinero, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Dinero attribute) {
        return attribute == null ? null : attribute.toBigDecimal();
    }

    @Override
    public Dinero convertToEntityAttribute(BigDecimal dbData) {
        return dbData == null ? null : new Dinero(dbData);
    }
}
