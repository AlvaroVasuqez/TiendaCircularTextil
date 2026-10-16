# Textil Circular (Android)

Prototipo móvil para la **Fundación Textil Circular** (caso DSY1105): permite a una emprendedora textil crear su cuenta, gestionar su ficha, publicar su catálogo de trabajos y responder solicitudes de clientes.

## Tecnologías

- Kotlin · Jetpack Compose · Material Design 3
- Arquitectura **MVVM** (ViewModel + StateFlow + Repository)
- Navigation Compose (`NavController` / `NavHost`) con rutas y argumentos
- **Window Size Classes**: barra inferior en teléfono, riel en pantallas medianas y cajón permanente en tablet

## Ejecutar

1. Abrir la carpeta `TextilCircular` en Android Studio (Ladybug o superior, JDK 17+).
2. Sincronizar Gradle y ejecutar en un emulador o dispositivo con Android 7.0+ (API 24).
3. En la pantalla inicial tocar **"Entrar con datos de ejemplo"** para cargar datos ficticios.

Desde terminal:

```powershell
.\gradlew.bat assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Documentación

La explicación paso a paso de cada decisión está en `../PASO_A_PASO.md`.

## Datos

Todos los datos son ficticios, en cumplimiento de la Ley 21.719 y la Ley 19.628 (uso de datasets anonimizados en pruebas académicas).
