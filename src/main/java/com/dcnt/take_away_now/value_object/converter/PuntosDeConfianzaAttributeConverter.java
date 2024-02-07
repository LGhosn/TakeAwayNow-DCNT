package com.dcnt.take_away_now.value_object.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.dcnt.take_away_now.value_object.PuntosDeConfianza;


@Converter
public class PuntosDeConfianzaAttributeConverter implements AttributeConverter<PuntosDeConfianza, Double> {

    @Override
    public Double convertToDatabaseColumn(PuntosDeConfianza attribute) {
        return attribute == null ? null : attribute.getCantidad();
    }

    @Override
    public PuntosDeConfianza convertToEntityAttribute(Double dbData) {
        return dbData == null ? null : new PuntosDeConfianza(dbData);
    }
}
