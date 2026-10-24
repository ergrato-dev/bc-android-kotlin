# Versiones del stack

Última revisión: 2026-10-24. Regla: versión completa pinada, nunca `latest`. Antes de fijar una versión: el tag existe, sin CVEs sin parchear, compatibilidad entre piezas.

| Pieza | Versión pinada | Verificación |
|---|---|---|
| Gradle | 9.8.2 | `services.gradle.org/versions/current` (build 2026-10-19). Avisos GHSA-j5m7-59rp-24f5, GHSA-xwqc-3h47-hg64, GHSA-mvvg-497x-hmj8 afectan `< 8.14.6` y `9.0.0 a 9.8.0`; parchados en 9.8.1. 9.8.2 no afectada. Avisos de enero 2026 (CVE-2026-22816, CVE-2026-22865) parchados en 9.3.0. |
| Kotlin | 2.4.21 | Último estable en GitHub `JetBrains/kotlin` (2026-10-08). Sin avisos de seguridad publicados. 2.5.0-Beta2 existe: no usar. |
| Android Gradle Plugin | 9.4.1 | Último estable en Google Maven (9.5.0 solo rc, 9.6.0 alpha). Sin CVEs en NVD. Mínimo de Gradle 9.6.0 (tabla oficial), cumplido por 9.8.2. Android Studio mínimo Quail 4 (2026.1.4). |
| JUnit (BOM) | 6.1.3 | Último en Maven Central. `kotlin-test-junit5` trae 5.10.1 por defecto; se fija el BOM 6.1.3 de forma explícita. Sin avisos de seguridad. Verificado con los 3 starters de la semana 1. |
| Compose BOM | 2026.09.00 | Último en Google Maven. |
| Android Studio | Quail 4 (2026.1.4) o superior | Mínimo para AGP 9.4. Rabbit 1 (2026.2.1) también lo soporta. Fijar build exacto en la semana 15. |
| JDK | 21 (LTS), Temurin 21.0.12 | Decisión del autor. Compilado y probado con Temurin 21.0.12 (semana 1). AGP 8.x exige JDK 17+; confirmar con AGP 9.4 en la semana 9. |
| compileSdk / targetSdk | 37 / 36 mínimo Play | Android 17 (API 37) estable desde junio 2026. Play exige targetSdk 36 desde 2026-08-31. |

Pendiente: build exacto de Android Studio (semana 15, cuando se usa) y JDK 21 con AGP 9.4 (semana 9).
