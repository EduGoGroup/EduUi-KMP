#!/bin/bash
#
# Script para configurar el proyecto Xcode de iOS para EduGo KMP.
#
# Requisitos:
#   - Xcode instalado
#   - xcodegen (brew install xcodegen)
#
# Uso:
#   cd iosApp
#   ./setup-ios.sh
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=== EduGo iOS Setup ==="
echo ""

# 1. Verificar Xcode
if ! command -v xcodebuild &> /dev/null; then
    echo "ERROR: Xcode no esta instalado."
    echo "Instala Xcode desde la App Store."
    exit 1
fi
echo "Xcode: $(xcodebuild -version | head -1)"

# 2. Verificar xcodegen
if ! command -v xcodegen &> /dev/null; then
    echo ""
    echo "xcodegen no encontrado. Instalando con Homebrew..."
    if command -v brew &> /dev/null; then
        brew install xcodegen
    else
        echo "ERROR: Homebrew no esta instalado."
        echo "Instala xcodegen manualmente: brew install xcodegen"
        exit 1
    fi
fi
echo "xcodegen: $(xcodegen --version)"

# 3. Activar enableIos en gradle.properties
GRADLE_PROPS="$PROJECT_ROOT/gradle.properties"
if grep -q "enableIos=false" "$GRADLE_PROPS"; then
    echo ""
    echo "Activando enableIos=true en gradle.properties..."
    sed -i '' 's/enableIos=false/enableIos=true/' "$GRADLE_PROPS"
    echo "enableIos activado."
fi

# 4. Compilar el framework iOS (primera vez, tarda varios minutos)
echo ""
echo "Compilando framework KMP para iOS Simulator..."
echo "(Esto puede tardar varios minutos la primera vez)"
echo ""
cd "$PROJECT_ROOT"
./gradlew :platforms:mobile:app:linkDebugFrameworkIosSimulatorArm64

# 5. Generar proyecto Xcode
echo ""
echo "Generando proyecto Xcode con xcodegen..."
cd "$SCRIPT_DIR"
xcodegen generate

echo ""
echo "=== Setup completado ==="
echo ""
echo "Para ejecutar en simulador:"
echo "  1. Abre iosApp/iosApp.xcodeproj en Xcode"
echo "  2. Selecciona un simulador iOS"
echo "  3. Cmd+R para ejecutar"
echo ""
echo "NOTA: Para desactivar la compilacion iOS (es lenta):"
echo "  Cambia enableIos=true a enableIos=false en gradle.properties"
