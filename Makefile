.PHONY: desktop-debug desktop-run desktop-stage desktop-build desktop-clear-session clean help

# Ejecutar desktop app en modo debug (puerto 5005, NO bloquea - attach después de que inicie)
desktop-debug:
	./gradlew :platforms:desktop:app:run -Pdebug=true

# Ejecutar desktop app normalmente (ambiente DEV)
desktop-run:
	./gradlew :platforms:desktop:app:run

# Ejecutar desktop app en ambiente STAGING
desktop-stage:
	./gradlew :platforms:desktop:app:run -Dapp.environment=STAGING

# Compilar desktop app
desktop-build:
	./gradlew :platforms:desktop:app:desktopMainClasses

# Eliminar sesión guardada (fuerza pantalla de login al arrancar)
desktop-clear-session:
	@echo 'import java.util.prefs.*;public class ClearAuth{public static void main(String[] a)throws Exception{var p=Preferences.userRoot().node("com.edugo.storage");p.remove("auth_token");p.remove("auth_user");p.remove("auth_context");p.flush();System.out.println("Sesion eliminada.");}}' > /tmp/ClearAuth.java
	@javac /tmp/ClearAuth.java -d /tmp
	@java -cp /tmp ClearAuth

# Limpiar build
clean:
	./gradlew clean

# Mostrar ayuda
help:
	@echo "Comandos disponibles:"
	@echo "  make desktop-debug   - Ejecutar desktop app en modo debug (puerto 5005)"
	@echo "  make desktop-run     - Ejecutar desktop app normal (DEV)"
	@echo "  make desktop-stage   - Ejecutar desktop app en ambiente STAGING"
	@echo "  make desktop-build          - Compilar desktop app"
	@echo "  make desktop-clear-session  - Eliminar sesion guardada (fuerza login)"
	@echo "  make clean                  - Limpiar proyecto"
