package com.edugo.kmp.logger

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

/**
 * Configuracion de Kermit para iOS/Native.
 *
 * Usa el NSLog writer de Kermit que es el predeterminado para plataformas Apple.
 * Los logs aparecen en la consola de Xcode.
 */
public actual object KermitConfig {
    /**
     * Crea un Logger de Kermit configurado para iOS.
     * En iOS, Kermit usa NSLog por defecto.
     */
    actual fun createLogger(): Logger {
        return Logger.withTag("EduGo")
    }

    /**
     * Crea un Logger con severidad minima configurada.
     */
    actual fun createLoggerWithMinSeverity(minSeverity: Severity): Logger {
        return Logger.withTag("EduGo")
    }
}
