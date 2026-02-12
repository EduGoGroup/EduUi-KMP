.PHONY: desktop-debug desktop-run desktop-build clean help

# Ejecutar desktop app en modo debug (espera conexión del debugger en puerto 5005)
desktop-debug:
	./gradlew :platforms:desktop:app:clean :platforms:desktop:app:run --debug-jvm

# Ejecutar desktop app normalmente
desktop-run:
	./gradlew :platforms:desktop:app:run

# Compilar desktop app
desktop-build:
	./gradlew :platforms:desktop:app:desktopMainClasses

# Limpiar build
clean:
	./gradlew clean

# Mostrar ayuda
help:
	@echo "Comandos disponibles:"
	@echo "  make desktop-debug   - Ejecutar desktop app en modo debug (puerto 5005)"
	@echo "  make desktop-run     - Ejecutar desktop app normal"
	@echo "  make desktop-build   - Compilar desktop app"
	@echo "  make clean           - Limpiar proyecto"
