# Proyecto hilo conductor: Bitácora de campo

Una app Android para registrar visitas e inspecciones en campo: observaciones con fotos, GPS y notas, que funciona **sin conexión** y sincroniza después. Cada semana añade una capa al mismo sistema. El dominio (agro, construcción, etc.) lo eliges en [dominios.md](dominios.md).

| Fase | Qué crece |
|---|---|
| 1 Kotlin (sem. 1-8) | Módulo `core` en Kotlin puro: modelo, validación, informes, coroutines y Flow |
| 2 Gradle (9-14) | El proyecto se vuelve multi-módulo: `core`, `data`, `build-logic` con convention plugins |
| 3 Fundamentos (15-20) | La primera app: pantallas, permisos de cámara y ubicación, flavors, firma |
| 4 Compose (21-27) | La interfaz completa: lista, detalle, formulario, mapa, tema, adaptive |
| 5 Arquitectura (28-33) | Room, sincronización con WorkManager, red con Ktor/Retrofit, Hilt, offline-first |
| 6 Calidad (34-38) | Tests, Macrobenchmark, Baseline Profiles, R8, seguridad |
| 7 Producción (39-42) | CI/CD, AAB firmado, publicación en Play, capstone |

## Reglas

- Un repositorio por aprendiz: `bc-android-kotlin-<tu-dominio>`.
- Una rama y un *pull request* por semana (`week-NN` → `main`).
- Nombres del código en inglés; datos y textos de la interfaz en el idioma del dominio.
- El `core` de la semana 1 no se descarta: es la base de todas las demás.
