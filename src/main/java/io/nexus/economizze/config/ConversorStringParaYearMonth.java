package io.nexus.economizze.config;

import java.time.YearMonth;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Conversor de string no formato yyyy-MM para YearMonth.
 */
@Component
public class ConversorStringParaYearMonth implements Converter<String, YearMonth> {

    @Override
    public YearMonth convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        return YearMonth.parse(source);
    }
}
