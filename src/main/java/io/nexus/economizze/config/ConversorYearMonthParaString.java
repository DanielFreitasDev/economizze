package io.nexus.economizze.config;

import java.time.YearMonth;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Conversor de YearMonth para string no formato yyyy-MM.
 */
@Component
public class ConversorYearMonthParaString implements Converter<YearMonth, String> {

    @Override
    public String convert(YearMonth source) {
        if (source == null) {
            return null;
        }
        return source.toString();
    }
}
