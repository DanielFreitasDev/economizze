package io.nexus.economizze.shared.util;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Utilitario para calculos de datas e competencia mensal.
 */
public final class DataUtil {

    private DataUtil() {
    }

    /**
     * Retorna o primeiro dia do mes da competencia informada.
     */
    public static LocalDate inicioDaCompetencia(YearMonth competencia) {
        return competencia.atDay(1);
    }

    /**
     * Retorna o ultimo dia do mes da competencia informada.
     */
    public static LocalDate fimDaCompetencia(YearMonth competencia) {
        return competencia.atEndOfMonth();
    }
}
